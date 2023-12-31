package id.deeromptech.ebc.ui.shopping.ui.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.SpacingDecorator.VerticalSpacingItemDecorator
import id.deeromptech.ebc.adapter.CategoriesRecyclerAdapter
import id.deeromptech.ebc.adapter.SearchRecyclerAdapter
import id.deeromptech.ebc.databinding.FragmentSearchBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.*

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private val TAG = "SearchFragment"
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethodManger: InputMethodManager
    val viewModel by viewModels<SearchViewModel> ()
    private lateinit var categoriesAdapter: CategoriesRecyclerAdapter
    private lateinit var searchAdapter: SearchRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCategories()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryRecyclerView()
        setupSearchRecyclerView()
        showKeyboardAutomatically()

        searchProducts()
        observeSearch()

        observeCategories()

        onSearchTextClick()

        onCancelTvClick()

        binding.frameScan.setOnClickListener {
            ToastUtils.showMessage(requireContext(), getString(R.string.g_coming_soon))
        }
        binding.fragmeMicrohpone.setOnClickListener {
            ToastUtils.showMessage(requireContext(), getString(R.string.g_coming_soon))
        }

    }

    private fun setupCategoryRecyclerView() {
        categoriesAdapter = CategoriesRecyclerAdapter()
        binding.rvCategories.apply {
            adapter = categoriesAdapter
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            addItemDecoration(VerticalSpacingItemDecorator(40))
        }
    }

    private fun setupSearchRecyclerView() {
        searchAdapter = SearchRecyclerAdapter()
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showKeyboardAutomatically() {
        inputMethodManger =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManger.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        binding.edSearch.requestFocus()
    }

    var job: Job? = null
    private fun searchProducts() {
        binding.edSearch.addTextChangedListener { query ->
            val queryTrim = query.toString().trim()
            if (queryTrim.isNotEmpty()) {
                val searchQuery = query.toString().substring(0, 1).toUpperCase()
                    .plus(query.toString().substring(1))
                job?.cancel()
                job = CoroutineScope(Dispatchers.IO).launch {
                    delay(500L)
                    viewModel.searchProducts(searchQuery)
                }
            } else {
                searchAdapter.differ.submitList(emptyList())
                hideCancelTv()
            }
        }
    }

    private fun hideCancelTv() {
        binding.tvCancel.visibility = View.GONE
    }

    private fun observeSearch() {
        viewModel.search.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Loading -> {
                    Log.d("test", "Loading")
                    return@Observer
                }

                is Resource.Success -> {
                    val products = response.data
                    searchAdapter.differ.submitList(products)
                    showChancelTv()
                    return@Observer
                }

                is Resource.Error -> {
                    Log.e(TAG, response.message.toString())
                    showChancelTv()
                    return@Observer
                } else -> Unit
            }
        })
    }

    private fun showChancelTv() {
        binding.tvCancel.visibility = View.VISIBLE
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Loading -> {
                    showCategoriesLoading()
                    return@Observer
                }

                is Resource.Success -> {
                    hideCategoriesLoading()
                    val categories = response.data
                    categoriesAdapter.differ.submitList(categories?.toList())
                    return@Observer
                }

                is Resource.Error -> {
                    hideCategoriesLoading()
                    Log.e(TAG, response.message.toString())
                    return@Observer
                } else -> Unit
            }
        })
    }

    private fun hideCategoriesLoading() {
        binding.progressbarCategories.visibility = View.GONE
    }

    private fun showCategoriesLoading() {
        binding.progressbarCategories.visibility = View.VISIBLE
    }

    private fun onSearchTextClick() {
        searchAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product", product)
            bundle.putBoolean("seller", false)

            /**
             * Hide the keyboard
             */

            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(requireView().windowToken, 0)

            findNavController().navigate(
                R.id.action_navigation_search_to_productDetailFragment,
                bundle
            )

        }
    }

    private fun onCancelTvClick() {
        binding.tvCancel.setOnClickListener {
            searchAdapter.differ.submitList(emptyList())
            binding.edSearch.setText("")
            hideCancelTv()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}