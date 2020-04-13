package com.vincent.android.pirsensor.utils

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.logd(message:String){

    Log.d(LOG_TAG,message)
}


fun FragmentActivity.loge(message:String){

    Log.e(LOG_TAG,message)
}