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
import id.deeromptech.ebc.adapter.BestDealsAdapter
import id.deeromptech.ebc.adapter.BestProductsAdapter
import id.deeromptech.ebc.adapter.SpecialProductsAdapter
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.FragmentMainCategoryBinding
import id.deeromptech.ebc.util.Constants.PRODUCT_FLAG
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.showBottomNavigationView
private val TAG = "MainCategory"
@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {

    private var _binding: FragmentMainCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var specialProductsAdapter: SpecialProductsAdapter
    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var bestProductsAdapter: BestProductsAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        specialProductsAdapter = SpecialProductsAdapter()
        bestDealsAdapter = BestDealsAdapter()
        bestProductsAdapter = BestProductsAdapter()
    }

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

        setupHeaderRecyclerView()
        observeHeaderProducts()

        setupBestDealsRecyclerView()
        observeBestDeals()

        setupAllProductsRecyclerView()
        observeAllProducts()

        headerPaging()
        bestDealsPaging()
        productsPaging()

        observeEmptyHeader()
        observeEmptyBestDeals()

        onHeaderProductClick()
        onBestDealsProductClick()

        observeAddHeaderProductsToCart()


        bestProductsAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product", product)
            bundle.putString("flag", PRODUCT_FLAG)
            findNavController().navigate(
                R.id.action_navigation_home_to_productDetailFragment,
                bundle
            )
        }


    }

    private fun setupHeaderRecyclerView() {
        binding.rvSpecialProducts.apply {
            adapter = specialProductsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun observeHeaderProducts() {
        viewModel.clothes.observe(viewLifecycleOwner) { clothesList ->
            specialProductsAdapter.differ.submitList(clothesList.toList())
        }
    }

    private fun setupBestDealsRecyclerView() {
        binding.rvBestDealsProducts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealsAdapter
        }
    }

    private fun observeBestDeals() {
        viewModel.bestDeals.observe(viewLifecycleOwner) { bestDealsList ->
            bestDealsAdapter.differ.submitList(bestDealsList.toList())
            binding.tvBestDeals.visibility = View.VISIBLE
        }
    }

    private fun setupAllProductsRecyclerView() {
        binding.rvBestProducts.apply {
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductsAdapter
        }
    }

    private fun observeAllProducts() {
        viewModel.home.observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Loading -> {
                    showBottomLoading()
                }

                is Resource.Success -> {
                    hideBottomLoading()
                    bestProductsAdapter.differ.submitList(response.data)
                    Log.d("test", response.data?.size.toString())
                }

                is Resource.Error -> {
                    hideBottomLoading()
                    Log.e(TAG, response.message.toString())
                } else -> Unit
            }
        }
    }

    private fun hideBottomLoading() {
        binding.mainCategoryProgressBar.visibility = View.GONE
        binding.bestProductProgressBar.visibility = View.GONE
        binding.tvBestProduct.visibility = View.VISIBLE

    }

    private fun showBottomLoading() {
        binding.mainCategoryProgressBar.visibility = View.VISIBLE
        binding.bestProductProgressBar.visibility = View.GONE
        binding.tvBestProduct.visibility = View.GONE
    }

    private fun headerPaging() {
        binding.rvSpecialProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && dx != 0) {
                    viewModel.getClothesProducts()
                }
            }
        })
    }

    private fun bestDealsPaging() {
        binding.rvBestDealsProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollHorizontally(1) && dx != 0) {
                    viewModel.getBestDealsProduct()
                }
            }
        })
    }

    private fun productsPaging() {
        binding.nsMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            if (v!!.getChildAt(0).bottom <= (v.height + scrollY)) {
                viewModel.getHomeProduct(bestProductsAdapter.differ.currentList.size)
            }
        })
    }

    private fun observeEmptyHeader() {
        viewModel.emptyClothes.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.apply {
                    rvSpecialProducts.visibility = View.GONE
                }
            }
        }
    }

    private fun observeEmptyBestDeals() {
        viewModel.emptyBestDeals.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.apply {
                    rvBestDealsProducts.visibility = View.GONE
                    tvBestDeals.visibility = View.GONE
                }
            }
        })
    }

    private fun onHeaderProductClick() {
        specialProductsAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product", product)
            findNavController().navigate(
                R.id.action_navigation_home_to_productDetailFragment,
                bundle
            )
        }

        specialProductsAdapter.onAddToCartClick = { product ->
            val image = (product.images?.get("images") as List<String>)[0]
            val cartProduct = Cart(
                product.id,
                product.title!!,
                product.seller!!,
                image,
                product.price!!,
                product.newPrice,
                1
            )
            viewModel.addProductToCart(cartProduct)
        }
    }

    private fun onBestDealsProductClick() {
        bestDealsAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product", product)
            findNavController().navigate(
                R.id.action_navigation_home_to_productDetailFragment,
                bundle
            )

        }
    }

    private fun observeAddHeaderProductsToCart() {
        viewModel.addToCart.observe(viewLifecycleOwner, Observer { response ->

            when (response) {
                is Resource.Loading -> {
                    showTopScreenProgressbar()
                    return@Observer
                }

                is Resource.Success -> {
                    hideTopScreenProgressbar()
                    ToastUtils.showMessage(requireActivity(), getString(R.string.product_added))
                    return@Observer
                }

                is Resource.Error -> {
                    hideTopScreenProgressbar()
                    return@Observer
                } else -> Unit
            }
        })
    }

    private fun hideTopScreenProgressbar() {

    }

    private fun showTopScreenProgressbar() {

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