package net.hax.niatool

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = "MainActivity"
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
            if (isChecked) startOverlay() else stopOverlay()
        }

        // Version note configuration
        (findViewById(R.id.version_note) as TextView).text =
                getString(R.string.text_version_note, packageManager.getPackageInfo(packageName, 0).versionName)
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
