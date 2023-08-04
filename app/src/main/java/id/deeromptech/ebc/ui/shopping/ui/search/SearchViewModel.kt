package id.deeromptech.ebc.ui.shopping.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.Category
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel(){

    val categories = MutableLiveData<Resource<List<Category>>>()
    private var categoriesSafe: List<Category>? = null
    val search = MutableLiveData<Resource<List<Product>>>()

    fun getCategories() {
        if(categoriesSafe != null){
            categories.postValue(Resource.Success(categoriesSafe!!))
            return
        }
        categories.postValue(Resource.Loading())
        firebaseDatabase.getCategories().addOnCompleteListener {
            if (it.isSuccessful) {
                val categoriesList = it.result!!.toObjects(Category::class.java)
                categoriesSafe = categoriesList
                categories.postValue(Resource.Success(categoriesList))
            } else
                categories.postValue(Resource.Error(it.exception.toString()))
        }


    }

    fun searchProducts(searchQuery: String) {
        search.postValue(Resource.Loading())
        firebaseDatabase.searchProducts(searchQuery).addOnCompleteListener {
            if (it.isSuccessful) {
                val productsList = it.result!!.toObjects(Product::class.java)
                search.postValue(Resource.Success(productsList))

            } else
                search.postValue(Resource.Error(it.exception.toString()))

        }
    }
}