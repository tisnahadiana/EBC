package id.deeromptech.ebc.data.model.cost

import com.google.gson.annotations.SerializedName

data class CostResponse(
    @SerializedName("rajaongkir")
    val rajaOngkir: CostRajaOngkir? = null
)
