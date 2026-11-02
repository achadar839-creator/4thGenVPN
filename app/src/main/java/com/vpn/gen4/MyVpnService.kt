package com.vpn.gen4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.File
import java.io.FileInputStream

class MyVpnService : VpnService(), Tunnel {
    private lateinit var backend: GoBackend
    private var isRunning = false

    companion object {
        const val CHANNEL_ID = "VpnChannel"
        const val NOTIF_ID = 1
    }

    override fun getName() = "4thGenTunnel"

    override fun onStateChange(newState: Tunnel.State) {}

    override fun onCreate() {
        super.onCreate()
        backend = GoBackend(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val cc = intent?.getStringExtra("COUNTRY_CODE") ?: "US"
        startForegroundNotification(cc)
        startWireGuardTunnel(cc)
        return START_STICKY
    }

    private fun startForegroundNotification(country: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "4th-Gen VPN Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("4TH-GEN VPN Connected")
            .setContentText("Tunnel active for server: $country")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
        startForeground(NOTIF_ID, notification)
    }

    private fun startWireGuardTunnel(cc: String) {
        if (isRunning) return
        try {
            val configFile = File(filesDir, "configs/$cc.conf")
            if (configFile.exists()) {
                val config = Config.parse(FileInputStream(configFile))
                backend.setState(this, Tunnel.State.UP, config)
                isRunning = true
            }
        } catch(e: Exception) {
            e.printStackTrace()
            stopVpn()
        }
    }

    private fun stopVpn() {
        if (isRunning) {
            try { backend.setState(this, Tunnel.State.DOWN, null) } catch(e: Exception) {}
            isRunning = false
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
