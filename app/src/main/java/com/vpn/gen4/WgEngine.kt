package com.vpn.gen4

import android.content.Context
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend

object WgEngine {
    private var backend: Backend? = null

    fun init(context: Context) {
        if (backend == null) {
            backend = GoBackend(context)
        }
    }

    fun getBackend(): Backend? {
        return backend
    }
}
