package id.deeromptech.ebc.ui.shopping.ui.address

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.databinding.FragmentAddressBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@AndroidEntryPoint
class AddressFragment: Fragment(){

    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<AddressViewModel>()
    val args by navArgs<AddressFragmentArgs>()
    private var currentAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentAddress = args.address

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

        val address = args.address
        if (address == null) {
            binding.buttonDelete.visibility = View.GONE
        } else {
            binding.apply {
                edAddressTitle.setText(address.addressTitle)
                edKampung.setText(address.kampung)
                edDesa.setText(address.desa)
                edSubdistrict.setText(address.kecamatan)
                edCity.setText(address.city)
                edProvince.setText(address.provinsi)
            }
        }

        binding.apply {
            val address = args.address

            // Hide the delete button if no Address is received (for add new address)
            if (address == null) {
                buttonDelete.visibility = View.GONE
            } else {
                // Populate the fields with the existing Address data
                edAddressTitle.setText(address.addressTitle)
                edKampung.setText(address.kampung)
                edDesa.setText(address.desa)
                edSubdistrict.setText(address.kecamatan)
                edCity.setText(address.city)
                edProvince.setText(address.provinsi)
            }

            buttonSave.setOnClickListener {
                val addressTitle = edAddressTitle.text.toString()
                val kampung = edKampung.text.toString()
                val desa = edDesa.text.toString()
                val kecamatan = edSubdistrict.text.toString()
                val city = edCity.text.toString()
                val state = edProvince.text.toString()

                val newAddress = if (address == null) {
                    // Create a new Address object if no existing address is available
                    Address(
                        UUID.randomUUID().toString(),
                        addressTitle,
                        kampung,
                        desa,
                        kecamatan,
                        city,
                        state
                    )
                } else {
                    // Update the existing Address object with the same ID
                    address.copy(
                        addressTitle = addressTitle,
                        kampung = kampung,
                        desa = desa,
                        kecamatan = kecamatan,
                        city = city,
                        provinsi = state
                    )
                }

                viewModel.addAddress(newAddress)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}