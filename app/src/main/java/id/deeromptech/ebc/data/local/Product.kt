package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val stock: String,
    val seller: String? = "",
    val images: List<String>,
    val addressStore: String? = ""
): Parcelable {
    constructor(): this("0", "","",0f,0f,"", "","", images = emptyList(), "")
}

