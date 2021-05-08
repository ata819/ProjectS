package com.epsilon.projects.models

import android.os.Parcel
import android.os.Parcelable

data class Household (
    val name: String = "",
    val image: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentID: String = "",
    var choreList: ArrayList<Chore> = ArrayList()

): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.createStringArrayList()!!,
            parcel.readString()!!,
        parcel.createTypedArrayList(Chore.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(documentID)
        parcel.writeTypedList(choreList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Household> {
        override fun createFromParcel(parcel: Parcel): Household {
            return Household(parcel)
        }

        override fun newArray(size: Int): Array<Household?> {
            return arrayOfNulls(size)
        }
    }
}