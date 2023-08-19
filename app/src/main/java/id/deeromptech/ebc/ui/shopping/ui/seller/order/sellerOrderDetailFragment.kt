package id.deeromptech.ebc.ui.shopping.ui.seller.order

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.BillingProductsSellerAdapter
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.data.local.OrderStatus
import id.deeromptech.ebc.databinding.FragmentSellerOrderDetailBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.VerticalItemDecoration
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@AndroidEntryPoint
class sellerOrderDetailFragment : Fragment() {

    private var _binding: FragmentSellerOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<sellerOrderDetailFragmentArgs>()
    private val billingProductsSellerAdapter by lazy { BillingProductsSellerAdapter() }
    private val decimalFormat =
        DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
    private val sellerOrderDetailViewModel by viewModels<SellerOrderDetailViewModel> ()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerOrderDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val order = args.order

        var totalPrice = order.totalPrice
        var products = order.products
        var address = order.address
        var userName = order.userName
        var userPhone = order.userPhone
        var email = order.email
        var date = order.date
        var orderId = order.orderId

        ToastUtils.showMessage(requireContext(), "${order.orderStatus}")
        setupOrderRv()

        binding.apply {

            if (order.orderStatus == "Ordered") {

                btnConfirmedOrder.visibility = View.VISIBLE
                btnCancelOrder.visibility = View.VISIBLE
                btnShippedOrder.visibility = View.GONE
                btnDeliveredOrder.visibility = View.GONE

            } else if (order.orderStatus == "Confirmed") {

                btnConfirmedOrder.visibility = View.GONE
                btnCancelOrder.visibility = View.GONE
                btnShippedOrder.visibility = View.VISIBLE
                btnDeliveredOrder.visibility = View.GONE

            } else if (order.orderStatus == "Shipped") {

                btnConfirmedOrder.visibility = View.GONE
                btnCancelOrder.visibility = View.GONE
                btnShippedOrder.visibility = View.GONE
                btnDeliveredOrder.visibility = View.VISIBLE

            } else if (order.orderStatus == "Delivered") {

                btnConfirmedOrder.visibility = View.GONE
                btnCancelOrder.visibility = View.GONE
                btnShippedOrder.visibility = View.GONE
                btnDeliveredOrder.visibility = View.GONE

            } else {
                //
            }

            tvOrderId.text = "Order #${order.orderId}"

            val stepLabels = listOf(
                OrderStatus.Ordered.status,
                OrderStatus.Confirmed.status,
                OrderStatus.Shipped.status,
                OrderStatus.Delivered.status
            )
            tvAddress.text = "${order.address}"
            tvUserName.text = "${getString(R.string.buyer)} : ${order.userName}"
            tvPhoneNumber.text = "${getString(R.string.phone)} : ${order.userPhone}"

            val formattedPrice = "Rp. ${decimalFormat.format(order.totalPrice)}"
            tvTotalPrice.text = formattedPrice

            stepsView.setLabels(stepLabels.toTypedArray())
                .setBarColorIndicator(
                    getContext()?.getResources()!!.getColor(com.anton46.stepsview.R.color.yellow)
                )
                .setProgressColorIndicator(
                    getContext()?.getResources()!!.getColor(R.color.green_variant)
                )
                .setLabelColorIndicator(getContext()?.getResources()!!.getColor(R.color.green))
                .setCompletedPosition(getCurrentOrderState(order.orderStatus))
                .drawView();
        }

        billingProductsSellerAdapter.differ.submitList(order.products)

        billingProductsSellerAdapter.onClickProduct = {
            val b = Bundle().apply {
                putParcelable("product", it)
                putBoolean("seller", false)
            }
            findNavController().navigate(
                R.id.action_sellerOrderDetailFragment_to_productDetailFragment,
                b
            )
        }

        binding.btnConfirmedOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle("Confirm Order")
            dialogResult.setImage(R.drawable.ic_verified)
            dialogResult.setMessage("Do you want to confirm this order?")
            dialogResult.setPositiveButton("Yes", onClickListener = {
                val orderData = Order(
                    OrderStatus.Confirmed.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    date,
                    orderId
                )

                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
            })
            dialogResult.setNegativeButton("No", onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        binding.btnCancelOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle("Cancel Order")
            dialogResult.setImage(R.drawable.ic_cancel)
            dialogResult.setMessage("Do you want to cancel this order?")
            dialogResult.setPositiveButton("Yes", onClickListener = {
                val orderData = Order(
                    OrderStatus.Canceled.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
            })
            dialogResult.setNegativeButton("No", onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        binding.btnShippedOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle("Shipped Order")
            dialogResult.setImage(R.drawable.ic_shipped)
            dialogResult.setMessage("Do you want to update this status order to shipped?")
            dialogResult.setPositiveButton("Yes", onClickListener = {
                val orderData = Order(
                    OrderStatus.Shipped.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
            })
            dialogResult.setNegativeButton("No", onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        binding.btnDeliveredOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle("Shipped Order")
            dialogResult.setImage(R.drawable.ic_delivered)
            dialogResult.setMessage("Do you want to update this status order to shipped?")
            dialogResult.setPositiveButton("Yes", onClickListener = {
                val orderData = Order(
                    OrderStatus.Delivered.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
            })
            dialogResult.setNegativeButton("No", onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }
    }

    private fun getCurrentOrderState(orderStatus: String): Int {
        return when (orderStatus) {
            "Ordered" -> 0
            "Confirmed" -> 1
            "Shipped" -> 2
            "Delivered" -> 3
            else -> 0
        }
    }

    private fun setupOrderRv() {
        binding.rvProducts.apply {
            adapter = billingProductsSellerAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}