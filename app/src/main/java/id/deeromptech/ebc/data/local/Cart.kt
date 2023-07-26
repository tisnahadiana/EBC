package id.deeromptech.ebc.data.local

data class Cart(
    val product: Product,
    val quantity: Int
){
    constructor(): this(Product(), 1)
}
