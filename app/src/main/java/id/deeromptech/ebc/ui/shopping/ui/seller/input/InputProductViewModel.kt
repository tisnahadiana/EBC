package id.deeromptech.ebc.ui.shopping.ui.seller.input

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import javax.inject.Inject

@HiltViewModel
class InputProductViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    val profile = MutableLiveData<Resource<User>>()

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

}