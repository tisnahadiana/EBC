package id.deeromptech.ebc.data.local

data class User(
    val name: String,
    val email: String,
    val phone: String,
    val imagePath: String = ""
){
    constructor(): this("","", "", "")
}
