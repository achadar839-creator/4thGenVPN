package com.vpn.gen4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val country = intent?.getStringExtra("SELECTED_COUNTRY") ?: "Optimal Server"
        Log.d("MyVpnService", "Starting VPN connection for: $country")
        
        showNotification(country)

        if (!isRunning) {
            isRunning = true
            Thread {
                startVpnTunnel(country)
            }.start()
        }
        return START_STICKY
    }

    private fun showNotification(country: String) {
        val channelId = "vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "VPN Connection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("4TH-GEN VPN Connected")
            .setContentText("Securely connected to $country")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
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
            if (vpnInterface != null) {
                Log.d("MyVpnService", "VPN Interface established successfully for $country")
            } else {
                Log.e("MyVpnService", "Failed: vpnInterface is null")
            }
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error establishing VPN tunnel", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("MyVpnService", "VPN Service destroyed")
    }
}
