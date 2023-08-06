package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*
import kotlin.collections.HashMap


@Parcelize
data class Product(
    val id :String,
    val title: String? = "",
    val description: String? = "",
    val category: String? = "",
    val newPrice:String?="",
    val price: String? = "",
    val seller: String? = "",

    val images:@RawValue HashMap<String, Any>?=null,
    val stock: String,

) : Parcelable
{
    constructor(
        id: String? = "",
        title: String? = "",
        description: String? = "",
        category: String? = "",
        newPrice: String? = "",
        price: String? = "",
        seller: String? = "",
        images: HashMap<String, Any>
    ) : this("",title,description,category,newPrice,price,seller, images,"" )

    constructor():this("","","","","",null,null,null, "")
}

