package com.devrinth.launchpad.search.external

import android.os.Parcel
import android.os.Parcelable

data class PluginResponse(
    var query: String,
    var value: String,
    var extra: String?,
    var action1: String,
    var imageUrl: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(query)
        parcel.writeString(value)
        parcel.writeString(extra)
        parcel.writeString(action1)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PluginResponse> {
        override fun createFromParcel(parcel: Parcel): PluginResponse {
            return PluginResponse(parcel)
        }

        override fun newArray(size: Int): Array<PluginResponse?> {
            return arrayOfNulls(size)
        }
    }
}
