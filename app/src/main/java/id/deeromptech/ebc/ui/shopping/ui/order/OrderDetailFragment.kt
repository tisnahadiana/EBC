package id.deeromptech.ebc.ui.shopping.ui.order

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.BillingProductsAdapter
import id.deeromptech.ebc.data.local.OrderStatus
import id.deeromptech.ebc.databinding.FragmentOrderDetailBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.util.VerticalItemDecoration
import id.deeromptech.ebc.util.toRupiah
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order = args.order


        setupOrderRv()

        binding.apply {

            if (order.orderNote == null) {
                tvOrderNote.visibility = View.GONE
            }

            tvOrderId.text = "Order #${order.orderId}"
            tvPhoneNumber.text = "${getString(R.string.phone)} : ${order.products[0].product.sellerPhone}"
            codeTV.text = order.codeTV
            estimationTV.text = order.estimationTV
            serviceTV.text = order.serviceTV
            serviceDescriptionTV.text = order.serviceDescriptionTV
            costValueTV.text = order.costValueTV

            val stepLabels = listOf(
                OrderStatus.Ordered.status,
                OrderStatus.Confirmed.status,
                OrderStatus.Shipped.status,
                OrderStatus.Delivered.status
            )
            tvAddress.text = order.address
//            tvPhoneNumber.text = phoneNumber.toString()

            val formattedPrice = "Rp. ${decimalFormat.format(order.totalPrice)}"
            tvTotalPrice.text = formattedPrice

            tvOrderNote.text = "${getString(R.string.Note)} : ${order.orderNote}"

            stepsView.setLabels(stepLabels.toTypedArray())
                .setBarColorIndicator(getContext()?.getResources()!!.getColor(com.anton46.stepsview.R.color.yellow))
                .setProgressColorIndicator(getContext()?.getResources()!!.getColor(R.color.green_variant))
                .setLabelColorIndicator(getContext()?.getResources()!!.getColor(R.color.green))
                .setCompletedPosition(getCurrentOrderState(order.orderStatus))
                .drawView();

            btnContactBuyer.setOnClickListener {
                val phoneNumber = order.userPhone
                showConfirmationDialog(phoneNumber)
            }
        }

        billingProductsAdapter.differ.submitList(order.products)

        billingProductsAdapter.onClickProduct = {
            val b = Bundle().apply {
                putParcelable("product", it)
                putBoolean("seller", false)
            }
            findNavController().navigate(R.id.action_orderDetailFragment_to_productDetailFragment, b)
        }
    }

    private fun showConfirmationDialog(phoneNumber: String) {
        val dialogResult = DialogResult(requireContext())
        dialogResult.setTitle(getString(R.string.contact_seller_title))
        dialogResult.setImage(R.drawable.ic_phonecall)
        dialogResult.setMessage(getString(R.string.contact_seller_dialog))
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