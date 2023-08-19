package id.deeromptech.ebc.data.network

import id.deeromptech.ebc.data.model.city.CityResponse
import id.deeromptech.ebc.data.model.cost.CostResponse
import retrofit2.http.*

interface ApiRajaOngkir {

    @GET("city")
    suspend fun getCities(
        @Query("key") apiKey: String?
    ): CityResponse

    @FormUrlEncoded
    @POST("cost")
    suspend fun getCost(
        @Header("key") apiKey: String?,
        @Field("origin") origin: String?,
        @Field("destination") destination: String?,
        @Field("weight") weight: Int?,
        @Field("courier") courier: String?
    ): CostResponse
}