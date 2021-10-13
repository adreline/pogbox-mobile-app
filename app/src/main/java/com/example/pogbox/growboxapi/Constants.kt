package com.example.pogbox.growboxapi

class Constants {

    companion object{
        const val DST_URL="http://192.168.1.16/api/getdst.php"
        const val DHT_URL="http://192.168.1.16/api/getdht.php"
        const val DHT2_URL="http://192.168.1.16/api/getdht2.php"
        const val GL_URL="http://192.168.1.16/api/growlight.php?switch="
        const val EXH_URL="http://192.168.1.16/api/exhaust.php?switch="
        const val SET_SCHEDULE_URL="http://192.168.1.16/api/makeschedule.php?"
        const val GET_SCHEDULE_URL="http://192.168.1.16/api/getschedule.php"
        const val SERVER_INFO="http://192.168.1.16/api/getsysteminfo.php"
        const val PLOTS_DIR="http://192.168.1.16/api/plots/"
    }

}