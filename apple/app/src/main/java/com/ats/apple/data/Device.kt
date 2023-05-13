package com.ats.apple.data

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.ats.apple.MainActivity

abstract class Device() {

    abstract val imageResource: Int

    abstract val defaultDeviceNameWithId: String

    abstract val deviceContext: DeviceContext

    fun getDrawable(): Drawable? {
        val context = MainActivity().applicationContext
        return AppCompatResources.getDrawable(context, imageResource)
    }

    fun isConnectable(): Boolean {
        return this is Connectable
    }
}