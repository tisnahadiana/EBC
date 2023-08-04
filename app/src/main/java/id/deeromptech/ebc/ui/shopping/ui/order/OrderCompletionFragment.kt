package id.deeromptech.ebc.ui.shopping.ui.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.FragmentBillingBinding
import id.deeromptech.ebc.databinding.FragmentOrderCompletionBinding
import id.deeromptech.ebc.util.Constants.ORDER_FAILED_FLAG
import id.deeromptech.ebc.util.Constants.ORDER_SUCCESS_FLAG

class OrderCompletionFragment : Fragment() {

    val args by navArgs<OrderCompletionFragmentArgs>()
    private var _binding: FragmentOrderCompletionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderCompletionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.GONE

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderFlag = args.orderCompletionFlag

        if (orderFlag == ORDER_FAILED_FLAG) {
            showErrorInformation()
            onErrorButtonClick()
        } else if (orderFlag == ORDER_SUCCESS_FLAG) {
            binding.btnCompletionAction.text = resources.getText(R.string.order_details)
            showSuccessInformation()
            onSuccessClick()
        }

        onCloseImageClick()
    }

    private fun onCloseImageClick() {
        binding.imgCloseOrderCompletion.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onSuccessClick() {
        binding.btnCompletionAction.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("order",args.order)
            findNavController().navigate(R.id.action_orderCompletionFragment_to_orderDetailFragment,bundle)
        }
    }

    private fun showSuccessInformation() {
        binding.apply {
            imgErrorTexture.setImageResource(R.drawable.payment_success)
            tvOrderFailed.text = resources.getText(R.string.payment_success)
            tvOrderTrack.visibility = View.VISIBLE
            tvPaymentExplanation.text =
                resources.getText(R.string.order_success_message).toString().plus(args.orderNumber)
        }
    }

    private fun onErrorButtonClick() {
        binding.btnCompletionAction.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showErrorInformation() {
        binding.apply {
            imgErrorTexture.setImageResource(R.drawable.payment_error)
            tvOrderFailed.text = resources.getText(R.string.payment_failed)
            tvPaymentExplanation.text = resources.getText(R.string.order_error_message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}