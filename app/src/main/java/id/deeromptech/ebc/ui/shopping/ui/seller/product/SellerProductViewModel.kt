package id.deeromptech.ebc.ui.shopping.ui.seller.product

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerProductViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    val profile = MutableLiveData<Resource<User>>()

    private val _sellerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val sellerProducts: StateFlow<Resource<List<Product>>> = _sellerProducts

    private val _deleteDialog = MutableSharedFlow<Product>()
    val deleteDialog = _deleteDialog.asSharedFlow()

    private val _cartProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val cartProducts = _cartProducts.asStateFlow()

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

    fun fetchSellerProducts(user: User) {
        viewModelScope.launch {
            _sellerProducts.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereEqualTo("seller", user.storeName).get().addOnSuccessListener { result ->
                val bestDealsProducts = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _sellerProducts.emit(Resource.Success(bestDealsProducts))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _sellerProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun deleteSellerProduct(product: Product) {
        viewModelScope.launch {
            try {
                _sellerProducts.emit(Resource.Loading())
                firestore.collection("Products")
                    .whereEqualTo("id", product.id)
                    .whereEqualTo("seller", product.seller)
                    .get()
                    .addOnSuccessListener { result ->
                        val documents = result.documents
                        if (!documents.isEmpty()) {
                            val document = documents[0]
                            document.reference.delete()
                            fetchSellerProducts(profile.value?.data ?: User())
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            _sellerProducts.emit(Resource.Error(it.message.toString()))
                        }
                    }
            } catch (e: Exception) {
                _sellerProducts.emit(Resource.Error(e.message.toString()))
            }
        }
    }

}