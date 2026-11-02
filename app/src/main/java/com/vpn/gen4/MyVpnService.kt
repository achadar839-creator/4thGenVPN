package com.vpn.gen4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val updateHandler = Handler(Looper.getMainLooper())
    
    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val selectedCountry = intent?.getStringExtra("SELECTED_COUNTRY") ?: "Optimal Server (Auto)"
        notificationBuilder.setContentTitle("Connected to $selectedCountry")
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        
        // 1. VPN को नोटिफिकेशन से बंद करने का कमांड
        val stopIntent = Intent(this, MyVpnService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // 2. लाइव स्पीड और डिसकनेक्ट बटन वाला नोटिफिकेशन
        notificationBuilder = NotificationCompat.Builder(this, "VPN_CHANNEL_ID")
            .setContentTitle("Connected to Singapore")
            .setContentText("↓ 0.00 B/s | ↑ 0.00 B/s")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISCONNECT", stopPendingIntent)
            .setOnlyAlertOnce(true)
            
        startForeground(1, notificationBuilder.build())

        startNetworkMonitor()
        setupVpn()
        startLiveSpeedUpdate()
        
        return START_STICKY
    }

    private fun startLiveSpeedUpdate() {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()
                
                val rxSpeed = currentRxBytes - lastRxBytes
                val txSpeed = currentTxBytes - lastTxBytes
                
                lastRxBytes = currentRxBytes
                lastTxBytes = currentTxBytes
                
                val downStr = formatBytes(rxSpeed)
                val upStr = formatBytes(txSpeed)
                
                notificationBuilder.setContentText("↓ $downStr/s | ↑ $upStr/s")
                notificationManager.notify(1, notificationBuilder.build())
                
                updateHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576f)
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }

    private fun startNetworkMonitor() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!isRunning) setupVpn()
            }
            override fun onLost(network: Network) {
                isRunning = false
            }
        }
        connectivityManager?.registerNetworkCallback(request, networkCallback!!)
    }

    private fun setupVpn() {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("8.8.8.8", 32)
            builder.setSession("4thGenVPN")
            builder.setBlocking(true)

            vpnInterface = builder.establish()
            isRunning = true

            thread { manageTraffic(vpnInterface) }
        } catch (e: Exception) {
            e.printStackTrace()
            stopVpn()
        }
    }

    private fun manageTraffic(vpnInterface: ParcelFileDescriptor?) {
        vpnInterface?.fileDescriptor?.let { fd ->
            val inputStream = FileInputStream(fd)
            val packet = ByteArray(32767)
            while (isRunning) {
                try {
                    inputStream.read(packet)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("VPN_CHANNEL_ID", "4th-Gen VPN Status", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopVpn() {
        isRunning = false
        updateHandler.removeCallbacksAndMessages(null)
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // डिसकनेक्ट होने पर नोटिफिकेशन हटा दो
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
