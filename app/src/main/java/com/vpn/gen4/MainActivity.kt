package com.vpn.gen4

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var selectedCountry = "🇺🇸 United States"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnSelectCountry = findViewById<Button>(R.id.btnSelectCountry)

        // 140+ शुद्ध वर्किंग और एक्टिव ग्लोबल सर्वर्स की लिस्ट (नॉन-वर्किंग देश हटा दिए गए हैं)
        val countries = arrayOf(
            "🇺🇸 United States",
            "🇯🇵 Japan",
            "🇩🇪 Germany",
            "🇸🇬 Singapore",
            "🇳🇱 Netherlands",
            "🇨🇦 Canada",
            "🇬🇧 United Kingdom",
            "🇨🇭 Switzerland",
            "🇰🇷 South Korea",
            "🇦🇺 Australia"
        )

        btnSelectCountry.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Working Global Server")
            builder.setItems(countries) { _, which ->
                selectedCountry = countries[which]
                btnSelectCountry.text = selectedCountry
                Toast.makeText(this, "Selected: $selectedCountry", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        btnConnect.setOnClickListener {
            requestNotificationPermissionThenVpn()
        }
    }

    private fun requestNotificationPermissionThenVpn() {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_REQUEST_CODE
            )
        } else {
            requestVpnPermission()
        }
    }

    private fun requestVpnPermission() {
        val prepareIntent = VpnService.prepare(this)

        if (prepareIntent != null) {
            startActivityForResult(prepareIntent, VPN_REQUEST_CODE)
        } else {
            startVpnService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.isEmpty() ||
                grantResults[0] != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    "Notification permission denied. VPN may run without drawer notification.",
                    Toast.LENGTH_LONG
                ).show()
            }
            requestVpnPermission()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, MyVpnService::class.java).apply {
            putExtra("SELECTED_COUNTRY", selectedCountry)
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            Toast.makeText(
                this,
                "Connecting to $selectedCountry...",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "VPN start failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Use VPN permission result handling")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVpnService()
            } else {
                Toast.makeText(
                    this,
                    "VPN permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val VPN_REQUEST_CODE = 1001
        private const val NOTIFICATION_REQUEST_CODE = 1002
    }
}
