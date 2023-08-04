package id.deeromptech.ebc.ui.shopping.ui.billing

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.SpacingDecorator.HorizantalSpacingItemDecorator
import id.deeromptech.ebc.adapter.AddressAdapter
import id.deeromptech.ebc.adapter.BillingProductsAdapter
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.databinding.FragmentBillingBinding
import id.deeromptech.ebc.util.Constants.ORDER_FAILED_FLAG
import id.deeromptech.ebc.util.Constants.ORDER_SUCCESS_FLAG
import id.deeromptech.ebc.util.Constants.UPDATE_ADDRESS_FLAG
import id.deeromptech.ebc.util.Resource
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@AndroidEntryPoint
class BillingFragment : Fragment() {

    private val args by navArgs<BillingFragmentArgs>()
    val TAG = "BillingFragment"
    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!

    private lateinit var shippingAddressesAdapter : AddressAdapter
    private val cartProductsAdapter by lazy { BillingProductsAdapter() }
    private val viewModel by viewModels<BillingViewModel>()


    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bottomNavigation = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigation?.visibility = View.INVISIBLE
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

        val price = args.price

        if (price == "null") {
            binding.apply {
                linear.visibility = View.GONE
                btnPlaceOlder.visibility = View.GONE
                line2.visibility = View.GONE
                rvProducts.visibility = View.GONE
                line3.visibility = View.GONE
            }
        } else {
            binding.apply {
                linear.visibility = View.VISIBLE
                btnPlaceOlder.visibility = View.VISIBLE
                line2.visibility = View.VISIBLE
                btnPlaceOlder.visibility = View.VISIBLE
                val formattedTvPrice = "Rp. ${decimalFormat.format(price)}"
                tvTotalprice.text = formattedTvPrice
                rvProducts.visibility = View.VISIBLE
                setupProductsRecyclerview()
                cartProductsAdapter.differ.submitList(args.products?.products)
            }
        }

        onAddAddressImgClick()
        onImgCloseClick()
        setupAddressesRecyclerview()
        observeAddresses()
        onShippingItemClick()
        onPlaceOrderClick()

        observePlaceOrder()


    }

    private fun setupProductsRecyclerview() {
        binding.rvProducts.apply {
            adapter = cartProductsAdapter
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            addItemDecoration(HorizantalSpacingItemDecorator(23))
        }
    }

    private fun onAddAddressImgClick() {
        binding.imgAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }
    }

    private fun onImgCloseClick() {
        binding.imgCloseBilling.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAddressesRecyclerview() {
        shippingAddressesAdapter = AddressAdapter(args.clickFlag)
        binding.rvAdresses.apply {
            adapter = shippingAddressesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(HorizantalSpacingItemDecorator(23))
        }
    }

    private fun observeAddresses() {
        viewModel.addresses.observe(viewLifecycleOwner) { response ->
            if (response.data == null)
                hideLoading()
            else
                when (response) {
                    is Resource.Loading -> {
                        showLoading()
                    }

                    is Resource.Success -> {
                        hideLoading()
                        shippingAddressesAdapter.differ.submitList(response.data.toList())
                    }

                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, response.message.toString())
                        Toast.makeText(activity, "Error occurred", Toast.LENGTH_SHORT).show()
                    } else -> Unit
                }
        }
    }

    private fun hideLoading() {
        binding.progressbarAddresses.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressbarAddresses.visibility = View.VISIBLE
    }

    private var selectedAddress: Address? = null
    private fun onShippingItemClick() {
        shippingAddressesAdapter.onBtnClick = { address ->
            if(args.clickFlag == UPDATE_ADDRESS_FLAG) {
                val bundle = Bundle()
                bundle.putParcelable("address", address)
                selectedAddress = address
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, bundle)

            }else{
                selectedAddress = address
                binding.tvSelectAddressError.visibility = View.GONE
            }
        }
    }

    private fun onPlaceOrderClick() {
        binding.btnPlaceOlder.setOnClickListener {
            if (selectedAddress == null) {
                binding.tvSelectAddressError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            setupAlertDialog()


        }
    }

    private fun setupAlertDialog() {
        val alertDialog = AlertDialog.Builder(context).create()
        val view = LayoutInflater.from(context).inflate(R.layout.delete_alert_dialog,null,false)
        alertDialog.setView(view)
        val title = view.findViewById<TextView>(R.id.tv_delete_item)
        val message = view.findViewById<TextView>(R.id.tv_delete_message)
        val btnConfirm = view.findViewById<Button>(R.id.btn_yes)
        val btnCancel = view.findViewById<Button>(R.id.btn_no)
        title.text = resources.getText(R.string.place_order)
        message.text = resources.getText(R.string.place_order_confirmation)
        btnConfirm.text = resources.getText(R.string.confirm)
        btnCancel.text = resources.getText(R.string.g_cancel)


        btnConfirm.setOnClickListener {
            viewModel.placeOrder(args.products!!.products,selectedAddress!!,args.price!!)
            alertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun observePlaceOrder() {
        viewModel.placeOrder.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showPlaceOrderLoading()
                }

                is Resource.Success -> {
                    hidePlaceOrderLoading()
                    val bundle = Bundle()
                    bundle.putString("order_completion_flag", ORDER_SUCCESS_FLAG)
                    bundle.putString("orderNumber",response.data?.id)
                    bundle.putParcelable("order",response.data)
                    findNavController().navigate(R.id.action_billingFragment_to_orderCompletionFragment,bundle)
                }

                is Resource.Error -> {
                    hidePlaceOrderLoading()
                    Log.e(TAG,response.message.toString())
                    val bundle = Bundle()
                    bundle.putString("order_completion_flag", ORDER_FAILED_FLAG)
                    findNavController().navigate(R.id.action_billingFragment_to_orderCompletionFragment,bundle)
                } else -> Unit
            }
        }
    }

    private fun hidePlaceOrderLoading() {
        binding.apply {
            progressbarPlaceOrder.visibility = View.GONE
            btnPlaceOlder.visibility = View.VISIBLE
        }

    }

    private fun showPlaceOrderLoading() {
        binding.apply {
            progressbarPlaceOrder.visibility = View.VISIBLE
            btnPlaceOlder.visibility = View.INVISIBLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}