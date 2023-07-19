package id.deeromptech.ebc.ui.shopping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.ActivityMainBinding
import id.deeromptech.ebc.ui.auth.login.LoginActivity
import id.deeromptech.ebc.util.DialogResult

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.setIcon(R.drawable.logo_ebc)

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
        val dialogResult:DialogResult = DialogResult(this@MainActivity)
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