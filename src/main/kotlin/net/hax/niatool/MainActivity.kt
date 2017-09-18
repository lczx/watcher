package net.hax.niatool

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.util.Log

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
            // TODO: actually create a service
            Log.i(TAG, "Thins thing changed? Outstanding!")
        }
    }

}