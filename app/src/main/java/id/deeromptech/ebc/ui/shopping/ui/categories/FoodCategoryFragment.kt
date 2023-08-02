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
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.SpacingDecorator.HorizantalSpacingItemDecorator
import id.deeromptech.ebc.adapter.BestProductsAdapter
import id.deeromptech.ebc.data.local.CategoryList
import id.deeromptech.ebc.databinding.FragmentElectronicsBinding
import id.deeromptech.ebc.databinding.FragmentFoodBinding
import id.deeromptech.ebc.util.Constants
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FoodCategoryFragment : Fragment(R.layout.fragment_food) {

    val TAG = "FoodCategoryFragment"
    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!
    private lateinit var headerAdapter: BestProductsAdapter
    private lateinit var productsAdapter: BestProductsAdapter

    val viewModel by viewModels<FoodViewModel> ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headerAdapter = BestProductsAdapter()
        productsAdapter = BestProductsAdapter()

        viewModel.getFood()
        viewModel.getMostRequestedFood()

        Log.d("Test","food")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
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
        viewModel.mostFoodsAccessories.observe(viewLifecycleOwner, Observer { response ->

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
        viewModel.food.observe(viewLifecycleOwner, Observer { response ->

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
                    viewModel.getMostRequestedFood(headerAdapter.differ.currentList.size)

            }
        })
    }

    private fun productsPaging() {
        binding.scrollCupboard.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v!!.getChildAt(0).bottom <= (v.height + scrollY)) {
                viewModel.getFood(productsAdapter.differ.currentList.size)
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