package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressStore(
    val id: String,
    val addressTitle: String,
    val kampung: String,
    val desa: String,
    val kecamatan: String,
    val city: String,
    val provinsi: String
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "")
}

