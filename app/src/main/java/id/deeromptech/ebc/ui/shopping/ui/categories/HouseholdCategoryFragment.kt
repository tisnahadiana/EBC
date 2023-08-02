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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.SpacingDecorator.HorizantalSpacingItemDecorator
import id.deeromptech.ebc.adapter.BestProductsAdapter
import id.deeromptech.ebc.databinding.FragmentHouseholdBinding
import id.deeromptech.ebc.util.Constants
import id.deeromptech.ebc.util.Resource

@AndroidEntryPoint
class HouseholdCategoryFragment : Fragment(R.layout.fragment_household) {

    val TAG = "HouseholdCategoryFragment"
    private var _binding: FragmentHouseholdBinding? = null
    private val binding get() = _binding!!
    private lateinit var headerAdapter: BestProductsAdapter
    private lateinit var productsAdapter: BestProductsAdapter

    val viewModel by viewModels<HouseholdViewModel> ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headerAdapter = BestProductsAdapter()
        productsAdapter = BestProductsAdapter()

        viewModel.getHousehold()
        viewModel.getMostRequestedHousehold()

        Log.d("Test","electronics")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHouseholdBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderRecyclerview()
        observeHeader()

        setupProductsRecyclerView()
        observeProducts()

        headerPaging()
        productsPaging()

        productsAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product",product)
            bundle.putString("flag", Constants.PRODUCT_FLAG)
            Log.d("test",product.newPrice!!)

            findNavController().navigate(R.id.action_navigation_home_to_productDetailFragment,bundle)
        }

        headerAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product",product)
            bundle.putString("flag", Constants.PRODUCT_FLAG)
            findNavController().navigate(R.id.action_navigation_home_to_productDetailFragment,bundle)
        }
    }
    private fun setupHeaderRecyclerview() {
        binding.rvHeader.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = headerAdapter
            addItemDecoration(HorizantalSpacingItemDecorator(100))
        }
    }

    private fun observeHeader() {
        viewModel.mostHouseholdAccessories.observe(viewLifecycleOwner, Observer { response ->

            when (response) {
                is Resource.Loading -> {
                    showTopLoading()
                    return@Observer
                }

                is Resource.Success -> {
                    hideTopLoading()
                    headerAdapter.differ.submitList(response.data)
                    return@Observer
                }

                is Resource.Error -> {
                    hideTopLoading()
                    Log.e(TAG, response.message.toString())
                    return@Observer
                } else -> Unit
            }
        })
    }

    private fun setupProductsRecyclerView() {
        binding.rvProducts.apply {
            adapter = productsAdapter
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun observeProducts() {
        viewModel.household.observe(viewLifecycleOwner, Observer { response ->

            when (response) {
                is Resource.Loading -> {
                    showBottomLoading()
                    return@Observer
                }

                is Resource.Success -> {
                    hideBottomLoading()
                    productsAdapter.differ.submitList(response.data)
                    return@Observer
                }

                is Resource.Error -> {
                    hideBottomLoading()
                    Log.e(TAG, response.message.toString())
                    return@Observer
                } else -> Unit
            }
        })
    }

    private fun headerPaging() {
        binding.rvHeader.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollHorizontally(1) && dx != 0)
                    viewModel.getMostRequestedHousehold(headerAdapter.differ.currentList.size)

            }
        })
    }

    private fun productsPaging() {
        binding.scrollCupboard.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v!!.getChildAt(0).bottom <= (v.height + scrollY)) {
                viewModel.getHousehold(productsAdapter.differ.currentList.size)
            }
        })
    }
    private fun hideBottomLoading() {
        binding.progressbar2.visibility = View.GONE
    }

    private fun showBottomLoading() {
        binding.progressbar2.visibility = View.VISIBLE
    }

    private fun hideTopLoading() {
        binding.progressbar1.visibility = View.GONE
    }

    private fun showTopLoading() {
        binding.progressbar1.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}