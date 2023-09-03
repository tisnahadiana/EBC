package id.deeromptech.ebc.ui.shopping.ui.seller.verification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.data.model.ResultData
import id.deeromptech.ebc.data.model.city.CityResult
import id.deeromptech.ebc.databinding.FragmentSellerVerificationBinding
import id.deeromptech.ebc.ui.shopping.ui.shippingcost.ShippingCostViewModel
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.gone
import id.deeromptech.ebc.util.visible
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SellerVerificationFragment : Fragment() {

    private var _binding: FragmentSellerVerificationBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<SellerVerificationViewModel>()
    private val args by navArgs<SellerVerificationFragmentArgs>()
    private val mainViewModel by viewModels<ShippingCostViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadCities()
    }

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
                        ToastUtils.showMessage(requireContext(), "Data updated successfully!")
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        binding.btnSaveSellerData.revertAnimation()
                        ToastUtils.showMessage(requireContext(), "Data update failed")
                    }
                    else -> {}
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

        if (args.edit) {
            binding.apply {
                setUserInformation(args.user)
                btnSaveSellerData.visibility = View.INVISIBLE
                btnEditInfoSeller.visibility = View.VISIBLE
                tvTitle.setText(R.string.edit_seller_data)
            }
        }
    }

    private fun loadCities() {
        val listCity = mainViewModel.getCities()
        listCity.observe(this) {
            when (it) {
                is ResultData.Success -> {
                    binding.loadingPB.gone()
                    initSpinner(it.data?.rajaOngkir?.results ?: return@observe)

                }
                is ResultData.Loading -> binding.loadingPB.visible()
                is ResultData.Failed -> showErrorMessage(it.message.toString())
                is ResultData.Exception -> showErrorMessage(it.message.toString())
            }
        }
    }

    private fun initSpinner(cityResults: List<CityResult?>) {
        val cities = mutableListOf<String>()
        val couriers = resources.getStringArray(R.array.list_courier)
        for (i in cityResults.indices) cities.add(cityResults[i]?.cityName ?: "")
        val cityAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, cities)
        val courierAdapter =
            ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, couriers)

        binding.originAutoCompleteTV.setAdapter(cityAdapter)
    }

    private fun showErrorMessage(message: String) {
        binding.loadingPB.gone()
        ToastUtils.showMessage(requireContext(), getString(R.string.message_error, message))
    }

    private fun setUserInformation(user: User) {

        binding.apply {
            edRegisterNameStore.setText(user.storeName)
            edRegisterRekening.setText(user.rekening)
            edAddressStore.setText(user.addressStore)
            originAutoCompleteTV.setText(user.cityStore)
        }
    }

    fun saveSellerData() {
        val nameStore = binding.edRegisterNameStore.text.toString()
        val rekening = binding.edRegisterRekening.text.toString()
        val address = binding.edAddressStore.text.toString()
        val cityStore = binding.originAutoCompleteTV.text.toString()

        if (nameStore.isBlank()) {
            ToastUtils.showMessage(requireContext(), getString(R.string.messsage_nameStore_blank))
            return
        }

        if (rekening.isBlank()) {
            ToastUtils.showMessage(requireContext(), getString(R.string.message_rekening_blank))
            return
        }

        if (address.isBlank()) {
            ToastUtils.showMessage(requireContext(), getString(R.string.message_addressStore_blank))
            return
        }

        if (cityStore.isBlank()) {
            ToastUtils.showMessage(requireContext(), getString(R.string.message_cityStore_blank))
            return
        }


        val user = User(
            "", "", "", "", "seller",
            addressStore = address,
            storeName = nameStore,
            rekening = rekening,
            cityStore = cityStore
        )

        viewModel.updateUserStoreData(user)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}