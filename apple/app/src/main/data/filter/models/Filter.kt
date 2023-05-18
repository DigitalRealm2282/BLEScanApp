package com.ats.apple.data.filter.models

import com.ats.apple.data.model.BaseDevice


abstract class Filter {
    abstract fun apply(baseDevices: List<BaseDevice>): List<BaseDevice>
}