package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    val id:Int,
    val product: Product,
    val quantity: Int,
    val newPrice:String?,
): Parcelable {
    constructor(): this(0,Product(), 1,"")
}
