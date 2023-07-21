package id.deeromptech.ebc.data.local

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val stock: String,
    val detail: String,
    val images: List<String>
) {
    constructor(): this("0", "","",0f,0f,"", "","", images = emptyList())
}
