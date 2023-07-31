package id.deeromptech.ebc.data.local

data class User(
    val name: String,
    val email: String,
    val phone: String,
    val imagePath: String = "",
    val role: String? = null,
    val addressUser: Address? = null,
    val storeName: String? = null,
    val addressStore: String? = null,
    val rekening: Int? = null
){
    constructor(): this("","", "", "","", Address(), "","",0)
}
