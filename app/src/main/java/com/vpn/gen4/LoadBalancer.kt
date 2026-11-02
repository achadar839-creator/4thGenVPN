package com.vpn.gen4

import java.io.File

object LoadBalancer {
    fun getServerByCountry(configDirPath: String, countryCode: String): VpnServer? {
        // जो देश चुना जाएगा (जैसे "SG"), यह उसी की .conf फाइल को उठाएगा
        val targetFile = File(configDirPath, "$countryCode.conf")
        
        if (targetFile.exists()) {
            return parseWgConfig(targetFile.absolutePath)
        }
        return null
    }
}
