package id.deeromptech.ebc.data.local

data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<Cart>,
    val address: Address
)
