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

private const val TAG = "FoodViewModel"
class FoodViewModel constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private var foodProducts: List<Product>? = null
    val food = MutableLiveData<Resource<List<Product>>>()
    private var mostRequestedFoodProducts: List<Product>? = null
    val mostFoodsAccessories = MutableLiveData<Resource<List<Product>>>()

    private var mostRequestedFoodPage: Long = 3
    private var foodPage: Long = 4


    fun getFood(size: Int = 0) {
        if (foodProducts != null && size == 0) {
            food.postValue(Resource.Success(foodProducts!!))
            return
        }
        food.postValue(Resource.Loading())
        shouldPaging(Constants.FOOD_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                Log.d("test", "paging")
                firebaseDatabase.getProductsByCategory(
                    Constants.FOOD_CATEGORY,
                    foodPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                food.postValue(Resource.Success(productsList))
                                foodProducts = productsList
                                foodPage += 4

                            }
                        } else
                            food.postValue(Resource.Error(it.exception.toString()))
                    }
            } else {
                food.postValue(Resource.Error("Cannot page"))
            }
        }
    }

    fun getMostRequestedFood(size: Int = 0) {
        if (mostRequestedFoodProducts != null && size == 0) {
            mostFoodsAccessories.postValue(Resource.Success(mostRequestedFoodProducts!!))
            return
        }
        mostFoodsAccessories.postValue(Resource.Loading())
        shouldPaging(Constants.FOOD_CATEGORY, size) { shouldPaging ->
            if (shouldPaging) {
                food.postValue(Resource.Loading())
                firebaseDatabase.getProductsByCategory(
                    Constants.FOOD_CATEGORY,
                    mostRequestedFoodPage
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documents = it.result
                            if (!documents!!.isEmpty) {
                                val productsList = documents.toObjects(Product::class.java)
                                mostFoodsAccessories.postValue(Resource.Success(productsList))
                                mostRequestedFoodProducts = productsList
                                mostRequestedFoodPage += 4

                            }
                        } else
                            mostFoodsAccessories.postValue(Resource.Error(it.exception.toString()))
                    }
            } else
                mostFoodsAccessories.postValue(Resource.Error("Cannot paging"))
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