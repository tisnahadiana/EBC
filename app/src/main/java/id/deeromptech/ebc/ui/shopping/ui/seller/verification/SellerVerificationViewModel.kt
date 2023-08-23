package id.deeromptech.ebc.ui.shopping.ui.seller.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerVerificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addNewAddress = MutableStateFlow<Resource<Address>>(Resource.Unspecified())
    val addNewAddress = _addNewAddress.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _updateUserStoreDataResult = MutableStateFlow<Resource<Unit>>(Resource.Unspecified())
    val updateUserStoreDataResult = _updateUserStoreDataResult.asStateFlow()


    fun addAddress(address: Address) {
        val validateInputs = validateInputs(address)
        if (validateInputs){
            viewModelScope.launch { _addNewAddress.emit(Resource.Loading()) }
            firestore.collection("user").document(auth.uid!!).collection("addressStore").document()
                .set(address).addOnSuccessListener {
                    viewModelScope.launch { _addNewAddress.emit(Resource.Success(address)) }
                }.addOnFailureListener {
                    viewModelScope.launch { _addNewAddress.emit(Resource.Error(it.message.toString())) }
                }
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }

    private fun validateInputs(address: Address): Boolean {
        return address.addressTitle.trim().isNotEmpty() &&
                address.kampung.trim().isNotEmpty() &&
                address.desa.trim().isNotEmpty() &&
                address.kecamatan.trim().isNotEmpty() &&
                address.city.trim().isNotEmpty() &&
                address.provinsi.trim().isNotEmpty()
    }

    fun addNewAddress(address: Address) {
        val validateInputs = validateInputs(address)
        if (validateInputs) {
            viewModelScope.launch { _addNewAddress.emit(Resource.Loading()) }
            firestore.collection("users").document(auth.currentUser?.uid ?: "")
                .collection("addressStore").document()
                .set(address)
                .addOnSuccessListener {
                    viewModelScope.launch { _addNewAddress.emit(Resource.Success(address)) }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _addNewAddress.emit(Resource.Error(exception.message ?: ""))
                    }
                }
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }

    fun updateUserStoreData(user: User) {
        viewModelScope.launch {
            _updateUserStoreDataResult.emit(Resource.Loading()) // Start the loading animation
        }

        firestore.collection("users").document(auth.currentUser?.uid ?: "")
            .update(
                "storeName", user.storeName,
                "rekening", user.rekening,
                "role", user.role,
                "addressStore", user.addressStore,
                "cityStore", user.cityStore
            )
            .addOnSuccessListener {
                viewModelScope.launch {
                    _updateUserStoreDataResult.emit(Resource.Success(Unit)) // Success, stop the animation
                }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch {
                    _updateUserStoreDataResult.emit(Resource.Error(exception.message ?: "")) // Error, stop the animation
                }
            }
    }
}