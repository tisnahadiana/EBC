package id.deeromptech.ebc.ui.shopping.ui.order

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
class AllOrdersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()
    val profile = MutableLiveData<Resource<User>>()

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


    fun getAllOrders(user: User){
        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
        }

        firestore.collection("orders")
            .whereEqualTo("userName", user.name).whereEqualTo("email",user.email).get()
            .addOnSuccessListener {
                val orders = it.toObjects(Order::class.java)
                viewModelScope.launch {
                    _allOrders.emit(Resource.Success(orders))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _allOrders.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}