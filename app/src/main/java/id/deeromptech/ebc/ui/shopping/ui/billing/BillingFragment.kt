package id.deeromptech.ebc.ui.shopping.ui.billing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.AddressAdapter
import id.deeromptech.ebc.adapter.BillingProductsAdapter
import id.deeromptech.ebc.data.local.*
import id.deeromptech.ebc.databinding.FragmentBillingBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.ui.shopping.ui.order.OrderViewModel
import id.deeromptech.ebc.util.HorizontalItemDecoration
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@AndroidEntryPoint
class BillingFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<Cart>()
    private var totalPrice = 0f
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    private var selectedAddress: Address? = null
    private val orderViewModel by viewModels<OrderViewModel> ()
    var user: User?=null

    val TAG = "BillingFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingViewModel.getUser()

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        observeAddress()
//        setupAddressRv()

        if (!args.payment){
            binding.apply {
                buttonPlaceOrder.visibility  = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        binding.imageAddAddress.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("user", user)
            }
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment, bundle)
        }



//        lifecycleScope.launchWhenStarted {
//            billingViewModel.address.collectLatest {
//                when (it) {
//                    is Resource.Loading -> {
//                        binding.progressbarAddress.visibility = View.VISIBLE
//                    }
//
//                    is Resource.Success -> {
//                        addressAdapter.differ.submitList(it.data)
//                        binding.progressbarAddress.visibility = View.GONE
//                    }
//
//                    is Resource.Error -> {
//                        binding.progressbarAddress.visibility = View.GONE
//                        ToastUtils.showMessage(requireContext(), it.message.toString())
//                    }
//                    else -> Unit
//                }
//            }
//        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        ToastUtils.showMessage(requireContext(), "Your order was placed")
                    }

                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        ToastUtils.showMessage(requireContext(), it.message.toString())
                    }
                    else -> Unit
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        val formattedPrice = "Rp. ${decimalFormat.format(totalPrice)}"
        binding.tvTotalPrice.text = formattedPrice

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment){
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (user?.addressUser == null) {
                ToastUtils.showMessage(requireContext(), "Please Select address")
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }
    private fun observeAddress() {
        billingViewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    hideLoading()
                    val user = response.data
                    this.user = user
                    binding.apply {
                        textShippingAddress.text = user?.addressUser
                    }
                    return@observe
                }

                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(
                        activity,
                        resources.getText(R.string.error_occurred),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, response.message.toString())
                    return@observe
                } else -> Unit
            }
        }
    }

    fun showLoading() {
        binding.progressbarAddress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.progressbarAddress.visibility = View.GONE
    }

    private fun showOrderConfirmationDialog() {
        val address = binding.textShippingAddress.text.toString().trim()
        val dialogResult = DialogResult(requireContext())
        dialogResult.setTitle("Order Items")
        dialogResult.setImage(R.drawable.ic_order)
        dialogResult.setMessage("Do you want to order your cart item?")
        dialogResult.setPositiveButton("Yes", onClickListener = {
            val order = Order(
                OrderStatus.Ordered.status,
                totalPrice,
                products,
                address,
                user!!.name,
                user!!.phone,
                user!!.email
            )
            orderViewModel.placeOrder(order)
            dialogResult.dismiss()
        })
        dialogResult.setNegativeButton("No", onClickListener = {
            dialogResult.dismiss()
        })
        dialogResult.show()
    }

//    private fun setupAddressRv() {
//        binding.rvAddress.apply {
//            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
//            adapter = addressAdapter
//            addItemDecoration(HorizontalItemDecoration())
//        }
//    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}