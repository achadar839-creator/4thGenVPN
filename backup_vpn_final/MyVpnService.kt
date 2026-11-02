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

    @Volatile
    private var vpnInterface: ParcelFileDescriptor? = null

    private var isRunning = false

    companion object {
        private const val TAG = "MyVpnService"
        private const val CHANNEL_ID = "vpn_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val country = intent?.getStringExtra("SELECTED_COUNTRY")
            ?: "Optimal Server"

        if (isRunning) return START_NOT_STICKY

        Log.d(TAG, "Starting VPN service: $country")

        try {
            createNotificationChannel()
            startForeground(
                NOTIFICATION_ID,
                buildNotification(
                    "Connecting to $country",
                    "Establishing VPN interface..."
                )
            )

            isRunning = true

            Thread {
                startVpnTunnel(country)
            }.start()

        } catch (e: Exception) {
            Log.e(TAG, "Foreground service startup failed", e)
            isRunning = false
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "4TH-GEN VPN Connection",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        message: String
    ): Notification {

        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(
        title: String,
        message: String
    ) {
        val notification = buildNotification(title, message)

        getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, notification)
    }

    private fun startVpnTunnel(country: String) {
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 32)
                .setSession("4TH-GEN VPN ($country)")
                .setMtu(1500)

            // Temporary interface test:
            // No default route is added until a real VPN
            // packet-processing tunnel is integrated.

            val established = builder.establish()

            if (established != null) {
                vpnInterface = established

                Log.d(TAG, "VPN interface established for $country")

                updateNotification(
                    "4TH-GEN VPN Interface Active",
                    "$country selected — server tunnel pending"
                )

            } else {
                Log.e(TAG, "VPN interface establishment failed")

                updateNotification(
                    "4TH-GEN VPN Failed",
                    "Could not establish VPN interface"
                )

                stopVpn()
            }

        } catch (e: Exception) {
            Log.e(TAG, "VPN tunnel error", e)

            updateNotification(
                "4TH-GEN VPN Error",
                e.message ?: "Connection failed"
            )

            stopVpn()
        }
    }

    private fun stopVpn() {
        isRunning = false

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }

        vpnInterface = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        Log.d(TAG, "VPN permission revoked")
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        Log.d(TAG, "VPN service destroyed")

        isRunning = false

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Interface cleanup error", e)
        }

        vpnInterface = null

        super.onDestroy()
    }
}
