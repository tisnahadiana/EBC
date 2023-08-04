package id.deeromptech.ebc.ui.shopping.ui.billing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Constants.ORDER_PLACED_STATE
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BillingViewModel  @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {
    val placeOrder = MutableLiveData<Resource<Order>>()
    val addresses = MutableLiveData<Resource<List<Address>>>()

    private val _address = MutableStateFlow<Resource<List<Address>>>(Resource.Unspecified())
    val address = _address.asStateFlow()

    init {
        getShippingAddresses()
    }

    private fun getShippingAddresses() {
        addresses.postValue(Resource.Loading())
        firebaseDatabase.getAddresses()?.addSnapshotListener { value, error ->
            if (error != null) {
                addresses.postValue(Resource.Error(error.toString()))
                return@addSnapshotListener
            }
            if (!value!!.isEmpty) {
                val addressesList = value.toObjects(Address::class.java)
                addresses.postValue(Resource.Success(addressesList))
            }
        }
    }

    fun placeOrder(products:List<Cart>,address: Address,price:String){
        placeOrder.postValue(Resource.Loading())
        val id = Random.nextInt(9999999)
        val date = Calendar.getInstance().time
        val order = Order(id.toString(),date,price,ORDER_PLACED_STATE)

        firebaseDatabase.placeOrder(products, address, order).addOnCompleteListener {
            if(it.isSuccessful)
                placeOrder.postValue(Resource.Success(order))
            else
                placeOrder.postValue(Resource.Error(it.exception.toString()))
        }
    }
}