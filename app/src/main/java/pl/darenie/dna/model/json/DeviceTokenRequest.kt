package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName

class DeviceTokenRequest(@SerializedName("deviceToken") val deviceToken: String) {
}