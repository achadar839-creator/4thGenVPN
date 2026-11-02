package com.vpn.gen4

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val country = intent?.getStringExtra("SELECTED_COUNTRY") ?: "Optimal Server"
        Log.d("MyVpnService", "Starting VPN connection for: $country")
        
        if (!isRunning) {
            isRunning = true
            startVpnTunnel(country)
        }
        return START_STICKY
    }

    private fun startVpnTunnel(country: String) {
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .setSession("4TH-GEN VPN ($country)")
                .setMtu(1500)

            vpnInterface = builder.establish()
            Log.d("MyVpnService", "VPN Interface established successfully for $country")
        } catch (e: Exception) {
            Log.e("MyVpnService", "Failed to establish VPN tunnel", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("MyVpnService", "VPN Service destroyed")
    }
}
