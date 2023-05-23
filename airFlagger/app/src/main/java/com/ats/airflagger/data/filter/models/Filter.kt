package com.ats.airflagger.data.filter.models

import com.ats.airflagger.data.model.BaseDevice


abstract class Filter {
    abstract fun apply(baseDevices: List<BaseDevice>): List<BaseDevice>
}