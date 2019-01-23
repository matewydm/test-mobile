package pl.darenie.dna.callback

import pl.darenie.dna.model.json.SettlementStatusRequest

interface SettlementCallback {

    fun call(settlementStatusRequest: SettlementStatusRequest)

    fun notify(settlementStatusRequest: SettlementStatusRequest)

}