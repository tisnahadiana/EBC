package id.deeromptech.ebc.ui.shopping.ui.seller.verification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentSellerVerificationBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SellerVerificationFragment : Fragment() {

    private var _binding: FragmentSellerVerificationBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<SellerVerificationViewModel>()
    private val args by navArgs<SellerVerificationFragmentArgs>()

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
                    is Resource.Loading -> binding.btnSaveSellerData.startAnimation()
                    is Resource.Success -> {
                        binding.btnSaveSellerData.revertAnimation()
                        ToastUtils.showMessage(requireContext(),"Data updated successfully!")
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        binding.btnSaveSellerData.revertAnimation()
                        ToastUtils.showMessage(requireContext(),"Data update failed")
                    }
                    else -> { }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.apply {
            btnSaveSellerData.setOnClickListener {
                saveSellerData()
//                saveSellerAddress()
            }
            btnEditInfoSeller.setOnClickListener {
                saveSellerData()
            }
        }

        if (args.edit){
            binding.apply {
                setUserInformation(args.user)
                btnSaveSellerData.visibility = View.INVISIBLE
                btnEditInfoSeller.visibility = View.VISIBLE
                tvTitle.setText(R.string.edit_seller_data)
            }
        }
    }

    private fun setUserInformation(user: User) {

        binding.apply {
            edRegisterNameStore.setText(user.storeName)
            edRegisterRekening.setText(user.rekening)
            edAddressStore.setText(user.addressStore)
        }
    }

    fun saveSellerData() {
        val nameStore = binding.edRegisterNameStore.text.toString()
        val rekening = binding.edRegisterRekening.text.toString()
        val address = binding.edAddressStore.text.toString()

        val user = User(
            "","","","","seller",
            addressStore = address,
            storeName = nameStore,
            rekening = rekening,
        )

        viewModel.updateUserStoreData(user)

    }

//    fun saveSellerAddress() {
//        binding.apply {
//            val addressTitle = edAddressTitle.text.toString()
//            val kampung = edKampung.text.toString()
//            val desa = edDesa.text.toString()
//            val kecamatan = edSubdistrict.text.toString()
//            val city = edCity.text.toString()
//            val state = edProvince.text.toString()
//
//            val newAddress = Address(
//                UUID.randomUUID().toString(),
//                addressTitle,
//                kampung,
//                desa,
//                kecamatan,
//                city,
//                state
//            )
//
//            viewModel.addNewAddress(newAddress)
//        }
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}