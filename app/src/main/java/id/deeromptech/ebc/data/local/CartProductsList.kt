package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class CartProductsList (
    val products: @RawValue List<Cart>
) : Parcelable {


}