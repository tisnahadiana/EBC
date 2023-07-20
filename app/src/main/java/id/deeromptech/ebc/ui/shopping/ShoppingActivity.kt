package id.deeromptech.ebc.ui.shopping

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.ActivityShoppingBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.ui.auth.login.LoginActivity

class ShoppingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShoppingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setLogo(R.drawable.logo_ebc_actionbar)
        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))

        auth = Firebase.auth
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                signOut()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        val navView: BottomNavigationView = binding.navView
        navView.itemBackground = getIndicatorDrawable()
        navView.setOnNavigationItemSelectedListener { item ->
            navView.itemBackground = getIndicatorDrawable(item.itemId)
            true
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_shopping)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_search ,R.id.navigation_dashboard, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun getIndicatorDrawable(): ShapeDrawable {
        val ovalShape = OvalShape()
        val shapeDrawable = ShapeDrawable(ovalShape)
        shapeDrawable.paint.color = getColor(R.color.green) // Set indicator color
        return shapeDrawable
    }

    // Method to get the custom indicator drawable for a specific item
    private fun getIndicatorDrawable(itemId: Int): ShapeDrawable {
        // You can customize the indicator drawable for each item here
        // For example, you can use different colors for different items
        // or have a different indicator shape for specific items.
        // For this example, we use the same indicator for all items.
        return getIndicatorDrawable()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        val dialogResult: DialogResult = DialogResult(this@ShoppingActivity)
        dialogResult.setTitle("Logout")
        dialogResult.setImage(R.drawable.logout)
        dialogResult.setMessage("Apakah anda yakin ingin logout?")
        dialogResult.setPositiveButton("Ya", onClickListener = {
            auth.signOut()
            googleSignInClient.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        })
        dialogResult.setNegativeButton("Tidak", onClickListener = {
            dialogResult.dismiss()
        })
        dialogResult.show()


    }
}