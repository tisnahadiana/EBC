package id.deeromptech.ebc.data.local

sealed class CategoryList(val  category: String){

    object Beauty: CategoryList("Beauty")
    object Electronics: CategoryList("Electronics")
    object Fashion: CategoryList("Fashion")
    object Food: CategoryList("Food")
    object Handycrafts: CategoryList("Handycrafts")
    object Household: CategoryList("Household")
}
