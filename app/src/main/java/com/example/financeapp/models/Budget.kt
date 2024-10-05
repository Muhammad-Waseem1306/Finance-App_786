import android.os.Parcel
import android.os.Parcelable

data class Budget(
    var id: String,
    val amount: Double,
    val category: String,
    val startDate: String,
    val endDate: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeDouble(amount)
        parcel.writeString(category)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Budget> {
        override fun createFromParcel(parcel: Parcel): Budget {
            return Budget(parcel)
        }

        override fun newArray(size: Int): Array<Budget?> {
            return arrayOfNulls(size)
        }
    }
}
