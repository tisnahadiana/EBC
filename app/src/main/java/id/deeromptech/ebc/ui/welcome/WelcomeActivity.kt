package id.deeromptech.ebc.ui.welcome

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.databinding.ActivityWelcomeBinding
import id.deeromptech.ebc.ui.auth.login.LoginActivity

@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {

    private val binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }
    private val welcomeViewModel: WelcomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        binding.button.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
//            welcomeViewModel.setFirstTime(false)
            editor.putBoolean("is_first_time", false) // Set the "is_first_time" to false
            editor.apply() // Apply the changes
            startActivity(intent)
            finish()
        }

        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
//            welcomeViewModel.setFirstTime(false)
            editor.putBoolean("is_first_time", false) // Set the "is_first_time" to false
            editor.apply() // Apply the cha
            startActivity(intent)
            finish()
        }
    }
}