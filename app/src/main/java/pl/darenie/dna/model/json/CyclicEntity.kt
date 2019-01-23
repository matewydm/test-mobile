package pl.darenie.dna.model.json

import pl.darenie.dna.model.enums.CyclicType

class CyclicEntity(var type: CyclicType,
                   var day: Int,
                   var hour: Int,
                   var minute: Int)  {

    fun getTime() : String {
        val minuteString = if (minute == 0) {
            "00"
        } else {
            minute.toString()
        }
        return hour.toString().plus(":").plus(minuteString)
    }
}