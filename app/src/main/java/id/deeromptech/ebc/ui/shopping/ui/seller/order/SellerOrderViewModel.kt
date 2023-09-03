package id.deeromptech.ebc.ui.shopping.ui.seller.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerOrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    val profile = MutableLiveData<Resource<User>>()

    fun getAllOrders(user: String){
        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
        }
            try {
                val ordersList = mutableListOf<Order>()

                firestore.collection("orders").get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot) {
                            val order = document.toObject(Order::class.java)
                            // Check if the order has a product with the specified seller
                            val hasMatchingProduct = order.products.any { product ->
                                product.product.seller == user
                            }
                            if (hasMatchingProduct) {
                                ordersList.add(order)
                            }
                        }
                        viewModelScope.launch {
                            _allOrders.emit(Resource.Success(ordersList))
                        }
                    }
                    .addOnFailureListener { exception ->
                        viewModelScope.launch {
                            _allOrders.emit(Resource.Error(exception.message.toString()))
                        }
                    }
            } catch (exception: Exception) {
                viewModelScope.launch {
                    _allOrders.emit(Resource.Error(exception.message.toString()))
                }
            }
    }

    fun getUser() {
        profile.postValue(Resource.Loading())
        firebaseDatabase.getUser().addSnapshotListener { value, error ->
            if (error != null) {
                profile.postValue(Resource.Error(error.message ?: "Unknown error occurred"))
            } else {
                val user = value?.toObject(User::class.java) ?: User()
                profile.postValue(Resource.Success(user))
            }
        }
    }
}