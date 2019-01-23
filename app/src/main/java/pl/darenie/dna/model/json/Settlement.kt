package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName

class Settlement (var id: Long,
                  @SerializedName("billId") var billId: Long,
                  var payer: User,
                  var charger: User,
                  var charge:  Double,
                  var status: String){
}