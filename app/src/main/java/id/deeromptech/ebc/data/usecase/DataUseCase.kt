package id.deeromptech.ebc.data.usecase

import id.deeromptech.ebc.data.model.ResultData
import id.deeromptech.ebc.data.model.city.CityResponse
import id.deeromptech.ebc.data.model.cost.CostResponse
import id.deeromptech.ebc.data.repositories.DataRepository
import javax.inject.Inject

class DataUseCase @Inject constructor(
    private val dataRepository: DataRepository
) {

    companion object {
        const val STATUS_OK = 200
    }

    suspend fun getCities(): ResultData<CityResponse> {
        return try {
            val cityResponse = dataRepository.getCities()
            if (cityResponse.rajaOngkir?.status?.code == STATUS_OK) {
                ResultData.Success(cityResponse)
            } else {
                ResultData.Failed(cityResponse.rajaOngkir?.status?.description)
            }
        } catch (e: Exception) {
            ResultData.Exception(e.message)
        }
    }

    suspend fun getCost(
        origin: String,
        destination: String,
        weight: Int,
        courier: String
    ): ResultData<CostResponse> {
        return try {
            val costResponse = dataRepository.getCost(origin, destination, weight, courier)
            if (costResponse.rajaOngkir?.status?.code == STATUS_OK) {
                ResultData.Success(costResponse)
            } else {
                ResultData.Failed(costResponse.rajaOngkir?.status?.description)
            }
        } catch (e: Exception) {
            ResultData.Exception(e.message)
        }
    }
}