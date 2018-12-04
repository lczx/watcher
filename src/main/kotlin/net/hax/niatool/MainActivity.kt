package net.hax.niatool

import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import net.hax.niatool.updater.UpdateData
import net.hax.niatool.updater.UpdateInstaller
import net.hax.niatool.updater.UpdateManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERM_SYSTEM_ALERT = 1337
    }

    private val floatingSettingsWindowDelegate = lazy { FloatingSettingsWindow(this) }
    private val floatingSettingsWindow by floatingSettingsWindowDelegate
    private lateinit var mServiceToggleSwitch: SwitchCompat
    private var pendingUpdateToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationSettings.initialize(applicationContext)
        setTheme(R.style.AppTheme_MainActivity) // Get rid of the launcher theme
        setContentView(R.layout.activity_main)

        // Toolbar configuration
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // Service toggle switch configuration
        mServiceToggleSwitch = findViewById(R.id.switch_service_toggle) as SwitchCompat
        mServiceToggleSwitch.isSaveEnabled = false
        mServiceToggleSwitch.setOnLongClickListener { v ->
            val location = calculateSwitchTipLocation(v)
            val cheatSheet = Toast.makeText(this, R.string.toast_service_toggle, Toast.LENGTH_SHORT)
            cheatSheet.setGravity(Gravity.TOP or GravityCompat.START, location.x, location.y)
            cheatSheet.show(); true
        }
        mServiceToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Note: this gets invoked even when we toggle the switch programmatically,
            // but start/stopService never start or stop the service twice, so we are OK
            if (isChecked) attemptStartOverlay() else stopOverlay()
        }

        // Version note & long-click force update
        val updateManger = UpdateManager(this, UpdateListener())
        with(findViewById(R.id.version_note) as TextView) {
            val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
            text = getString(R.string.text_version_note, currentVersion)
            setOnLongClickListener {
                pendingUpdateToast = Toast.makeText(this@MainActivity, R.string.toast_version_check_force, Toast.LENGTH_SHORT)
                pendingUpdateToast!!.show()
                updateManger.run(force = true)
                true
            }
        }

        // Check for updates
        updateManger.run()
    }

    override fun onStart() {
        super.onStart()

        // Update the switch depending if our service is running or not
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        mServiceToggleSwitch.isChecked = manager.getRunningServices(Integer.MAX_VALUE).find {
            OverlayService::class.java.name == it.service.className
        } != null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.open_settings) {
            val dp = resources.displayMetrics.density
            floatingSettingsWindow.showAtLocation(findViewById(R.id.open_settings), Gravity.TOP or GravityCompat.END,
                    (4 * dp).toInt(), obtainStatusBarHeight(resources) + (4 * dp).toInt())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingSettingsWindowDelegate.isInitialized() && floatingSettingsWindow.isShowing)
            floatingSettingsWindow.dismiss()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // The user returned from the system settings...
        if (requestCode == REQUEST_CODE_PERM_SYSTEM_ALERT) {
            if (Settings.canDrawOverlays(this)) // ...and gave us the permission to start the overlay
                startOverlay()
            else // ...but did not gave us the permission, uncheck the switch
                mServiceToggleSwitch.isChecked = false
        }
    }

    private fun attemptStartOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // If we are on Marshmallow, we need to ask for the permission... and listen in #onActivityResult()
            val acceptAction = DialogInterface.OnClickListener { _, _ ->
                startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                        REQUEST_CODE_PERM_SYSTEM_ALERT)
            }
            val dismissAction = DialogInterface.OnClickListener { _, _ ->
                mServiceToggleSwitch.isChecked = false
            }

            AlertDialog.Builder(this)
                    .setTitle(R.string.overlay_permission_title)
                    .setMessage(R.string.overlay_permission_message)
                    .setPositiveButton(android.R.string.ok, acceptAction) // "Yes" button
                    .setNegativeButton(android.R.string.cancel, dismissAction) // "No" button
                    .setOnCancelListener { dismissAction.onClick(null, 0) } // Back / Click outside
                    .show()
        } else {
            // ...otherwise we can start the overlay right now
            startOverlay()
        }
    }

    private fun startOverlay() {
        Log.d(TAG, "Starting overlay service")
        startService(OverlayServiceUtil.createStartIntent(this, "glyph"))
    }

    private fun stopOverlay() {
        Log.d(TAG, "Stopping overlay service")
        startService(OverlayServiceUtil.createStopIntent(this))
    }

    private fun calculateSwitchTipLocation(v: View): Point {
        // Simulate ActionMenuItemView#onLongClick() toast positioning
        val toolbarPos = IntArray(2)
        val screenPos = IntArray(2)
        v.getLocationOnScreen(screenPos)
        (v.parent as View).getLocationOnScreen(toolbarPos)
        val displayFrame = Rect()
        v.getWindowVisibleDisplayFrame(displayFrame)

        var referenceX = screenPos[0] + v.width / 2
        if (ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_RTL)
            referenceX = v.resources.displayMetrics.widthPixels - referenceX // mirror

        return Point(referenceX, toolbarPos[1] + (v.parent as View).height - displayFrame.top)
    }

    inner class UpdateListener : UpdateManager.EventListener {
        override fun onUpdateAvailable(updateManager: UpdateManager, updateData: UpdateData, cached: Boolean, forced: Boolean) {
            val newVersionChip = findViewById(R.id.version_new)
            newVersionChip.visibility = View.VISIBLE
            newVersionChip.setOnClickListener {
                val alert = AlertDialog.Builder(this@MainActivity).setTitle(R.string.dialog_update_title)
                if (updateManager.canDownload) {
                    alert.setMessage(getString(R.string.dialog_update_text, updateData.version.pretty))
                            .setNegativeButton(R.string.dialog_update_button_no, null)
                            .setPositiveButton(R.string.dialog_update_button_yes) { _, _ ->
                                UpdateInstaller(this@MainActivity).downloadAndInstall(updateData)
                            }
                } else {
                    alert.setMessage(getString(R.string.dialog_update_text_offline, updateData.version.pretty))
                            .setPositiveButton(android.R.string.ok, null)
                }
                alert.show()
            }
        }

        override fun onNoUpdate(updateManager: UpdateManager, cached: Boolean, forced: Boolean) {
            if (forced) {
                pendingUpdateToast?.cancel()
                Toast.makeText(this@MainActivity, R.string.toast_version_no_update, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
