package id.deeromptech.ebc.ui.shopping.ui.seller.verification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentSellerVerificationBinding
import id.deeromptech.ebc.util.Resource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

@AndroidEntryPoint
class SellerVerificationFragment : Fragment() {

    private var _binding: FragmentSellerVerificationBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<SellerVerificationViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerVerificationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            "","","","","seller", Address(),
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}