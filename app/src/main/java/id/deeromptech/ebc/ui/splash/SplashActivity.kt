package id.deeromptech.ebc.ui.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val SPLASH_DELAY_MS = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            initialCheck()
        }, SPLASH_DELAY_MS)

    }

    private fun initialCheck() {
        val isFirstTime = sharedPreferences.getBoolean("is_first_time", true)

        if (isFirstTime) {
            startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
        } else {
            val currentUser = auth.currentUser
            val targetActivity = if (currentUser != null) {
                ShoppingActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, targetActivity))
        }

        finish()
    }
}