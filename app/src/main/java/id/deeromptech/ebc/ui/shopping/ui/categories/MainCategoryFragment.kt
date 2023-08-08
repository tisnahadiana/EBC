package id.deeromptech.ebc.ui.shopping.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.BestDealsAdapter
import id.deeromptech.ebc.adapter.BestProductsAdapter
import id.deeromptech.ebc.adapter.SpecialProductsAdapter
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.FragmentMainCategoryBinding
import id.deeromptech.ebc.util.Constants.PRODUCT_FLAG
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.showBottomNavigationView
import kotlinx.coroutines.flow.collectLatest

private val TAG = "MainCategory"
@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {

    private var _binding: FragmentMainCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var specialProductsAdapter: SpecialProductsAdapter
    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var bestProductsAdapter: BestProductsAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpecialProductsRV()
        setupBestDealsRV()
        setupBestProductsRV()

        specialProductsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_navigation_home_to_productDetailFragment, b)
        }

        bestDealsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_navigation_home_to_productDetailFragment, b)
        }

        bestProductsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_navigation_home_to_productDetailFragment, b)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.specialProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        specialProductsAdapter.differ.submitList(it.data)
                        hideLoading()
//                        ToastUtils.showMessage(requireContext(), "Fetch Data Success")
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

        lifecycleScope.launchWhenStarted {
            viewModel.bestDealsProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        bestDealsAdapter.differ.submitList(it.data)
                        hideLoading()
//                        ToastUtils.showMessage(requireContext(), "Fetch Data Success")
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

        lifecycleScope.launchWhenStarted {
            viewModel.bestProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.bestProductProgressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        bestProductsAdapter.differ.submitList(it.data)
                        binding.bestProductProgressBar.visibility = View.GONE
//                        ToastUtils.showMessage(requireContext(), "Fetch Data Success")
                    }
                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        ToastUtils.showMessage(requireContext(), "Fetch Data Failed : ${it.message}")
                        binding.bestProductProgressBar.visibility = View.GONE
                    }
                    else -> Unit
                }

            }
        }

        binding.nsMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{ v, _,scrolly,_,_ ->
            if (v.getChildAt(0).bottom <= v.height + scrolly){
                viewModel.fetchBestProducts()
            }
        })
    }

    private fun setupSpecialProductsRV() {
        specialProductsAdapter = SpecialProductsAdapter()
        binding.rvSpecialProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = specialProductsAdapter
        }
    }
    private fun setupBestProductsRV() {
        bestProductsAdapter = BestProductsAdapter()
        binding.rvBestProducts.apply {
            layoutManager = GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductsAdapter
        }
    }

    private fun setupBestDealsRV() {
        bestDealsAdapter = BestDealsAdapter()
        binding.rvBestDealsProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealsAdapter
        }
    }

    private fun hideLoading() {
        binding.mainCategoryProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainCategoryProgressBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}