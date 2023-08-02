package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class Cart(
    val id:Int,
    val name:String,
    val store:String,
    val image:String,
    val price:String,
    val newPrice:String?,
    val quantity:Int,
) {
    constructor() : this(0,"","","","","",0 )
}
