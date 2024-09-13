package com.example.payment_app.Models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.auth.PhoneAuthProvider

data class ForceResendingTokenParcelable(val token: PhoneAuthProvider.ForceResendingToken) : Parcelable {
    constructor(parcel: Parcel) : this(
        token = PhoneAuthProvider.ForceResendingToken.CREATOR.createFromParcel(parcel)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        token.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ForceResendingTokenParcelable> {
        override fun createFromParcel(parcel: Parcel): ForceResendingTokenParcelable {
            return ForceResendingTokenParcelable(parcel)
        }

        override fun newArray(size: Int): Array<ForceResendingTokenParcelable?> {
            return arrayOfNulls(size)
        }
    }

    fun toForceResendingToken(): PhoneAuthProvider.ForceResendingToken {
        return token
    }
}
