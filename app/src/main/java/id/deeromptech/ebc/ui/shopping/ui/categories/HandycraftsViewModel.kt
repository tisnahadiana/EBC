package id.deeromptech.ebc.ui.shopping.ui.categories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import id.deeromptech.ebc.data.local.Category
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Constants
import id.deeromptech.ebc.util.Resource

private const val TAG = "HandycraftsViewModel"
class HandycraftsViewModel constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private var handycraftsProducts: List<Product>? = null
    val handycrafts = MutableLiveData<Resource<List<Product>>>()
    private var mostRequestedHandycraftsProducts: List<Product>? = null
    val mostHandycraftsAccessories = MutableLiveData<Resource<List<Product>>>()

    private var mostRequestedHandycraftsPage: Long = 3
    private var handycraftsPage: Long = 4

    fun getHandycrafts(size: Int = 0) {
        if (handycraftsProducts != null && size == 0) {
            handycrafts.postValue(Resource.Success(handycraftsProducts!!))
            return
        }
        handycrafts.postValue(Resource.Loading())
        shouldPaging(Constants.HANDYCRAFTS_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                Log.d("test", "paging")
                firebaseDatabase.getProductsByCategory(
                    Constants.HANDYCRAFTS_CATEGORY,
                    handycraftsPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                handycrafts.postValue(Resource.Success(productsList))
                                handycraftsProducts = productsList
                                handycraftsPage += 4

                            }
                        } else
                            handycrafts.postValue(Resource.Error(it.exception.toString()))
                    }
            } else {
                handycrafts.postValue(Resource.Error("Cannot page"))
            }
        }
    }

    fun getMostRequestedHandycrafts(size: Int = 0) {
        if (mostRequestedHandycraftsProducts != null && size == 0) {
            mostHandycraftsAccessories.postValue(Resource.Success(mostRequestedHandycraftsProducts!!))
            return
        }
        mostHandycraftsAccessories.postValue(Resource.Loading())
        shouldPaging(Constants.HANDYCRAFTS_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                handycrafts.postValue(Resource.Loading())
                firebaseDatabase.getProductsByCategory(
                    Constants.HANDYCRAFTS_CATEGORY,
                    mostRequestedHandycraftsPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                mostHandycraftsAccessories.postValue(Resource.Success(productsList))
                                mostRequestedHandycraftsProducts = productsList
                                mostRequestedHandycraftsPage += 4

                            }
                        } else
                            mostHandycraftsAccessories.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                mostHandycraftsAccessories.postValue(Resource.Error("Cannot paging"))
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