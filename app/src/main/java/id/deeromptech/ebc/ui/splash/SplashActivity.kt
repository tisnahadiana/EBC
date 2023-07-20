package id.deeromptech.ebc.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.ui.auth.login.LoginActivity
import id.deeromptech.ebc.ui.shopping.ShoppingActivity
import id.deeromptech.ebc.ui.welcome.WelcomeActivity

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    private val SPLASH_DELAY_MS = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            initialCheck()
        }, SPLASH_DELAY_MS)

    }

    private fun initialCheck() {
        sessionViewModel.checkIfFirstTime().observe(this) { isFirstTime ->
            if (isFirstTime) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    startActivity(Intent(this, ShoppingActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
        }
    }
}