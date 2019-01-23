package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Friend(@SerializedName("firebaseToken") val firebaseToken: String,
             val email : String,
             val firstname: String,
             val lastname: String,
             @SerializedName("birthDate") val birthDate: String,
             @SerializedName("phoneNumber") val phoneNumber: String,
             @SerializedName("cyclicAccounting") var cyclicAccounting: CyclicEntity?,
             var balance : Balance) : Serializable{
    fun displayName() : String = firstname.plus(" ").plus(lastname)
}