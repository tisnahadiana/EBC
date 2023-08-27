package id.deeromptech.ebc.ui.shopping.ui.order

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.AllOrdersAdapter
import id.deeromptech.ebc.databinding.FragmentOrdersBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest

private val TAG = "AllOrdersFragment"
@AndroidEntryPoint
class AllOrdersFragment: Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<AllOrdersViewModel> ()
    val allOrdersAdapter by lazy { AllOrdersAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUser()

        setupOrdersRv()

        lifecycleScope.launchWhenStarted {
            viewModel.profile.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data
                        user?.let {
                            viewModel.getAllOrders(it) // Pass the user object
                        }
                    }
                    is Resource.Error -> {
                        // Handle error state
                        Log.e(TAG, resource.message ?: "Unknown error occurred")
                    }
                    // Handle loading state if needed
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.allOrders.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarAllOrders.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        allOrdersAdapter.differ.submitList(it.data)
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

        allOrdersAdapter.onClick = {
            val action = AllOrdersFragmentDirections.actionAllOrdersFragmentToOrderDetailFragment(it)
            findNavController().navigate(action)
        }

        allOrdersAdapter.onDeleteClick = {order ->
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
                                                    allOrdersAdapter.differ.submitList(it.data)
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
                                    findNavController().navigateUp()
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

    private fun setupOrdersRv() {
        binding.rvAllOrders.apply {
            adapter = allOrdersAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}