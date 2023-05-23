package com.ats.airflagger.data.filter.models

import com.ats.airflagger.data.model.BaseDevice

class IgnoredFilter : Filter() {
    override fun apply(baseDevices: List<BaseDevice>): List<BaseDevice> {
        return baseDevices.filter {
            it.ignore
        }
    }

    companion object {
        fun build(): Filter = IgnoredFilter()
    }
}