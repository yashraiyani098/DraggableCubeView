package com.ext.draggablerotationalcubelibrary

import android.os.Parcel
import android.os.Parcelable

data class CubeItemData(
    val header: String,
    val headerVisible: Boolean,
    val image: Any?,  // Can be either String (URL) or Int (drawable resource),
    val imageVisible: Boolean,
    val description: String,
    val descriptionVisible: Boolean,

) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(header)
        dest.writeByte(if (headerVisible) 1 else 0)
        // Write image type
        when (image) {
            is String -> {
                dest.writeString("String")
                dest.writeString(image as String)
            }
            is Int -> {
                dest.writeString("Int")
                dest.writeInt(image as Int)
            }
            else -> {
                dest.writeString("")
            }
        }
        dest.writeByte(if (imageVisible) 1 else 0)
        dest.writeString(description)
        dest.writeByte(if (descriptionVisible) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<CubeItemData> {
        override fun createFromParcel(source: Parcel): CubeItemData {
            return CubeItemData(
                header = source.readString() ?: "",
                headerVisible = source.readByte() != 0.toByte(),
                image = when (val type = source.readString() ?: "") {
                    "String" -> source.readString()
                    "Int" -> source.readInt()
                    else -> null
                },
                imageVisible = source.readByte() != 0.toByte(),
                description = source.readString() ?: "",
                descriptionVisible = source.readByte() != 0.toByte()
            )
        }

        override fun newArray(size: Int): Array<CubeItemData?> {
            return arrayOfNulls(size)
        }
    }
}
