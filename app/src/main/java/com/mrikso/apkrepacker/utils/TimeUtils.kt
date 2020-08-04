package com.mrikso.apkrepacker.utils

class TimeUtils {

    fun formatStopWatchTime(seconds : Long?): String{
        seconds?.let {
            val hours: Long = (seconds/(1000 *60 * 60))%24
            val minutes: Long = (seconds/(1000 *60)) % 60
            val secs: Long = (seconds/1000) % 60

            return String.format("%02d:%02d:%02d", hours, minutes, secs)
        }
        return ""
    }
}