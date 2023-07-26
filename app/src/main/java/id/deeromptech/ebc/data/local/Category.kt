package id.deeromptech.ebc.data.local

sealed class Category(val  category: String){

    object Beauty: Category("Beauty")
    object Electronics: Category("Electronics")
    object Fashion: Category("Fashion")
    object Food: Category("Food")
    object Handycrafts: Category("Handycrafts")
    object Household: Category("Household")
}
