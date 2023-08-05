package id.deeromptech.ebc.ui.auth.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDb // Inject FirebaseDb
) : ViewModel() {


    val saveUserInformationGoogleSignIn = MutableLiveData<Resource<String>>()
    val register = MutableLiveData<Resource<User>>()

    val login = MutableLiveData<Boolean>()
    val loginError = MutableLiveData<String>()

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    val resetPassword = MutableLiveData<Resource<String>>()

    fun registerNewUser(
        user: User,
        password: String
    ) {
        register.postValue(Resource.Loading())
        firebaseDatabase.createNewUser(user.email, password).addOnCompleteListener {
            if (it.isSuccessful)
                firebaseDatabase.saveUserInformation(Firebase.auth.currentUser!!.uid, user)
                    .addOnCompleteListener { it2 ->
                        if (it2.isSuccessful) {
                            register.postValue(Resource.Success(user))
                        } else
                            register.postValue(Resource.Error(it2.exception.toString()))

                    }
            else
                register.postValue(Resource.Error(it.exception.toString()))
        }
    }

    private fun saveUserInformationGoogleSignIn(
        userUid: String,
        user: User
    ) {
        firebaseDatabase.checkUserByEmail(user.email) { error, isAccountExisted ->
            if (error != null)
                saveUserInformationGoogleSignIn.postValue(Resource.Error(error))
            else
                if (isAccountExisted!!)
                    saveUserInformationGoogleSignIn.postValue(Resource.Success(userUid))
                else
                    firebaseDatabase.saveUserInformation(userUid, user).addOnCompleteListener {
                        if (it.isSuccessful)
                            saveUserInformationGoogleSignIn.postValue(Resource.Success(userUid))
                        else
                            saveUserInformationGoogleSignIn.postValue(Resource.Error(it.exception.toString()))
                    }
        }
    }

    fun loginUser(
        email: String,
        password: String
    ) = firebaseDatabase.loginUser(email, password).addOnCompleteListener {
        if (it.isSuccessful)
            login.postValue(true)
        else
            loginError.postValue(it.exception.toString())
    }

    fun resetPassword(email: String) {
        resetPassword.postValue(Resource.Loading())
        firebaseDatabase.resetPassword(email).addOnCompleteListener {
            if (it.isSuccessful)
                resetPassword.postValue(Resource.Success(email))
            else
                resetPassword.postValue(Resource.Error(it.exception.toString()))

        }
    }

    fun signInWithGoogle(idToken: String) {
        saveUserInformationGoogleSignIn.postValue(Resource.Loading())
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseDatabase.signInWithGoogle(credential).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val userFirebase = FirebaseAuth.getInstance().currentUser
                val fullNameArray = userFirebase!!.displayName?.split(" ")
                val firstName = fullNameArray!![0]
                val role = "user"

                val user = User(firstName,  userFirebase.email.toString(), "","",role)
                saveUserInformationGoogleSignIn(userFirebase.uid, user)
            } else
                saveUserInformationGoogleSignIn.postValue(Resource.Error(task.exception.toString()))


        }
    }

    fun logOut(){
        firebaseDatabase.logout()
    }

    fun isUserSignedIn() : Boolean {
        if (FirebaseAuth.getInstance().currentUser?.uid != null)
            return true
        return false

    }
}