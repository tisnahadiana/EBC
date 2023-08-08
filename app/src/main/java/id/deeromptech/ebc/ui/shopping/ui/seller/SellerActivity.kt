package id.deeromptech.ebc.ui.shopping.ui.seller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.ActivityInputProductBinding
import id.deeromptech.ebc.databinding.ActivitySellerBinding
import id.deeromptech.ebc.ui.shopping.ui.profile.ProfileFragment

class SellerActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySellerBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.imageButton.setOnClickListener {
            val intent = Intent(this, InputProductActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ProfileFragment::class.java)
        startActivity(intent)
    }
}