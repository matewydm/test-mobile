package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName
import java.util.*

class UserRegistrationRequest(val email : String,
                              val password : String,
                              val firstname: String,
                              val lastname: String,
                              @SerializedName("birthDate") val birthDate: String,
                              @SerializedName("phoneNumber") val phoneNumber: String) {

}