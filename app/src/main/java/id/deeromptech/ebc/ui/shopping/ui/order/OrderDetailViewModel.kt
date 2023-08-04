package id.deeromptech.ebc.ui.shopping.ui.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    val orderAddress = MutableLiveData<Resource<Address>>()
    val orderProducts = MutableLiveData<Resource<List<Cart>>>()

    fun getOrderAddressAndProducts(order: Order) {
        orderAddress.postValue(Resource.Loading())
        orderProducts.postValue(Resource.Loading())
        firebaseDatabase.getOrderAddressAndProducts(order, { address, aError ->
            if (aError != null)
                orderAddress.postValue(Resource.Error(aError))
            else
                orderAddress.postValue(Resource.Success(address!!))
        }, { products, pError ->

            if (pError != null)
                orderProducts.postValue(Resource.Error(pError))
            else
                orderProducts.postValue(Resource.Success(products!!))

        })
    }
}