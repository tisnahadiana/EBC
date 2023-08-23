package id.deeromptech.ebc.ui.shopping.ui.address

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.data.model.ResultData
import id.deeromptech.ebc.data.model.city.CityResult
import id.deeromptech.ebc.databinding.FragmentAddressBinding
import id.deeromptech.ebc.ui.shopping.ui.shippingcost.ShippingCostViewModel
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.gone
import id.deeromptech.ebc.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AddressFragment: Fragment(){

    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<AddressViewModel>()
    private val args by navArgs<AddressFragmentArgs>()
    private var currentAddress: Address? = null
    private lateinit var address: String
    private lateinit var cityUser: String
    private lateinit var cityStore: String

    private val mainViewModel by viewModels<ShippingCostViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        address = args.user.addressUser.toString()


        loadCities()

        lifecycleScope.launchWhenStarted {
            viewModel.addNewAddress.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        findNavController().navigateUp()
                    }

                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), it.message.toString() )
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                ToastUtils.showMessage(requireContext(), it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.user.addressUser != null){
            binding.edAddressStore.setText(address)
        }


        if (args.user.role == "seller"){
            binding.destinationTIL.visibility = View.VISIBLE
            cityStore = args.user.cityStore
            binding.destinationAutoCompleteTV.setText(cityStore)
        } else {
            cityUser = args.user.cityUser
            binding.originAutoCompleteTV.setText(cityUser)
        }

        viewModel.updateUserStoreDataResult
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading()
                    is Resource.Success -> {
                        hideLoading()
                        ToastUtils.showMessage(requireContext(),"Data updated successfully!")
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        ToastUtils.showMessage(requireContext(),"Data update failed")
                    }
                    else -> { }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.buttonSave.setOnClickListener {
            saveUserAddress()
        }

        binding.imageAddressClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonDelete.setOnClickListener {
            currentAddress?.let { address ->
                val alertDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete item from cart")
                    setMessage("Do you want to delete this item from your cart?")
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton("Yes") { dialog, _ ->
                        viewModel.deleteAddress(address)
                        dialog.dismiss()
                    }
                }
                alertDialog.create()
                alertDialog.show()
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
        val courierAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, couriers)

        binding.destinationAutoCompleteTV.setAdapter(cityAdapter)
        binding.originAutoCompleteTV.setAdapter(cityAdapter)
    }

    private fun showErrorMessage(message: String) {
        binding.loadingPB.gone()
        ToastUtils.showMessage(requireContext(),getString(R.string.message_error, message))
    }

    private fun setUserInformation(user: String) {
        binding.edAddressStore.setText(user)
    }

    fun showLoading() {
        binding.progressbarAddress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.progressbarAddress.visibility = View.GONE
    }

    fun saveUserAddress() {
        val address = binding.edAddressStore.text.toString()
        val cityUser = binding.originAutoCompleteTV.text.toString()
        val cityStore = binding.destinationAutoCompleteTV.text.toString()

        val user = User(
            "","","","","seller",
            addressUser = address,
            cityStore = cityStore,
            cityUser = cityUser
        )

        viewModel.updateUserStoreData(user)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}