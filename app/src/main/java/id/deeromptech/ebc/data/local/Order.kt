package id.deeromptech.ebc.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random.Default.nextLong

@Parcelize
data class Order(
    val id: String,
    val date:Date,
    val totalPrice:String,
    val state:String
) :  Parcelable {
    constructor():this("",Date(),"","")
}
