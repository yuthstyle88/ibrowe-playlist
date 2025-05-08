package com.brave.braveandroidplaylist.extension

import android.content.res.Resources
import android.util.TypedValue

val Number.dpToPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )