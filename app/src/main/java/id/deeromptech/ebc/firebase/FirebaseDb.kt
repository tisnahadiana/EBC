package id.deeromptech.ebc.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.util.Constants.ADDRESS_COLLECTION
import id.deeromptech.ebc.util.Constants.CART_COLLECTION
import id.deeromptech.ebc.util.Constants.CATEGORIES_COLLECTION
import id.deeromptech.ebc.util.Constants.CATEGORY
import id.deeromptech.ebc.util.Constants.FASHION
import id.deeromptech.ebc.util.Constants.ORDERS
import id.deeromptech.ebc.util.Constants.PRODUCTS_COLLECTION
import id.deeromptech.ebc.util.Constants.QUANTITY
import id.deeromptech.ebc.util.Constants.STORES_COLLECTION
import id.deeromptech.ebc.util.Constants.USERS_COLLECTION

class FirebaseDb {
    private val usersCollectionRef = Firebase.firestore.collection(USERS_COLLECTION)
    private val storesCollection = Firebase.firestore.collection(STORES_COLLECTION)
    private val productsCollection = Firebase.firestore.collection(PRODUCTS_COLLECTION)
    private val categoriesCollection = Firebase.firestore.collection(CATEGORIES_COLLECTION)

    private val firebaseAuth = Firebase.auth
    private val firebaseStorage = Firebase.storage.reference

    val userUid = FirebaseAuth.getInstance().currentUser?.uid
    private val userCartCollection = userUid?.let {
        Firebase.firestore.collection(USERS_COLLECTION).document(it).collection(CART_COLLECTION)
    }
    private val userAddressesCollection = userUid?.let {
        Firebase.firestore.collection(USERS_COLLECTION).document(it).collection(ADDRESS_COLLECTION)

    }


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

    fun getUser() = usersCollectionRef
        .document(FirebaseAuth.getInstance().currentUser!!.uid)
    fun getUserAddressStore() = usersCollectionRef
        .document(FirebaseAuth.getInstance().currentUser!!.uid).collection("addressStore")

    fun getItemsInCart() = userCartCollection!!

//    fun getProductInCart(product: Cart) = userCartCollection!!
//        .whereEqualTo(ID, product.id).get()

    fun increaseProductQuantity(documentId: String): Task<Transaction> {
        val document = userCartCollection!!.document(documentId)
        return Firebase.firestore.runTransaction { transaction ->
            val productBefore = transaction.get(document)
            var quantity = productBefore.getLong(QUANTITY)
            quantity = quantity!! + 1
            transaction.update(document, QUANTITY, quantity)
        }

    }

    fun decreaseProductQuantity(documentId: String): Task<Transaction> {
        val document = userCartCollection!!.document(documentId)
        return Firebase.firestore.runTransaction { transaction ->
            val productBefore = transaction.get(document)
            var quantity = productBefore.getLong(QUANTITY)
            quantity = if (quantity!!.toInt() == 1)
                1
            else
                quantity - 1
            transaction.update(document, QUANTITY, quantity)

        }

    }

    fun deleteProductFromCart(documentId: String) =
        userCartCollection!!.document(documentId).delete()

//    fun getProductFromCartProduct(cartProduct: Cart) =
//        productsCollection.whereEqualTo(ID, cartProduct.id).get()

    fun uploadUserProfileImage(image: ByteArray, imageName: String): UploadTask {
        val imageRef = firebaseStorage.child("profileImages")
            .child(firebaseAuth.currentUser!!.uid)
            .child(imageName)
        return imageRef.putBytes(image)
    }

    fun getImageUrl(
        name: String,
        email: String,
        phone: String,
        imageName: String,
        role: String,
        addressUser: String,
        storeName: String,
        addressStore: String,
        rekening : String,
        cityUser : String,
        cityStore: String,
        onResult: (User?, String?) -> Unit,
    ) {
        if (imageName.isNotEmpty())
            firebaseStorage.child("profileImages")
                .child(firebaseAuth.currentUser!!.uid)
                .child(imageName).downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val imageUrl = it.result.toString()
                        val user = User(name, email, phone, imageUrl,role,addressUser,storeName,addressStore,rekening, cityUser, cityStore)
                        onResult(user, null)
                    } else
                        onResult(null, it.exception.toString())

                } else {
            val user = User(name, email, phone, "",role, addressUser, storeName, addressStore, rekening, cityUser, cityStore)
            onResult(user, null)
        }
    }

    fun updateUserInformation(user: User) =
        Firebase.firestore.runTransaction { transaction ->
            val userPath = usersCollectionRef.document(Firebase.auth.currentUser!!.uid)
            if (user.imagePath.isNotEmpty()) {
                transaction.set(userPath, user)
            } else {
                val imagePath = transaction.get(userPath)["imagePath"] as String
                user.imagePath = imagePath
                transaction.set(userPath, user)
            }

        }


    fun addProductToCart(product: Cart) = userCartCollection?.document()!!.set(product)

    fun getClothesProducts(pagingPage: Long) =
        productsCollection.whereEqualTo(CATEGORY, FASHION).limit(pagingPage).get()

    fun getBestDealsProducts(pagingPage: Long) =
//        productsCollection.whereEqualTo(CATEGORY, BEST_DEALS).limit(pagingPage).get()
        productsCollection.whereEqualTo(CATEGORY, FASHION).limit(pagingPage).get()

    fun getHomeProducts(pagingPage: Long) =
        productsCollection.limit(pagingPage).get()

    fun getProductsByCategory(category: String,page:Long) =
        productsCollection.whereEqualTo(CATEGORY,category).limit(page).get()

    fun getAddresses() = userAddressesCollection

//    fun placeOrder(products: List<Cart>, address: Address, order: Order) =
//        Firebase.firestore.runBatch { batch ->
//            //filter every product to its store
//            /**
//             * create a map of products that has the size of stores list,
//            the map has stores name as keys
//             */
//
//            val stores = ArrayList<String>()
//            products.forEach { cartProduct ->
//                if (!stores.contains(cartProduct.store)) {
//                    stores.add(cartProduct.store)
//                }
//            }
//
//            val productsMap = HashMap<String, ArrayList<Cart>>(stores.size)
//            stores.forEach { storeName ->
//                val tempList = ArrayList<Cart>()
//                products.forEach { cartProduct ->
//                    if (cartProduct.store == storeName)
//                        tempList.add(cartProduct)
//                    productsMap[storeName] = tempList
//                }
//            }
//
//
//            /**
//            // Adding order,address and products to each store
//             */
//            productsMap.forEach {
//                val store = it.key
//                val orderProducts = it.value
//                val orderNum = order.id
//                var price = 0
//
//                orderProducts.forEach { it2 ->
//                    if (it2.newPrice != null && it2.newPrice.isNotEmpty()) {
//                        price += it2.newPrice.toInt() * it2.quantity
//                    } else
//                        price += it2.price.toInt() * it2.quantity
//                }
//
//                Log.d("test", "$store $price")
//
//                val storeOrder = Order(
//                    orderNum.toString(),
//                    Calendar.getInstance().time,
//                    price.toString(),
//                    ORDER_PLACED_STATE
//                )
//
//                val storeDocument = storesCollection
//                    .document(store)
//                    .collection("orders")
//                    .document()
//
//                batch.set(storeDocument, storeOrder)
//
//                val storeOrderAddress = storeDocument.collection(ADDRESS_COLLECTION).document()
//                batch.set(storeOrderAddress, address)
//
//
//                orderProducts.forEach {
//                    val storeOrderProducts =
//                        storeDocument.collection(PRODUCTS_COLLECTION).document()
//                    batch.set(storeOrderProducts, it)
//                }
//
//
//            }
//
//            /**
//            // Adding order,address and products to the user
//             */
//            val userOrderDocument =
//                usersCollectionRef.document(FirebaseAuth.getInstance().currentUser!!.uid)
//                    .collection("orders").document()
//            batch.set(userOrderDocument, order)
//
//            products.forEach {
//                val userProductDocument =
//                    userOrderDocument.collection(PRODUCTS_COLLECTION).document()
//                batch.set(userProductDocument, it)
//            }
//
//            val userAddressDocument = userOrderDocument.collection(ADDRESS_COLLECTION).document()
//
//            batch.set(userAddressDocument, address)
//
//        }.also {
//            deleteCartItems()
//        }

    private fun deleteCartItems() {
        userCartCollection?.get()?.addOnSuccessListener {
            Firebase.firestore.runBatch { batch ->
                it.documents.forEach {
                    val document = userCartCollection.document(it.id)
                    batch.delete(document)
                }
            }
        }
    }

    fun getUserOrders() = usersCollectionRef
        .document(FirebaseAuth.getInstance().currentUser!!.uid)
        .collection(ORDERS)
        .orderBy("date", Query.Direction.DESCENDING)
        .get()

//    fun getOrderAddressAndProducts(
//        order: Order,
//        address: (Address?, String?) -> Unit,
//        products: (List<Cart>?, String?) -> Unit
//    ) {
//        usersCollectionRef
//            .document(Firebase.auth.currentUser!!.uid).collection(ORDERS)
//            .whereEqualTo("id", order.id)
//            .get().addOnCompleteListener {
//                if (it.isSuccessful) {
//                    val id = it.result?.documents?.get(0)?.id
//                    usersCollectionRef.document(Firebase.auth.currentUser!!.uid)
//                        .collection(ORDERS).document(id!!).collection(ADDRESS_COLLECTION).get()
//                        .addOnCompleteListener { it2 ->
//                            if (it2.isSuccessful) {
//                                val address2 = it2.result?.toObjects(Address::class.java)
//                                Log.d("test", address2!!.size.toString())
//                                address(address2?.get(0), null)
//                            } else
//                                address(null, it2.exception.toString())
//                        }
//
//                    usersCollectionRef.document(Firebase.auth.currentUser!!.uid)
//                        .collection(ORDERS).document(id).collection(PRODUCTS_COLLECTION).get()
//                        .addOnCompleteListener { it2 ->
//                            if (it2.isSuccessful) {
//                                val products2 = it2.result?.toObjects(Cart::class.java)
//                                Log.d("test", products2!!.size.toString())
//                                products(products2, null)
//                            } else
//                                products(null, it2.exception.toString())
//                        }
//
//
//                } else {
//                    address(null, it.exception.toString())
//                    products(null, it.exception.toString())
//                }
//            }
//    }

    fun getCategories() = categoriesCollection.orderBy("rank").get()

    fun searchProducts(searchQuery: String) = productsCollection
        .orderBy("name")
        .startAt(searchQuery)
        .endAt("\u03A9+$searchQuery")
        .limit(5)
        .get()

}