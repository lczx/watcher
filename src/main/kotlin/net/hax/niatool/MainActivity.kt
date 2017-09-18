package net.hax.niatool

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = "MainActivity"
        private val REQUEST_CODE_PERM_SYSTEM_ALERT = 1337
    }

    private lateinit var mServiceToggleSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        // Service toggle switch configuration
        mServiceToggleSwitch = findViewById(R.id.switch_service_toggle) as SwitchCompat
        mServiceToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Note: this gets invoked even when we toggle the switch programmatically,
            // but start/stopService never start or stop the service twice, so we are OK
            if (isChecked) attemptStartOverlay() else stopOverlay()
        }

        // Version note configuration
        (findViewById(R.id.version_note) as TextView).text =
                getString(R.string.text_version_note, packageManager.getPackageInfo(packageName, 0).versionName)
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
        startService(Intent(this, OverlayService::class.java))
    }

    private fun stopOverlay() {
        Log.d(TAG, "Stopping overlay service")
        stopService(Intent(this, OverlayService::class.java))
    }

}
