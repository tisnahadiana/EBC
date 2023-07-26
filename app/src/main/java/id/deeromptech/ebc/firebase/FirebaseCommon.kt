package id.deeromptech.ebc.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.deeromptech.ebc.data.local.Cart

class FirebaseCommon(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val cartCollection = firestore.collection("user").document(auth.uid!!).collection("cart")

    fun addProductToCart(cart: Cart, onResult: (Cart?, Exception?) -> Unit) {
        cartCollection.document().set(cart)
            .addOnSuccessListener {
                onResult(cart, null)
            }.addOnFailureListener {
                onResult(null,it)
            }
    }

    fun increaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        firestore.runTransaction { transition ->
            val documentRef = cartCollection.document(documentId)
            val document = transition.get(documentRef)
            val productObject = document.toObject(Cart::class.java)
            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity +1
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transition.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }
    }
}