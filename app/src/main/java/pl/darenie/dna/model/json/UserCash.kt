package pl.darenie.dna.model.json

import android.widget.EditText
import com.google.gson.annotations.SerializedName

class UserCash(@SerializedName("firebaseToken") val firebaseToken: String,
               var cash: Double) {
}