package id.deeromptech.ebc.ui.shopping.ui.categories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.Category
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import javax.inject.Inject

private const val TAG = "ShoppingViewModel"
@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel () {

    val clothes = MutableLiveData<List<Product>>()
    val emptyClothes = MutableLiveData<Boolean>()
    val bestDeals = MutableLiveData<List<Product>>()
    val emptyBestDeals = MutableLiveData<Boolean>()
    val home = MutableLiveData<Resource<List<Product>>>()
    val addToCart = MutableLiveData<Resource<Boolean>>()

    private var clothesPaging: Long = 5
    private var bestDealsPaging: Long = 5
    private var homePage: Long = 10
    fun getClothesProducts() =
        firebaseDatabase.getClothesProducts(clothesPaging).addOnCompleteListener {
            if (it.isSuccessful) {
                val documents = it.result
                if (!documents!!.isEmpty) {
                    val productsList = documents.toObjects(Product::class.java)
                    clothes.postValue(productsList)
                    clothesPaging += 5
                } else
                    emptyClothes.postValue(true)

            } else
                Log.e(TAG, it.exception.toString())

        }

    fun getBestDealsProduct() =
        firebaseDatabase.getBestDealsProducts(bestDealsPaging).addOnCompleteListener {
            if (it.isSuccessful) {
                val documents = it.result
                if (!documents!!.isEmpty) {
                    val productsList = documents.toObjects(Product::class.java)
                    bestDeals.postValue(productsList)
                    bestDealsPaging += 5
                } else
                    emptyBestDeals.postValue(true)

            } else
                Log.e(TAG, it.exception.toString())
        }

    fun getHomeProduct(size: Int = 0) {
        home.postValue(Resource.Loading())
        shouldPagingHome(size)
        { shouldPaging ->
            if (shouldPaging) {
                home.postValue(Resource.Loading())
                firebaseDatabase.getHomeProducts(homePage)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                home.postValue(Resource.Success(productsList))
                                homePage += 4

                            }
                        } else
                            home.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                home.postValue(Resource.Error("Cannot paging"))
        }
    }

    private fun shouldPagingHome(listSize: Int, onSuccess: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("products").get().addOnSuccessListener {
                var productsCount = 0
                it.toObjects(Category::class.java).forEach { category ->
                    productsCount += category.products!!.toInt()
                }

                if (listSize == productsCount)
                    onSuccess(false)
                else
                    onSuccess(true)

            }
    }

    fun addProductToCart(product: Cart) =
        checkIfProductAlreadyAdded(product) { isAdded, id ->
            if (isAdded) {
                firebaseDatabase.increaseProductQuantity(id).addOnCompleteListener {
                    if (it.isSuccessful)
                        addToCart.postValue(Resource.Success(true))
                    else
                        addToCart.postValue(Resource.Error(it.exception!!.message!!))

                }
            } else {
                firebaseDatabase.addProductToCart(product).addOnCompleteListener {
                    if (it.isSuccessful)
                        addToCart.postValue(Resource.Success(true))
                    else
                        addToCart.postValue(Resource.Error(it.exception!!.message!!))
                }
            }
        }

    private fun checkIfProductAlreadyAdded(
        product: Cart,
        onSuccess: (Boolean, String) -> Unit
    ) {
        addToCart.postValue(Resource.Loading())
        firebaseDatabase.getProductInCart(product).addOnCompleteListener {
            if (it.isSuccessful) {
                val documents = it.result!!.documents
                if (documents.isNotEmpty())
                    onSuccess(true, documents[0].id) // true ---> product is already in cart
                else
                    onSuccess(false, "") // false ---> product is not in cart
            } else
                addToCart.postValue(Resource.Error(it.exception.toString()))

        }
    }
}