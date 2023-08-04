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
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.AllOrdersAdapter
import id.deeromptech.ebc.databinding.FragmentMainCategoryBinding
import id.deeromptech.ebc.databinding.FragmentOrdersBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AllOrdersFragment: Fragment() {
    val TAG = "AllOrdersFragment"
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<AllOrdersViewModel> ()
    val allOrdersAdapter by lazy { AllOrdersAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getUserOrders()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        val root: View = binding.root
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeAllOrders()
        onCloseClick()
        onItemClick()
        binding.imageCloseOrders.setOnClickListener {
            findNavController().navigateUp()
        }

    }
    private fun setupRecyclerView() {
        binding.rvAllOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allOrdersAdapter
        }
    }

    private fun observeAllOrders() {
        viewModel.userOrders.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    hideLoading()
                    val orders = response.data
                    if (orders!!.isEmpty())
                        binding.apply {
                            imgEmptyBox.visibility = View.VISIBLE
                            imgEmptyBoxTexture.visibility = View.VISIBLE
                            tvEmptyOrders.visibility = View.VISIBLE
                            return@observe
                        }
                    binding.apply {
                        imgEmptyBox.visibility = View.GONE
                        imgEmptyBoxTexture.visibility = View.GONE
                        tvEmptyOrders.visibility = View.GONE
                    }
                    allOrdersAdapter.differ.submitList(orders)
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

    private fun onCloseClick() {
        binding.imageCloseOrders.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onItemClick() {
        allOrdersAdapter.onItemClick = {order ->
            val bundle = Bundle()
            bundle.putParcelable("order",order)
            findNavController().navigate(R.id.action_allOrdersFragment_to_orderDetailFragment,bundle)

        }
    }

    private fun hideLoading() {
        binding.progressbarAllOrders.visibility = View.GONE

    }

    private fun showLoading() {
        binding.progressbarAllOrders.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}