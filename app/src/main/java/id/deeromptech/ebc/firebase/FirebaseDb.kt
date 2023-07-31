package id.deeromptech.ebc.firebase

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.util.Constants.STORES_COLLECTION
import id.deeromptech.ebc.util.Constants.USERS_COLLECTION

class FirebaseDb {
    private val usersCollectionRef = Firebase.firestore.collection(USERS_COLLECTION)
    private val storesCollection = Firebase.firestore.collection(STORES_COLLECTION)

    private val firebaseAuth = Firebase.auth
    fun createNewUser(
        email: String, password: String
    ) = firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun saveUserInformation(
        userUid: String,
        user: User
    ) = usersCollectionRef.document(userUid).set(user)

    fun loginUser(
        email: String,
        password: String
    ) = firebaseAuth.signInWithEmailAndPassword(email, password)

    //true -> already existed account
    //false -> new account
    fun checkUserByEmail(email: String, onResult: (String?, Boolean?) -> Unit) {
        usersCollectionRef.whereEqualTo("email", email).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result.toObjects(User::class.java)
                    if (user.isEmpty())
                        onResult(null, false)
                    else
                        onResult(null, true)
                } else
                    onResult(it.exception.toString(), null)
            }
    }

    fun resetPassword(email: String) = firebaseAuth.sendPasswordResetEmail(email)

    fun signInWithGoogle(credential: AuthCredential) =
        FirebaseAuth.getInstance().signInWithCredential(credential)

    fun logout() = Firebase.auth.signOut()
}