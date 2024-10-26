import android.os.Parcel
import android.os.Parcelable

data class Budget(
    var id: String = "",
    var date: String = 0L.toString(), // Assuming you want to keep date as String
    var limit: Double = 0.0,
    var category: String = "",
    var spent: String = "0.0",// Default value should also be a String
    var remaining: String = "0.0" // Default value should also be a String
) : Parcelable {
    // No-argument constructor needed for Firestore deserialization
    constructor() : this("", 0L.toString(), 0.0, "", "0.0", "0.0")

    // Parcelable implementation
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "0", // Default value for date
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?:"0.0",
        parcel.readString() ?: "0.0"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(date)
        parcel.writeDouble(limit)
        parcel.writeString(category)
        parcel.writeString(spent)
        parcel.writeString(remaining)
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