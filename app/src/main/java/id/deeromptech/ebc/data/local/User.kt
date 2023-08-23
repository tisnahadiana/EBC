package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    var imagePath: String = "",
    val role: String = "",
    val addressUser: String = "",
    val cityUser : String = "",
    val storeName: String = "",
    val addressStore: String = "",
    val cityStore: String = "",
    val rekening: String = ""
): Parcelable {
    constructor(): this("","", "", "","", "", "","","")
}
