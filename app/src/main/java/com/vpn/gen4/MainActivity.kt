package com.vpn.gen4

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    private var isConnected = false
    private val servers = arrayOf("Optimal Server (Auto)", "🇺🇸 United States", "🇬🇧 United Kingdom", "🇯🇵 Japan", "🇸🇬 Singapore", "🇩🇪 Germany", "🇮🇳 India")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WgEngine.init(applicationContext)

        val btnSelectServer = findViewById<CardView>(R.id.btnSelectServer)
        val tvCurrentServer = findViewById<TextView>(R.id.tvCurrentServer)
        val btnConnect = findViewById<CardView>(R.id.btnConnect)
        val btnSettings = findViewById<TextView>(R.id.btnSettings)

        btnSettings.setOnClickListener {
            val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            builder.setTitle("Settings").setMessage("Advanced 4th-Gen features coming soon!")
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }.show()
        }

        btnSelectServer.setOnClickListener {
            val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            builder.setTitle("Select Global Node")
            builder.setItems(servers) { _, which ->
                tvCurrentServer.text = servers[which]
                tvCurrentServer.setTextColor(Color.parseColor("#39FF14"))
            }
            builder.show()
        }

        btnConnect.setOnClickListener {
            if (!isConnected) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 1)
                } else {
                    startVpn(tvCurrentServer.text.toString())
                }
            } else {
                stopVpn()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val tvCurrentServer = findViewById<TextView>(R.id.tvCurrentServer)
            startVpn(tvCurrentServer.text.toString())
        }
    }

    private fun startVpn(selectedCountry: String) {
        isConnected = true
        val vpnIntent = Intent(this, MyVpnService::class.java).apply {
            putExtra("SELECTED_COUNTRY", selectedCountry)
        }
        startService(vpnIntent)
        
        findViewById<TextView>(R.id.tvConnectState).text = "CONNECTED"
        findViewById<TextView>(R.id.tvConnectState).setTextColor(Color.parseColor("#39FF14"))
    }

    private fun stopVpn() {
        isConnected = false
        val vpnIntent = Intent(this, MyVpnService::class.java).apply { action = "STOP" }
        startService(vpnIntent)

        findViewById<TextView>(R.id.tvConnectState).text = "CONNECT"
        findViewById<TextView>(R.id.tvConnectState).setTextColor(Color.WHITE)
    }
}
