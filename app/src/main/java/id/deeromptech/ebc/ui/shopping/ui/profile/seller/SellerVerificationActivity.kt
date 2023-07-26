package id.deeromptech.ebc.ui.shopping.ui.profile.seller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.deeromptech.ebc.databinding.ActivitySellerVerificationBinding

class SellerVerificationActivity : AppCompatActivity() {

    private val binding: ActivitySellerVerificationBinding by lazy {
        ActivitySellerVerificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}