package id.deeromptech.ebc.ui.shopping.ui.shippingcost

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.model.ResultData
import id.deeromptech.ebc.data.model.city.CityResponse
import id.deeromptech.ebc.data.model.cost.CostResponse
import id.deeromptech.ebc.data.usecase.DataUseCase
import javax.inject.Inject

@HiltViewModel
class ShippingCostViewModel @Inject constructor(
    private val useCase: DataUseCase
) : ViewModel() {

    fun getCities(): LiveData<ResultData<CityResponse>> {
        return liveData {
            emit(ResultData.Loading())
            emit(useCase.getCities())
        }
    }

    fun getCost(
        origin: String,
        destination: String,
        weight: Int,
        courier: String
    ): LiveData<ResultData<CostResponse>> {
        return liveData {
            emit(ResultData.Loading())
            emit(useCase.getCost(origin, destination, weight, courier))
        }
    }
}