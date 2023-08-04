package id.deeromptech.ebc.ui.shopping.ui.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllOrdersViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    val userOrders = MutableLiveData<Resource<List<Order>>>()

    fun getUserOrders() {
        userOrders.postValue(Resource.Loading())
        firebaseDatabase.getUserOrders().addOnCompleteListener {
            if (it.isSuccessful)
                userOrders.postValue(Resource.Success(it.result.toObjects(Order::class.java)))
            else
                userOrders.postValue(Resource.Error(it.exception.toString()))
        }
    }
}