package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

class UserDetailCash (@SerializedName("firebaseToken") val firebaseToken: String,
                      val firstname: String,
                      val lastname: String,
                      var cash: Double
                   ) : Serializable {

}