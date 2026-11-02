package com.vpn.gen4

import android.app.*
import android.content.*
import android.net.TrafficStats
import android.os.*
import androidx.core.app.NotificationCompat
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.*
import kotlin.concurrent.thread

class MyVpnService : Service() {
    private var isRunning = false
    private lateinit var notifMgr: NotificationManager
    private lateinit var notifBuilder: NotificationCompat.Builder
    private val updateHandler = Handler(Looper.getMainLooper())
    private var lastRx = TrafficStats.getTotalRxBytes()
    private var lastTx = TrafficStats.getTotalTxBytes()

    private val wgTunnel = object : Tunnel {
        override fun getName() = "wg0"
        override fun onStateChange(newState: Tunnel.State) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopVpn(); return START_NOT_STICKY }
        val country = intent?.getStringExtra("SELECTED_COUNTRY") ?: "Optimal Server"
        notifMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("VPN_CH", "4th-Gen Status", NotificationManager.IMPORTANCE_LOW)
            notifMgr.createNotificationChannel(channel)
        }
        
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = Intent(this, MyVpnService::class.java).apply { action = "STOP" }
        val spi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        
        notifBuilder = NotificationCompat.Builder(this, "VPN_CH")
            .setContentTitle("Connected to $country").setContentText("↓ 0.0 B/s | ↑ 0.0 B/s")
            .setSmallIcon(android.R.drawable.sym_def_app_icon).setContentIntent(pi)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISCONNECT", spi)
            .setOnlyAlertOnce(true)
            
        startForeground(1, notifBuilder.build())
        setupRealWireGuardVpn()
        startLiveSpeedUpdate()
        return START_STICKY
    }

    private fun setupRealWireGuardVpn() {
        thread {
            try {
                val wgInt = Interface.Builder().addAddress(InetNetwork.parse("10.0.0.2/32")).parsePrivateKey("yAnz5TF+lXXJte14tji3zlMNq+hd2rYUMIWPnjhlXEQ=").build()
                val wgPeer = Peer.Builder().addAllowedIp(InetNetwork.parse("0.0.0.0/0")).parsePublicKey("bmXOC+F1FxEMF9dyiK2H5/1SUtzH0JuVo51h2wPfgyo=").parseEndpoint("162.159.192.1:2408").build()
                val config = Config.Builder().setInterface(wgInt).addPeer(wgPeer).build()
                WgEngine.getBackend()?.setState(wgTunnel, Tunnel.State.UP, config)
                isRunning = true
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun startLiveSpeedUpdate() {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                val rx = TrafficStats.getTotalRxBytes()
                val tx = TrafficStats.getTotalTxBytes()
                val down = formatBytes(rx - lastRx)
                val up = formatBytes(tx - lastTx)
                lastRx = rx; lastTx = tx
                notifBuilder.setContentText("↓ $down/s | ↑ $up/s")
                notifMgr.notify(1, notifBuilder.build())
                updateHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576f)
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024f)
        else -> "$bytes B"
    }

    private fun stopVpn() {
        isRunning = false
        updateHandler.removeCallbacksAndMessages(null)
        thread { try { WgEngine.getBackend()?.setState(wgTunnel, Tunnel.State.DOWN, null) } catch (e: Exception) {} }
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() { super.onDestroy(); stopVpn() }
}
