package id.deeromptech.ebc.data.repositories

import id.deeromptech.ebc.data.model.city.CityResponse
import id.deeromptech.ebc.data.model.cost.CostResponse
import id.deeromptech.ebc.data.network.ApiRajaOngkir
import id.deeromptech.ebc.util.APIKey.Companion.API_KEY
import javax.inject.Inject

class DataRepository @Inject constructor(
    private val apiRajaOngkir: ApiRajaOngkir
) {

    suspend fun getCities(): CityResponse {
        //insert your API Key
        return apiRajaOngkir.getCities(API_KEY)
    }

    suspend fun getCost(
        origin: String,
        destination: String,
        weight: Int,
        courier: String
    ): CostResponse {
        //insert your API Key
        return apiRajaOngkir.getCost(API_KEY, origin, destination, weight, courier)
    }
}