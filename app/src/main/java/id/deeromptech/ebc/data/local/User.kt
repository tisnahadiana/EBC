package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String,
    val email: String,
    val phone: String,
    var imagePath: String = "",
    val role: String? = null,
    val addressUser: Address? = null,
    val storeName: String? = null,
    val addressStore: String? = null,
    val rekening: Int? = null
): Parcelable {
    constructor(): this("","", "", "","", Address(), "","",0)
}
