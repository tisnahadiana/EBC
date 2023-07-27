package id.deeromptech.ebc.data.local

sealed class OrderStatus(val status: String) {

    object Ordered: OrderStatus("Ordered")
    object Canceled: OrderStatus("Canceled")
    object Confirmed: OrderStatus("Confirmed")
    object Shipped: OrderStatus("Shipped")
    object Delivered: OrderStatus("Delivered")
    object Returned: OrderStatus("Returned")

}

fun getOrderStatus(status: String): OrderStatus {
    return when (status) {
        "Ordered" -> {
            OrderStatus.Ordered
        }
        "Canceled" -> {
            OrderStatus.Ordered
        }
        "Confirmed" -> {
            OrderStatus.Ordered
        }
        "Shipped" -> {
            OrderStatus.Ordered
        }
        "Delivered" -> {
            OrderStatus.Ordered
        }
        else -> OrderStatus.Returned
    }
}
