package id.deeromptech.ebc.ui.shopping.ui.seller.order

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
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.SellerOrderAdapter
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentSellerOrderBinding
import id.deeromptech.ebc.ui.shopping.ui.order.AllOrdersFragmentDirections
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
//        showOrderSeller()
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

    }

//    private fun showOrderSeller() {
//
//        val user: User? = null
//        viewModel.getAllOrders(user?.storeName.toString())
//
//        ToastUtils.showMessage(requireContext(), "${user?.storeName.toString()}")
//    }

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

                    ToastUtils.showMessage(requireContext(), "${user?.storeName}")
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