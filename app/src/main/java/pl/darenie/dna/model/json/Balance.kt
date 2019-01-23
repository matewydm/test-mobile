package pl.darenie.dna.model.json

import java.io.Serializable

class Balance (var debt : Double,
               var due : Double,
               var balance: Double) : Serializable{
}