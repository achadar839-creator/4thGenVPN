package com.vpn.gen4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var selectedCountryCode = "SG" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        val btnSelectCountry = findViewById<Button>(R.id.btnSelectCountry)
        val btn2xBridge = findViewById<Button>(R.id.btn2xBridge)

        val configDirPath = "${filesDir.absolutePath}/configs"

        val countries = arrayOf("Singapore", "Japan", "Canada", "Switzerland", "Mexico", "Netherlands", "Norway")
        val countryCodes = arrayOf("SG", "JP", "CA", "CH", "MX", "NL", "NO")

        btnSelectCountry.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Working Global Server")
                .setItems(countries) { _, which ->
                    selectedCountryCode = countryCodes[which]
                    btnSelectCountry.text = "Server: ${countries[which]}"
                    Toast.makeText(this, "${countries[which]} Selected", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        btn2xBridge.setOnClickListener {
            selectedCountryCode = "BRIDGE_ASIA"
            btnSelectCountry.text = "Server: 2X BRIDGE (SG+JP)"
            Toast.makeText(this, "2X Bridge Active! Click Connect.", Toast.LENGTH_LONG).show()
        }

        btnConnect.setOnClickListener {
            val selectedServer = LoadBalancer.getServerByCountry(configDirPath, selectedCountryCode)

            if (selectedServer != null) {
                val vpnConfig = VpnEngine.buildConfig(selectedServer)
                startVpnTunnel(vpnConfig)
                Toast.makeText(this, "Connecting to $selectedCountryCode...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "$selectedCountryCode config missing!", Toast.LENGTH_SHORT).show()
            }
        }

        btnDisconnect.setOnClickListener {
            val stopIntent = Intent(this, MyVpnService::class.java)
            stopIntent.action = "ACTION_DISCONNECT"
            startService(stopIntent)
            Toast.makeText(this, "VPN Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVpnTunnel(config: com.wireguard.config.Config) {
        // Wireguard logic
    }
}
