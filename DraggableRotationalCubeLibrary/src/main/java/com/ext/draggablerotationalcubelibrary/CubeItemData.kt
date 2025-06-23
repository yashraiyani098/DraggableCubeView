package com.ext.draggablerotationalcubelibrary

import android.os.Parcel
import android.os.Parcelable

data class CubeItemData(
    val header: String,
    val detected: String,
    val death: String
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(header)
        dest.writeString(detected)
        dest.writeString(death)
    }

    companion object CREATOR : Parcelable.Creator<CubeItemData> {
        override fun createFromParcel(source: Parcel): CubeItemData {
            return CubeItemData(
                header = source.readString() ?: "",
                detected = source.readString() ?: "",
                death = source.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<CubeItemData?> {
            return arrayOfNulls(size)
        }
    }
}
