package id.deeromptech.ebc.ui.shopping.ui.categories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Category
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Constants
import id.deeromptech.ebc.util.Resource
import javax.inject.Inject

private const val TAG = "FashionViewModel"
@HiltViewModel
class FashionViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private var fashionProducts: List<Product>? = null
    val fashion = MutableLiveData<Resource<List<Product>>>()
    private var mostRequestFashionProducts: List<Product>? = null
    val mostFashionAccessories = MutableLiveData<Resource<List<Product>>>()

    private var mostRequestedFashionsPage: Long = 3
    private var fashionPage: Long = 4

    fun getFashion(size: Int = 0) {
        if (fashionProducts != null && size == 0) {
            fashion.postValue(Resource.Success(fashionProducts!!))
            return
        }
        fashion.postValue(Resource.Loading())
        shouldPaging(Constants.FASHION_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                Log.d("test", "paging")
                firebaseDatabase.getProductsByCategory(
                    Constants.FASHION_CATEGORY,
                    fashionPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                fashion.postValue(Resource.Success(productsList))
                                fashionProducts = productsList
                                fashionPage += 4

                            }
                        } else
                            fashion.postValue(Resource.Error(it.exception.toString()))
                    }
            } else {
                fashion.postValue(Resource.Error("Cannot page"))
            }
        }
    }

    fun getMostRequestedFashion(size: Int = 0) {
        if (mostRequestFashionProducts != null && size == 0) {
            mostFashionAccessories.postValue(Resource.Success(mostRequestFashionProducts!!))
            return
        }
        mostFashionAccessories.postValue(Resource.Loading())
        shouldPaging(Constants.FASHION_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                fashion.postValue(Resource.Loading())
                firebaseDatabase.getProductsByCategory(
                    Constants.FASHION_CATEGORY,
                    mostRequestedFashionsPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                mostFashionAccessories.postValue(Resource.Success(productsList))
                                mostRequestFashionProducts = productsList
                                mostRequestedFashionsPage += 4

                            }
                        } else
                            mostFashionAccessories.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                mostFashionAccessories.postValue(Resource.Error("Cannot paging"))
        }
    }

    private fun shouldPaging(category: String, listSize: Int, onSuccess: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("categories")
            .whereEqualTo("name", category).get().addOnSuccessListener {
                val tempCategory = it.toObjects(Category::class.java)
                val products = tempCategory[0].products
                Log.d("test", " $category : prodcuts ${tempCategory[0].products}, size $listSize")
                if (listSize == products)
                    onSuccess(false).also { Log.d(TAG, "$category Paging:false") }
                else
                    onSuccess(true).also { Log.d(TAG, "$category Paging:true") }

            }
    }
}