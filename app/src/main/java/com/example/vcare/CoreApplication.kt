package com.example.vcare

import android.app.Application
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CoreApplication:Application()

