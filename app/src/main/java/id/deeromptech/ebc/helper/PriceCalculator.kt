package id.deeromptech.ebc.helper

fun Float?.getProductPrice(price: Float) : Float {
    //this --> Percentage
    if (this == null)
        return price
    val remainingPrivePercentage = 1f - this
    val priceAfterOffer = remainingPrivePercentage * price

    return priceAfterOffer
}