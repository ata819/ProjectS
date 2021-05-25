package com.epsilon.projects.models

import android.os.Parcel
import android.os.Parcelable

data class Card (
        val name: String = "",
        val createdBy: String = "",
        val assignedTo: ArrayList<String> = ArrayList(),
        val dueDate: Long = 0
        ) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString()!!,
            source.readString()!!,
            source.createStringArrayList()!!,
            source.readLong()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeLong(dueDate)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }
    }
}