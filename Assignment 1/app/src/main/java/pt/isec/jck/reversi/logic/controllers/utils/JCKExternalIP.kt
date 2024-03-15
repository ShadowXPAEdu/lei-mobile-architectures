package pt.isec.jck.reversi.logic.controllers.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

fun getExternalIP(): String {
    val urls = arrayOf(
        "https://ipinfo.io/ip",
        "https://checkip.amazonaws.com/",
        "https://api.ipify.org",
        "https://api-ipv4.ip.sb/ip"
    )
    for (url in urls) {
        try {
            val extIP = URL(url)
            val br = BufferedReader(InputStreamReader(extIP.openStream()))
            return br.readLine()
        } catch (ex: IOException) {
        }
    }
    return "127.0.0.1"
}
