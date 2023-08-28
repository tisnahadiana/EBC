package id.deeromptech.ebc.ui.shopping.ui.seller.product

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.SpacingDecorator.VerticalSpacingItemDecorator
import id.deeromptech.ebc.adapter.SellerProductAdapter
import id.deeromptech.ebc.databinding.FragmentSellerProductBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest

private val TAG = "SellerProductFragment"
@AndroidEntryPoint
class SellerProductFragment : Fragment() {

    private var _binding: FragmentSellerProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var sellerProductAdapter: SellerProductAdapter
    val viewModel by viewModels<SellerProductViewModel> ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerProductBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProductSellerRV()

        viewModel.getUser()

        viewModel.profile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    user?.let {
                        viewModel.fetchSellerProducts(it) // Pass the user object
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

        lifecycleScope.launchWhenStarted {
            viewModel.sellerProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        sellerProductAdapter.differ.submitList(it.data)
                        hideLoading()
                        if (it.data.isNullOrEmpty()) {
                            binding.imgProductEmpty.visibility = View.VISIBLE
                            binding.tvProductEmpty.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        ToastUtils.showMessage(requireContext(), "Fetch Data Failed : ${it.message}")
                    }
                    else -> Unit
                }

            }
        }

        sellerProductAdapter.onClick = {
            val b = Bundle().apply {
                putParcelable("product", it)
                putBoolean("seller", true)
            }
            findNavController().navigate(R.id.action_sellerProductFragment_to_productDetailFragment, b)
        }

        sellerProductAdapter.onDelete = { product ->
            val dialogResult = DialogResult(requireContext())
            dialogResult.setTitle(getString(R.string.delete_product_title))
            dialogResult.setImage(R.drawable.ic_sell_product)
            dialogResult.setMessage(getString(R.string.delete_product_dialog) + product.name)
            dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
                lifecycleScope.launchWhenStarted {
                    viewModel.deleteSellerProduct(product)
                }
                dialogResult.dismiss()
            })
            dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
                dialogResult.dismiss()
            })
            dialogResult.show()
        }

        sellerProductAdapter.onUpdate = {
            val bundle = Bundle().apply {
                putParcelable("product", it)
                putBoolean("edit", true)
            }
            findNavController().navigate(R.id.action_sellerProductFragment_to_inputProductFragment, bundle)
        }
    }

    private fun hideLoading() {
        binding.progressBarSellerProduct.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressBarSellerProduct.visibility = View.VISIBLE
    }

    private fun setupProductSellerRV() {
        sellerProductAdapter = SellerProductAdapter()
        binding.rvProductSeller.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = sellerProductAdapter
            addItemDecoration(VerticalSpacingItemDecorator(40))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}