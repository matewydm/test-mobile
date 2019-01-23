package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName
import pl.darenie.dna.model.enums.Priority
import java.io.Serializable

class Bill(var name: String,
           var payment: Double,
           var priority: Priority,
           var payers: List<UserDetailCash>,
           var chargers: List<UserDetailCash>) : Serializable{
    @SerializedName("cyclicBill") var cyclicBill: CyclicEntity? = null
    var id : Long? = null
}