package id.deeromptech.ebc.ui.shopping.ui.seller.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerOrderDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    fun placeOrder(order: Order) {
//        viewModelScope.launch {
//            _order.emit(Resource.Loading())
//        }
//        firestore.runBatch { batch ->
//            firestore.collection("orders").add(order)
//
//        }.addOnSuccessListener {
//            viewModelScope.launch {
//                _order.emit(Resource.Success(order))
//            }
//        }.addOnFailureListener {
//            viewModelScope.launch {
//                _order.emit(Resource.Error(it.message.toString()))
//            }
//        }

        viewModelScope.launch {
            try {
                _order.emit(Resource.Loading())
                firestore.collection("orders")
                    .whereEqualTo("orderId", order.orderId)
                    .whereEqualTo("userName", order.userName)
                    .get()
                    .addOnSuccessListener { result ->
                        val documents = result.documents
                        if (!documents.isEmpty()) {
                            val document = documents[0]
                            document.reference.delete()
                            document.reference.set(order)
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            _order.emit(Resource.Error(it.message.toString()))
                        }
                    }
            } catch (e: Exception) {
                _order.emit(Resource.Error(e.message.toString()))
            }
        }
    }
}


