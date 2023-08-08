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

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val args by navArgs<OrderDetailFragmentArgs>()
    private val decimalFormat =
        DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

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

        val order = args.order

        setupOrderRv()

        binding.apply {

            tvOrderId.text = "Order #${order.orderId}"

            val stepLabels = listOf(
                OrderStatus.Ordered.status,
                OrderStatus.Confirmed.status,
                OrderStatus.Shipped.status,
                OrderStatus.Delivered.status
            )

            tvFullName.text = order.address.addressTitle
            tvAddress.text =
                "${order.address.kampung} ${order.address.desa} ${order.address.kecamatan} ${order.address.city} ${order.address.provinsi}"

            val formattedPrice = "Rp. ${decimalFormat.format(order.totalPrice)}"
            tvTotalPrice.text = formattedPrice

            stepsView.setLabels(stepLabels.toTypedArray())
                .setBarColorIndicator(getContext()?.getResources()!!.getColor(com.anton46.stepsview.R.color.yellow))
                .setProgressColorIndicator(getContext()?.getResources()!!.getColor(R.color.green_variant))
                .setLabelColorIndicator(getContext()?.getResources()!!.getColor(R.color.green))
                .setCompletedPosition(getCurrentOrderState(order.orderStatus))
                .drawView();
        }

        billingProductsAdapter.differ.submitList(order.products)
    }

    private fun getCurrentOrderState(orderStatus: String): Int {
        return when (getOrderStatus(orderStatus)) {
            is OrderStatus.Ordered -> 0
            is OrderStatus.Confirmed -> 1
            is OrderStatus.Shipped -> 2
            is OrderStatus.Delivered -> 3
            else -> 0
        }
    }

    private fun setupOrderRv() {
        binding.rvProducts.apply {
            adapter = billingProductsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}