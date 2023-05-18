package com.ats.apple.data.filter.models

import com.ats.apple.data.model.BaseDevice

class NotifiedFilter : Filter() {
    override fun apply(baseDevices: List<BaseDevice>): List<BaseDevice> {
        return baseDevices.filter {
            it.notificationSent
        }
    }

    companion object {
        fun build(): Filter = NotifiedFilter()
    }
}
