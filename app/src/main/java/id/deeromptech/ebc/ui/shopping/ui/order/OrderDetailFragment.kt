package id.deeromptech.ebc.ui.shopping.ui.order

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.deeromptech.ebc.R
import id.deeromptech.ebc.SpacingDecorator.VerticalSpacingItemDecorator
import id.deeromptech.ebc.adapter.BillingProductsAdapter
import id.deeromptech.ebc.adapter.CartProductAdapter
import id.deeromptech.ebc.data.local.OrderStatus
import id.deeromptech.ebc.data.local.getOrderStatus
import id.deeromptech.ebc.databinding.FragmentOrderDetailBinding
import id.deeromptech.ebc.ui.shopping.ui.cart.CartViewModel
import id.deeromptech.ebc.util.Constants.ORDER_CONFIRM_STATE
import id.deeromptech.ebc.util.Constants.ORDER_Delivered_STATE
import id.deeromptech.ebc.util.Constants.ORDER_PLACED_STATE
import id.deeromptech.ebc.util.Constants.ORDER_SHIPPED_STATE
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.VerticalItemDecoration
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class OrderDetailFragment : Fragment() {

    val TAG = "OrderDetails"
    val args by navArgs<OrderDetailFragmentArgs>()
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private lateinit var productsAdapter: CartProductAdapter
    val viewModel by viewModels<OrderDetailViewModel> ()

    private val decimalFormat =
        DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOrderAddressAndProducts(args.order)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvOrderId.text = resources.getText(R.string.order)
            .toString().plus("# ${args.order.id}")
        setupRecyclerview()
        observeOrderAddress()

        observeProducts()
        onCloseImageClick()
        setupStepView()

    }

    private fun setupRecyclerview() {
        productsAdapter = CartProductAdapter("From Order Detail")
        binding.rvProducts.apply {
            adapter = productsAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(VerticalSpacingItemDecorator(23))
        }
    }

    private fun observeOrderAddress() {
        viewModel.orderAddress.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Loading -> {
                    showAddressLoading()
                }

                is Resource.Success -> {
                    hideAddressLoading()
                    val address = response.data
                    binding.apply {
                        tvFullName.text = address?.addressTitle
                        tvAddress.text = address?.kampung
                            .plus(", ${address?.desa}")
                            .plus(", ${address?.kecamatan}")
                        tvCity.text = address?.city
                    }
                }

                is Resource.Error -> {
                    hideAddressLoading()
                    Toast.makeText(
                        activity,
                        resources.getText(R.string.error_occurred),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, response.message.toString())
                } else -> Unit
            }
        }
    }

    private fun hideAddressLoading() {
        binding.apply {
            progressbarOrder.visibility = View.GONE
            stepView.visibility = View.VISIBLE
            tvShippingAddress.visibility = View.VISIBLE
            linearAddress.visibility = View.VISIBLE
        }
    }

    private fun showAddressLoading() {
        binding.apply {
            binding.apply {
                progressbarOrder.visibility = View.VISIBLE
                stepView.visibility = View.INVISIBLE
                tvShippingAddress.visibility = View.INVISIBLE
                linearAddress.visibility = View.INVISIBLE
            }
        }
    }
    private fun observeProducts() {
        viewModel.orderProducts.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Loading -> {
                    showProductsLoading()
                }

                is Resource.Success -> {
                    hideProductsLoading()
                    productsAdapter.differ.submitList(response.data)
                    binding.tvTotalPrice.text = args.order.totalPrice
                }

                is Resource.Error -> {
                    hideAddressLoading()
                    Toast.makeText(
                        activity,
                        resources.getText(R.string.error_occurred),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, response.message.toString())
                } else -> Unit
            }
        }
    }

    private fun hideProductsLoading() {
        binding.apply {
            progressbarOrder.visibility = View.GONE
            rvProducts.visibility = View.VISIBLE
            tvProducts.visibility = View.VISIBLE
            linear.visibility = View.VISIBLE
            line1.visibility = View.VISIBLE
        }
    }

    private fun showProductsLoading() {
        binding.apply {
            progressbarOrder.visibility = View.VISIBLE
            rvProducts.visibility = View.INVISIBLE
            tvProducts.visibility = View.INVISIBLE
            linear.visibility = View.INVISIBLE
            line1.visibility = View.INVISIBLE
        }
    }

    private fun onCloseImageClick() {
        binding.imageCloseOrder.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupStepView() {
        val state = when (args.order.state) {
            ORDER_PLACED_STATE -> 1
            ORDER_CONFIRM_STATE -> 2
            ORDER_SHIPPED_STATE -> 3
            ORDER_Delivered_STATE -> 4
            else -> {
                2
            }
        }

        Log.d("test2", args.order.state)
        Log.d("test2", state.toString())
        val steps = arrayOf<String>(
            resources.getText(R.string.g_order_placed).toString(),
            resources.getText(R.string.confirm).toString(),
            resources.getText(R.string.g_shipped).toString(),
            resources.getText(R.string.g_delivered).toString()
        )

        binding.stepView.apply {
            setLabels(steps)
            setBarColorIndicator(getContext().getResources().getColor(R.color.blue))
            setProgressColorIndicator(getContext().getResources().getColor(R.color.green))
            setLabelColorIndicator(getContext().getResources().getColor(R.color.black))
            setCompletedPosition(0)
            drawView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}