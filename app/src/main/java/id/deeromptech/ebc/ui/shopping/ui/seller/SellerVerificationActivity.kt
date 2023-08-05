package id.deeromptech.ebc.ui.shopping.ui.seller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.ActivitySellerVerificationBinding
import id.deeromptech.ebc.firebase.FirebaseDb
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
@AndroidEntryPoint
class SellerVerificationActivity : AppCompatActivity() {

    private val binding: ActivitySellerVerificationBinding by lazy {
        ActivitySellerVerificationBinding.inflate(layoutInflater)
    }

    val viewModel by viewModels<SellerVerificationViewModel>()
    val db = FirebaseDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.updateUserStoreDataResult
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> binding.btnSaveSellerData.startAnimation() // Start the animation
                    is Resource.Success -> binding.btnSaveSellerData.revertAnimation() // Stop the animation on success
                    is Resource.Error -> binding.btnSaveSellerData.revertAnimation() // Stop the animation on error
                    else -> { } // Handle other resource states if needed
                }
            }
            .launchIn(lifecycleScope)

        binding.apply {
            btnSaveSellerData.setOnClickListener {
                saveSellerData()
                saveSellerAddress()
            }
        }

    }

    fun saveSellerData() {
        val nameStore = binding.edRegisterNameStore.text.toString()
        val rekening = binding.edRegisterRekening.text.toString()

        val user = User(
            "","","","","seller",Address(),
            storeName = nameStore,
            rekening = rekening,
        )

        viewModel.updateUserStoreData(user)

    }

    fun saveSellerAddress() {
        binding.apply {
            val addressTitle = edAddressTitle.text.toString()
            val kampung = edKampung.text.toString()
            val desa = edDesa.text.toString()
            val kecamatan = edSubdistrict.text.toString()
            val city = edCity.text.toString()
            val state = edProvince.text.toString()

            val newAddress = Address(
                UUID.randomUUID().toString(),
                addressTitle,
                kampung,
                desa,
                kecamatan,
                city,
                state
            )

            viewModel.addNewAddress(newAddress)
        }

    }

}