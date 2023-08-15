package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random.Default.nextLong

@Parcelize
data class Order(
    val orderStatus: String = "",
    val totalPrice: Float = 0f,
    val products: List<Cart> = emptyList(),
    val address: String,
    val userName: String,
    val userPhone: String,
    val email: String,
    val date: String = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0,100_000_000_000) + totalPrice.toLong()
) :  Parcelable {
    constructor(): this("",0f, emptyList(),"","","","","",0L)
}
