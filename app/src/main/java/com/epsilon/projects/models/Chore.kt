package com.epsilon.projects.models

import android.os.Parcel
import android.os.Parcelable

data class Chore (
    var title: String = "",
    val createdBy: String = "",
    val cards: ArrayList<Card> = ArrayList()

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
            parcel.createTypedArrayList(Card.CREATOR)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int)= with(dest) {
       writeString(title)
        writeString(createdBy)
        writeTypedList(cards)
    }

    companion object CREATOR : Parcelable.Creator<Chore> {
        override fun createFromParcel(parcel: Parcel): Chore {
            return Chore(parcel)
        }

        override fun newArray(size: Int): Array<Chore?> {
            return arrayOfNulls(size)
        }
    }

}
