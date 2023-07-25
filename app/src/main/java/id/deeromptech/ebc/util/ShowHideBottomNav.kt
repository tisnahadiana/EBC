package id.deeromptech.ebc.util

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.deeromptech.ebc.R
import id.deeromptech.ebc.ui.shopping.ShoppingActivity

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            R.id.bottom_nav_view
        )
    bottomNavigationView.visibility = View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            R.id.bottom_nav_view
        )
    bottomNavigationView.visibility = View.VISIBLE
}