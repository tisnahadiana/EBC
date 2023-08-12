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
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentAddressBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        address = args.user.addressUser.toString()

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

        val user = User(
            "","","","","seller",
            addressUser = address
        )

        viewModel.updateUserStoreData(user)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}