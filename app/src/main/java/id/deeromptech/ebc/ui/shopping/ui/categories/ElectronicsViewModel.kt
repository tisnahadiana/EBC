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

private const val TAG = "ElectronicsViewModel"
class ElectronicsViewModel constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private var electronicsProducts: List<Product>? = null
    val electronics = MutableLiveData<Resource<List<Product>>>()
    private var mostRequestedelectronicsProducts: List<Product>? = null
    val mostElectronicsAccessories = MutableLiveData<Resource<List<Product>>>()

    private var mostRequestedElectronicsPage: Long = 3
    private var electronicsPage: Long = 4

    val fashion = MutableLiveData<Resource<List<Product>>>()

    fun getElecteronics(size: Int = 0) {
        if (electronicsProducts != null && size == 0) {
            electronics.postValue(Resource.Success(electronicsProducts!!))
            return
        }
        electronics.postValue(Resource.Loading())
        shouldPaging(Constants.ELECTRONICS_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                Log.d("test", "paging")
                firebaseDatabase.getProductsByCategory(
                    Constants.ELECTRONICS_CATEGORY,
                    electronicsPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                electronics.postValue(Resource.Success(productsList))
                                electronicsProducts = productsList
                                electronicsPage += 4

                            }
                        } else
                            electronics.postValue(Resource.Error(it.exception.toString()))
                    }
            } else {
                electronics.postValue(Resource.Error("Cannot page"))
            }
        }
    }

    fun getMostRequestedElectronics(size: Int = 0) {
        if (mostRequestedelectronicsProducts != null && size == 0) {
            mostElectronicsAccessories.postValue(Resource.Success(mostRequestedelectronicsProducts!!))
            return
        }
        mostElectronicsAccessories.postValue(Resource.Loading())
        shouldPaging(Constants.ELECTRONICS_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                fashion.postValue(Resource.Loading())
                firebaseDatabase.getProductsByCategory(
                    Constants.ELECTRONICS_CATEGORY,
                    mostRequestedElectronicsPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                mostElectronicsAccessories.postValue(Resource.Success(productsList))
                                mostRequestedelectronicsProducts = productsList
                                mostRequestedElectronicsPage += 4

                            }
                        } else
                            mostElectronicsAccessories.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                mostElectronicsAccessories.postValue(Resource.Error("Cannot paging"))
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