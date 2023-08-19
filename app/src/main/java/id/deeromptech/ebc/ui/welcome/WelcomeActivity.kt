package id.deeromptech.ebc.ui.welcome

import android.content.Intent
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

        binding.button.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            welcomeViewModel.setFirstTime(false)
            startActivity(intent)
            finish()
        }

        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            welcomeViewModel.setFirstTime(false)
            startActivity(intent)
            finish()
        }
    }
}