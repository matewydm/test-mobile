package pl.darenie.dna.model.json

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

class User(@SerializedName("firebaseToken") val firebaseToken: String,
                val email : String,
                val firstname: String,
                val lastname: String,
                @SerializedName("birthDate") val birthDate: String,
                @SerializedName("phoneNumber") val phoneNumber: String) : Serializable{

    @SerializedName("facebookId") var facebookId: String? = null
    val balance: Balance? = null
    fun displayName() : String = firstname.plus(" ").plus(lastname)
}