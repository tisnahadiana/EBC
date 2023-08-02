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

private const val TAG = "HouseholdViewModel"
class HouseholdViewModel constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private var householdProducts: List<Product>? = null
    val household = MutableLiveData<Resource<List<Product>>>()
    private var mostRequestedHouseholdProducts: List<Product>? = null
    val mostHouseholdAccessories = MutableLiveData<Resource<List<Product>>>()

    private var mostRequestedHouseholdPage: Long = 3
    private var householdPage: Long = 4

    fun getHousehold(size: Int = 0) {
        if (householdProducts != null && size == 0) {
            household.postValue(Resource.Success(householdProducts!!))
            return
        }
        household.postValue(Resource.Loading())
        shouldPaging(Constants.HOUSEHOLD_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                Log.d("test", "paging")
                firebaseDatabase.getProductsByCategory(
                    Constants.HOUSEHOLD_CATEGORY,
                    householdPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                household.postValue(Resource.Success(productsList))
                                householdProducts = productsList
                                householdPage += 4

                            }
                        } else
                            household.postValue(Resource.Error(it.exception.toString()))
                    }
            } else {
                household.postValue(Resource.Error("Cannot page"))
            }
        }
    }

    fun getMostRequestedHousehold(size: Int = 0) {
        if (mostRequestedHouseholdProducts != null && size == 0) {
            mostHouseholdAccessories.postValue(Resource.Success(mostRequestedHouseholdProducts!!))
            return
        }
        mostHouseholdAccessories.postValue(Resource.Loading())
        shouldPaging(Constants.HOUSEHOLD_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                household.postValue(Resource.Loading())
                firebaseDatabase.getProductsByCategory(
                    Constants.HOUSEHOLD_CATEGORY,
                    mostRequestedHouseholdPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                mostHouseholdAccessories.postValue(Resource.Success(productsList))
                                mostRequestedHouseholdProducts = productsList
                                mostRequestedHouseholdPage += 4

                            }
                        } else
                            mostHouseholdAccessories.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                mostHouseholdAccessories.postValue(Resource.Error("Cannot paging"))
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