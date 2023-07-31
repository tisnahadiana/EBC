package id.deeromptech.ebc.ui.shopping.ui.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addNewAddress = MutableStateFlow<Resource<Address>>(Resource.Unspecified())
    val addNewAddress = _addNewAddress.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _deleteAddress = MutableStateFlow<Resource<List<Address>>>(Resource.Unspecified())
    val deleteAddress = _deleteAddress.asStateFlow()

    private var addressDocuments = emptyList<DocumentSnapshot>()

    private val _deleteDialog = MutableSharedFlow<Cart>()
    val deleteDialog = _deleteDialog.asSharedFlow()
    fun deleteAddress(address: Address) {
        val index = deleteAddress.value.data?.indexOf(address)
        if (index != null && index != -1) {
            val documentId = addressDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("address")
                .document(documentId).delete()
        }
    }

    fun addAddress(address: Address) {
        val validateInputs = validateInputs(address)
        if (validateInputs){
            viewModelScope.launch { _addNewAddress.emit(Resource.Loading()) }
            firestore.collection("user").document(auth.uid!!).collection("address").document()
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



}