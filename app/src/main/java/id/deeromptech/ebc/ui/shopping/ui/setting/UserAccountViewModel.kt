package id.deeromptech.ebc.ui.shopping.ui.setting

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb,
    app: Application
) : AndroidViewModel(app) {

    val uploadProfileImage = MutableLiveData<Resource<String>>()
    val updateUserInformation = MutableLiveData<Resource<User>>()
    val passwordReset = MutableLiveData<Resource<String>>()

    fun uploadProfileImage(image: ByteArray) {
        uploadProfileImage.postValue(Resource.Loading())
        val name = UUID.nameUUIDFromBytes(image).toString()
        firebaseDatabase.uploadUserProfileImage(image, name).addOnCompleteListener {
            if (it.isSuccessful)
                uploadProfileImage.postValue(Resource.Success(name))
            else
                uploadProfileImage.postValue(Resource.Error(it.exception.toString()))
        }
    }

    fun updateInformation(name: String, email: String, phone: String, imageName: String) {
        updateUserInformation.postValue(Resource.Loading())

        firebaseDatabase.getImageUrl(name, email, phone, imageName) { user, exception ->

            if (exception != null)
                updateUserInformation.postValue(Resource.Error(exception))
                    .also { Log.d("test1", "up") }
            else
                user?.let {
                    onUpdateInformation(user).also { Log.d("test1", "down") }
                }
        }
    }

    private fun onUpdateInformation(user: User) {
        firebaseDatabase.updateUserInformation(user).addOnCompleteListener {
            if (it.isSuccessful)
                updateUserInformation.postValue(Resource.Success(user))
            else
                updateUserInformation.postValue(Resource.Error(it.exception.toString()))

        }
    }

    fun resetPassword(email: String) {
        passwordReset.postValue(Resource.Loading())
        firebaseDatabase.resetPassword(email).addOnCompleteListener {
            if (it.isSuccessful)
                passwordReset.postValue(Resource.Success(email))
            else
                passwordReset.postValue(Resource.Error(it.exception.toString()))
        }
    }

}