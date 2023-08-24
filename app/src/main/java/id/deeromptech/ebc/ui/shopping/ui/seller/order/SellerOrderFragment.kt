package id.deeromptech.ebc.ui.shopping.ui.seller.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.SellerOrderAdapter
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentSellerOrderBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.ui.auth.login.LoginActivity
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SellerOrderFragment : Fragment() {

    private var _binding: FragmentSellerOrderBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<SellerOrderViewModel> ()
    val sellerOrderAdapter by lazy { SellerOrderAdapter() }
    val order: Order? = null
    val TAG = "SellerOrderFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getUser()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerOrderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrdersRv()
        observeOrderSeller()

        lifecycleScope.launchWhenStarted {
            viewModel.allOrders.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarAllOrders.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        sellerOrderAdapter.differ.submitList(it.data)
                        if (it.data.isNullOrEmpty()) {
                            binding.tvEmptyOrders.visibility = View.VISIBLE
                        }
                    }

                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), it.message.toString() )
                        binding.tvEmptyOrders.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }

        sellerOrderAdapter.onClick = {
            val action = SellerOrderFragmentDirections.actionSellerOrderFragmentToSellerOrderDetailFragment(it)
            findNavController().navigate(action)
        }

        sellerOrderAdapter.onDeleteClick = {order ->
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle(getString(R.string.dialog_delete_order))
            dialogResult.setImage(R.drawable.ic_bin)
            dialogResult.setMessage(getString(R.string.dialog_message_order_result))
            dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
                val firestore = FirebaseFirestore.getInstance()

                firestore.collection("orders")
                    .whereEqualTo("orderId", order.orderId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            document.reference.delete()
                                .addOnSuccessListener {
                                    // Handle successful deletion
                                    Toast.makeText(requireContext(), "Order deleted", Toast.LENGTH_SHORT).show()
                                    lifecycleScope.launchWhenStarted {
                                        viewModel.allOrders.collectLatest {
                                            when(it){
                                                is Resource.Loading -> {
                                                    binding.progressbarAllOrders.visibility = View.VISIBLE
                                                }

                                                is Resource.Success -> {
                                                    binding.progressbarAllOrders.visibility = View.GONE
                                                    sellerOrderAdapter.differ.submitList(it.data)
                                                    if (it.data.isNullOrEmpty()) {
                                                        binding.tvEmptyOrders.visibility = View.VISIBLE
                                                    }
                                                }

                                                is Resource.Error -> {
                                                    ToastUtils.showMessage(requireContext(), it.message.toString() )
                                                    binding.tvEmptyOrders.visibility = View.GONE
                                                }
                                                else -> Unit
                                            }
                                        }
                                    }
                                    setupOrdersRv()
                                }
                                .addOnFailureListener { exception ->
                                    // Handle failure
                                    Toast.makeText(requireContext(), "Failed to delete order", Toast.LENGTH_SHORT).show()
                                    Log.e(TAG, "Error deleting order: ${exception.message}")
                                }
                        } else {
                            // Handle case when no matching order is found
                            Toast.makeText(requireContext(), "Order not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                        Toast.makeText(requireContext(), "Failed to delete order", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error deleting order: ${exception.message}")
                    }
                dialogResult.dismiss()
            })
            dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()

        }

    }

    var user: User? = null
    private fun observeOrderSeller() {
        viewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    hideLoading()
                    val user = response.data
                    this.user = user

                    viewModel.getAllOrders(user?.storeName.toString())
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

    private fun hideLoading() {
        binding.apply {
            binding.progressbarAllOrders.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.apply {
            binding.progressbarAllOrders.visibility = View.VISIBLE
        }
    }

    private fun setupOrdersRv() {
        binding.rvAllOrders.apply {
            adapter = sellerOrderAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}