package pl.darenie.dna.rest

import pl.darenie.dna.model.json.*
import retrofit2.Call
import retrofit2.http.*

interface RestProvider {

//    USERS

    @GET("user")
    fun getUser(): Call<User>

    @POST("user")
    fun updateUser(@Body user: User): Call<Void>

    @POST("user/fb_register")
    fun fbRegister(@Body user: User): Call<Void>

    @POST("user/register")
    fun register(@Body userRegistrationRequest: UserRegistrationRequest): Call<Void>

    @GET("user/all")
    fun getAllUsers() : Call<List<User>>

    @POST("user/device/pair")
    fun pairDevice(@Body deviceTokenRequest: DeviceTokenRequest) : Call<Void>

    @POST("user/device/separate")
    fun separateDevice(@Body deviceTokenRequest: DeviceTokenRequest) : Call<Void>

    @POST("user/device/refresh")
    fun refreshDevice(@Body deviceRefreshRequest: DeviceRefreshRequest) : Call<Void>

    @GET("user/friends")
    fun getUserFriends() : Call<List<User>>

    @GET("user/friend/{id}")
    fun getFriend(@Path("id") id: String) : Call<Friend>

    @POST("user/friend")
    fun updateFriend(@Body friend: Friend) : Call<Void>

    @GET("user/foreigners")
    fun getUserForeigners() : Call<List<User>>

    @GET("user/checks")
    fun getUserChecks() : Call<List<User>>

    @GET("user/invitations")
    fun getInvitations(): Call<List<User>>

    @POST("user/invite")
    fun inviteUsers(@Body invitationRequest: FriendInvitationRequest) : Call<Void>

    @POST("user/invitation/accept")
    fun acceptInvitation(@Body invitationRequest: UserIdDTO) : Call<Void>

    @POST("user/invitation/reject")
    fun rejectInvitation(@Body invitationRequest: UserIdDTO) : Call<Void>

//    BILLS

    @POST("bill")
    fun addBill(@Body bill: Bill): Call<Void>

    @GET("bill")
    fun getOwnedBills(): Call<List<Bill>>

    @GET("bill/history")
    fun getBillHistory(): Call<List<Bill>>

    @GET("settlement/awaiting")
    fun getAwaitingSettlements(): Call<List<Settlement>>

    @GET("settlement/unpaid")
    fun getUnpaidSettlements(): Call<List<Settlement>>

    @GET("settlement/dues")
    fun getDues(): Call<List<Settlement>>

    @POST("settlement")
    fun postSettlement(@Body settlementStatusRequest: SettlementStatusRequest): Call<Void>

    @POST("settlement/notification")
    fun notifySettlement(@Body settlementStatusRequest: SettlementStatusRequest): Call<Void>

}