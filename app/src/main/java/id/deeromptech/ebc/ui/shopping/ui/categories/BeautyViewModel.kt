package id.deeromptech.ebc.ui.shopping.ui.categories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Category
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Constants.BEAUTY_CATEGORY
import id.deeromptech.ebc.util.Resource
import javax.inject.Inject

private const val TAG = "BeautyViewModel"
@HiltViewModel
class BeautyViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel(){

    private var accessoriesProducts: List<Product>? = null
    val beauty = MutableLiveData<Resource<List<Product>>>()
    private var mostRequestedBeautyProducts: List<Product>? = null
    val mostBeautyAccessories = MutableLiveData<Resource<List<Product>>>()
    val fashion = MutableLiveData<Resource<List<Product>>>()

    private var mostRequestedAccessoryPage: Long = 3
    private var accessoryPage: Long = 4

    fun getAccessories(size: Int = 0) {
        if (accessoriesProducts != null && size == 0) {
            beauty.postValue(Resource.Success(accessoriesProducts!!))
            return
        }
        beauty.postValue(Resource.Loading())
        shouldPaging(BEAUTY_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                Log.d("test", "paging")
                firebaseDatabase.getProductsByCategory(BEAUTY_CATEGORY, accessoryPage)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                beauty.postValue(Resource.Success(productsList))
                                accessoriesProducts = productsList
                                accessoryPage += 4

                            }
                        } else
                            beauty.postValue(Resource.Error(it.exception.toString()))
                    }
            } else {
                beauty.postValue(Resource.Error("Cannot page"))
            }
        }
    }

    fun getMostRequestedAccessories(size: Int = 0) {
        if (mostRequestedBeautyProducts != null && size == 0) {
            mostBeautyAccessories.postValue(Resource.Success(mostRequestedBeautyProducts!!))
            return
        }
        mostBeautyAccessories.postValue(Resource.Loading())
        shouldPaging(BEAUTY_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                fashion.postValue(Resource.Loading())
                firebaseDatabase.getProductsByCategory(
                    BEAUTY_CATEGORY,
                    mostRequestedAccessoryPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                mostBeautyAccessories.postValue(Resource.Success(productsList))
                                mostRequestedBeautyProducts = productsList
                                mostRequestedAccessoryPage += 4

                            }
                        } else
                            mostBeautyAccessories.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                mostBeautyAccessories.postValue(Resource.Error("Cannot paging"))
        }
    }

    private fun shouldPaging(category: String, listSize: Int, onSuccess: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("categories")
            .whereEqualTo("name", category).get().addOnSuccessListener {
                val tempCategory = it.toObjects(Category::class.java)
                val products = tempCategory[0].products
                Log.d("test", " $category : products ${tempCategory[0].products}, size $listSize")
                if (listSize == products)
                    onSuccess(false).also { Log.d(TAG, "$category Paging:false") }
                else
                    onSuccess(true).also { Log.d(TAG, "$category Paging:true") }
            }
    }
}