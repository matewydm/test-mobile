package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName
import pl.darenie.dna.model.enums.SettlementStatus

class SettlementStatusRequest(@SerializedName("settlementId") var settlementId: Long,
                              var status: SettlementStatus) {
}