package com.vpn.gen4

import java.io.File

object LoadBalancer {
    fun getServerFilePath(configDirPath: String, countryCode: String): String? {
        if (countryCode == "BRIDGE_ASIA") {
            val sgFile = File(configDirPath, "SG.conf")
            val jpFile = File(configDirPath, "JP.conf")
            val available = mutableListOf<File>()
            if (sgFile.exists()) available.add(sgFile)
            if (jpFile.exists()) available.add(jpFile)
            if (available.isNotEmpty()) {
                return available.random().absolutePath
            }
            return null
        }
        val targetFile = File(configDirPath, "$countryCode.conf")
        if (targetFile.exists()) {
            return targetFile.absolutePath
        }
        return null
    }
}
