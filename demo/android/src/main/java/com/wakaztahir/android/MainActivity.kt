package com.wakaztahir.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.wakaztahir.common.DisplayDemo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DisplayDemo()
    }
}