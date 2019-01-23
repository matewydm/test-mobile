package pl.darenie.dna.model.json

import com.google.gson.annotations.SerializedName

class DeviceRefreshRequest(@SerializedName("oldDeviceToken") val oldDeviceToken: String,
                           @SerializedName("newDeviceToken") val newDeviceToken: String) {
}