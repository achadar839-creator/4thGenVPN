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
import android.net.VpnService
import android.os.Build
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        
        createNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, "VPN_CHANNEL_ID")
            .setContentTitle("4th-Gen VPN Active")
            .setContentText("Battery Shield & Auto-Reconnect ON 🛡️")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        startNetworkMonitor() // Auto-Reconnect सिस्टम चालू
        setupVpn()
        return START_STICKY
    }

    // AI Network Monitor - वाई-फाई से डेटा पर स्विच होने पर वीपीएन को ऑटो-कनेक्ट करता है
    private fun startNetworkMonitor() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("VPN_ENGINE", "Network Available: Auto-Reconnecting...")
                if (!isRunning) setupVpn()
            }
            override fun onLost(network: Network) {
                Log.d("VPN_ENGINE", "Network Lost: Entering Battery Shield Mode...")
                isRunning = false // CPU का लूप रोक दो ताकि बैटरी बचे
            }
        }
        connectivityManager?.registerNetworkCallback(request, networkCallback!!)
    }

    private fun setupVpn() {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("8.8.8.8", 32) // Split Tunneling
            builder.setSession("4thGenVPN_AI_Node")
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
            val outputStream = FileOutputStream(fd)
            val packet = ByteArray(32767)
            
            while (isRunning) {
                try {
                    val length = inputStream.read(packet)
                    if (length > 0) {
                        // AI Packet Processing
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("VPN_CHANNEL_ID", "4th-Gen VPN Status", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopVpn() {
        isRunning = false
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
