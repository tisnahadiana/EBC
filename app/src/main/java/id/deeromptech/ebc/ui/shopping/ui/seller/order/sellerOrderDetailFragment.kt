package id.deeromptech.ebc.ui.shopping.ui.seller.order

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
    private val sellerOrderDetailViewModel by viewModels<SellerOrderDetailViewModel>()
    var order = Order()


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

        val totalPrice = order.totalPrice
        val products = order.products
        val address = order.address
        val userName = order.userName
        val userPhone = order.userPhone
        val email = order.email
        val date = order.date
        val orderId = order.orderId
        val orderNote = order.orderNote
        val codeTV = order.codeTV
        val estimationTV = order.estimationTV
        val serviceTV = order.serviceTV
        val serviceDescriptionTV = order.serviceDescriptionTV
        val costValueTV = order.costValueTV

        setupOrderRv()

        binding.codeTV.text = order.codeTV
        binding.estimationTV.text = order.estimationTV
        binding.serviceTV.text = order.serviceTV
        binding.serviceDescriptionTV.text = order.serviceDescriptionTV
        binding.costValueTV.text = order.costValueTV

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

            } else if (order.orderStatus == "Canceled") {
                cardViewCancelOrder.visibility = View.VISIBLE
                stepsView.visibility = View.GONE
                btnConfirmedOrder.visibility = View.GONE
                btnCancelOrder.visibility = View.GONE
                btnShippedOrder.visibility = View.GONE
                btnDeliveredOrder.visibility = View.GONE
            }

            tvOrderId.text = "Order #${order.orderId}"

            tvAddress.text = "${order.address}"
            tvUserName.text = "${getString(R.string.buyer)} : ${order.userName}"
            tvPhoneNumber.text = "${getString(R.string.phone)} : ${order.userPhone}"
            tvOrderNote.text = "${getString(R.string.Note)} : ${order.orderNote}"

            val formattedPrice = "Rp. ${decimalFormat.format(order.totalPrice)}"
            tvTotalPrice.text = formattedPrice


            updateStepViewLabel()

            btnContactBuyer.setOnClickListener {
                val phoneNumber = order.userPhone
                showConfirmationDialog(phoneNumber)
            }

            imageCloseOrder.setOnClickListener {
                findNavController().navigateUp()
            }
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
            dialogResult.setTitle(getString(R.string.dialog_title_confirm_order))
            dialogResult.setImage(R.drawable.ic_verified)
            dialogResult.setMessage(getString(R.string.dialog_message_confirm_order))
            dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
                val orderData = Order(
                    OrderStatus.Confirmed.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    orderNote,
                    codeTV,
                    estimationTV,
                    serviceTV,
                    serviceDescriptionTV,
                    costValueTV,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
                findNavController().navigateUp()
            })
            dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        binding.btnCancelOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle(getString(R.string.dialog_cancel_order_title))
            dialogResult.setImage(R.drawable.ic_cancel)
            dialogResult.setMessage(getString(R.string.dialog_message_cancel_order))
            dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
                val orderData = Order(
                    OrderStatus.Canceled.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    orderNote,
                    codeTV,
                    estimationTV,
                    serviceTV,
                    serviceDescriptionTV,
                    costValueTV,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
                findNavController().navigateUp()
            })
            dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        binding.btnShippedOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle(getString(R.string.dialog_title_shipped_order))
            dialogResult.setImage(R.drawable.ic_shipped)
            dialogResult.setMessage(getString(R.string.dialog_message_order_shipped))
            dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
                val orderData = Order(
                    OrderStatus.Shipped.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    orderNote,
                    codeTV,
                    estimationTV,
                    serviceTV,
                    serviceDescriptionTV,
                    costValueTV,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
                findNavController().navigateUp()
            })
            dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        binding.btnDeliveredOrder.setOnClickListener {
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle(getString(R.string.dialog_title_delivered_order))
            dialogResult.setImage(R.drawable.ic_delivered)
            dialogResult.setMessage(getString(R.string.dialog_message_order_delivered))
            dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
                val orderData = Order(
                    OrderStatus.Delivered.status,
                    totalPrice,
                    products,
                    address,
                    userName,
                    userPhone,
                    email,
                    orderNote,
                    codeTV,
                    estimationTV,
                    serviceTV,
                    serviceDescriptionTV,
                    costValueTV,
                    date,
                    orderId
                )
                sellerOrderDetailViewModel.placeOrder(orderData)
                dialogResult.dismiss()
                findNavController().navigateUp()
            })
            dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }
    }

    fun updateStepViewLabel() {
        val order = args.order

        val stepLabels = listOf(
            OrderStatus.Ordered.status,
            OrderStatus.Confirmed.status,
            OrderStatus.Shipped.status,
            OrderStatus.Delivered.status
        )

        binding.stepsView.setLabels(stepLabels.toTypedArray())
            .setBarColorIndicator(
                getContext()?.getResources()!!.getColor(com.anton46.stepsview.R.color.yellow)
            )
            .setProgressColorIndicator(
                getContext()?.getResources()!!.getColor(R.color.green_variant)
            )
            .setLabelColorIndicator(getContext()?.getResources()!!.getColor(R.color.green))
            .setCompletedPosition(getCurrentOrderState(order.orderStatus))
            .drawView()
    }


    private fun showConfirmationDialog(phoneNumber: String) {
        val dialogResult = DialogResult(requireContext())
        dialogResult.setTitle(getString(R.string.contact_buyer_title))
        dialogResult.setImage(R.drawable.ic_phonecall)
        dialogResult.setMessage(getString(R.string.contact_buyer_dialog))
        dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
            contactBuyer(phoneNumber)
            dialogResult.dismiss()
        })
        dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
            dialogResult.dismiss()
        })
        dialogResult.show()
    }

    private fun contactBuyer(phoneNumber: String) {
        val dialPhoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(dialPhoneIntent)
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