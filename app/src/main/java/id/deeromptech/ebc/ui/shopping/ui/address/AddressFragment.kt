package id.deeromptech.ebc.ui.shopping.ui.address

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
import id.deeromptech.ebc.databinding.FragmentProductDetailBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddressFragment: Fragment(){

    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<AddressViewModel>()
    val args by navArgs<AddressFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                edFullName.setText(address.fullname)
                edStreet.setText(address.street)
                edPhone.setText(address.phone)
                edCity.setText(address.city)
                edState.setText(address.state)
            }
        }

        binding.apply {
            buttonSave.setOnClickListener {
                val addressTitle = edAddressTitle.text.toString()
                val fullname = edFullName.text.toString()
                val street = edStreet.text.toString()
                val phone = edPhone.text.toString()
                val city = edCity.text.toString()
                val state = edState.text.toString()
                val address = Address(addressTitle, fullname, street, phone, city, state)

                viewModel.addAddress(address)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}