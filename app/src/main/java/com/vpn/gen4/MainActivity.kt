package com.vpn.gen4

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vpn.gen4.core.VpnCoreService

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST_CODE = 100
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnConnect.setOnClickListener {
            if (!isConnected) {
                // VPN permission check
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, VPN_REQUEST_CODE)
                } else {
                    onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
                }
            } else {
                // Stop VPN
                stopService(Intent(this, VpnCoreService::class.java))
                isConnected = false
                tvStatus.text = "Status: Disconnected"
                btnConnect.text = "CONNECT"
                btnConnect.setBackgroundColor(android.graphics.Color.parseColor("#0284C7"))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            val intent = Intent(this, VpnCoreService::class.java)
            startService(intent)
            isConnected = true
            
            val tvStatus = findViewById<TextView>(R.id.tvStatus)
            val btnConnect = findViewById<Button>(R.id.btnConnect)
            
            tvStatus.text = "Status: Connected (4th-Gen Secure)"
            btnConnect.text = "DISCONNECT"
            btnConnect.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"))
        }
    }
}
