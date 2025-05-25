package com.devrinth.launchpad.adapters

import android.graphics.drawable.Drawable

data class PinnedActionAdapter(
    var action: String,
    var uri: String,
    var image: Drawable?,
)
