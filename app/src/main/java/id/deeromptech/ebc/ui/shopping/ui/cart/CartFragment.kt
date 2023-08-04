package id.deeromptech.ebc.ui.shopping.ui.cart

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.CartProductAdapter
import id.deeromptech.ebc.data.local.CartProductsList
import id.deeromptech.ebc.databinding.FragmentCartBinding
import id.deeromptech.ebc.ui.shopping.ui.categories.BeautyViewModel
import id.deeromptech.ebc.util.Constants.SELECT_ADDRESS_FLAG
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.VerticalItemDecoration
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private const val TAG = "CartFragment"
@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val cartAdapter by lazy { CartProductAdapter() }
    val viewModel by viewModels<CartViewModel> ()
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.imageCloseCart.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeCart()

        onCloseImgClick()

        onPlusClick()
        onMinusClick()
        onItemClick()

        observeProductClickNavigation()

        onCheckoutClick()
    }
    private var cartProducts: CartProductsList? = null

    private fun onCheckoutClick() {
        binding.buttonCheckout.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("price", binding.tvTotalPrice.text.toString())
            bundle.putString("clickFlag", SELECT_ADDRESS_FLAG)
            bundle.putParcelable("products", cartProducts)
            findNavController().navigate(R.id.action_navigation_cart_to_billingFragment, bundle)
        }
    }
    private fun observeProductClickNavigation() {
        viewModel.product.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        progressbarCart.visibility = View.INVISIBLE
                        val product = response.data
                        val bundle = Bundle()
                        bundle.putParcelable("product", product)
                        findNavController().navigate(
                            R.id.action_navigation_cart_to_productDetailFragment,
                            bundle
                        )
                        viewModel.product.postValue(null)
                    }
                }

                is Resource.Loading -> {
                    binding.apply {
                        progressbarCart.visibility = View.VISIBLE

                    }
                }

                is Resource.Error -> {
                    binding.apply {
                        progressbarCart.visibility = View.INVISIBLE
                        Log.e(TAG, response.message.toString())
                    }
                } else -> Unit
            }
        }
    }

    private fun observePlus() {
        viewModel.plus.observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        progressbarCart.visibility = View.INVISIBLE
                    }
                }

                is Resource.Loading -> {
                    binding.apply {
                        progressbarCart.visibility = View.VISIBLE
                    }
                }

                is Resource.Error -> {
                    binding.apply {
                        progressbarCart.visibility = View.INVISIBLE
                        Log.e(TAG, response.message.toString())
                    }
                } else -> Unit
            }
        }
    }

    private fun observeMinus() {
        viewModel.minus.observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        progressbarCart.visibility = View.INVISIBLE
                    }
                }

                is Resource.Loading -> {
                    binding.apply {
                        progressbarCart.visibility = View.VISIBLE
                    }
                }

                is Resource.Error -> {
                    binding.apply {
                        progressbarCart.visibility = View.INVISIBLE
                        Log.e(TAG, response.message.toString())
                    }
                } else -> Unit
            }
        }
    }
    private fun onItemClick() {
        cartAdapter.onItemClick = { cartProduct ->
            viewModel.getProductFromCartProduct(cartProduct)
        }
    }

    private fun onMinusClick() {
        cartAdapter.onMinusesClick = { cartProduct ->
            if (cartProduct.quantity > 1) {
                viewModel.decreaseQuantity(cartProduct)
                observeMinus()
            } else {
                val alertDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete item from cart")
                    setMessage("Do you want to delete this item from your cart?")
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton("Yes") { dialog, _ ->
                        viewModel.deleteProductFromCart(cartProduct)
                        dialog.dismiss()
                    }
                }
                alertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun onPlusClick() {
        cartAdapter.onPlusClick = { cartProduct ->
            viewModel.increaseQuantity(cartProduct)
            observePlus()
        }
    }

    private fun onCloseImgClick() {
        binding.imageCloseCart.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvCart.layoutManager = LinearLayoutManager(context)
            rvCart.adapter = cartAdapter
        }
    }

    private fun observeCart() {
        viewModel.cartProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                }

                is Resource.Success -> {
                    hideLoading()

                    //Handle empty cart case
                    val products = response.data
                    if (products!!.isNotEmpty()) { // cart is not empty
                        cartProducts = CartProductsList(products)
                        cartAdapter.differ.submitList(products)
                        var totalPrice:Double = 0.0
                        products.forEach {
                            if (it.newPrice != null && it.newPrice.isNotEmpty() && it.newPrice != "0") {
                                totalPrice += it.newPrice.toDouble() * it.quantity
                            } else
                                totalPrice += it.price.toDouble() * it.quantity


                        }

                        val formattedPrice = "Rp. ${decimalFormat.format(totalPrice)}"
                        binding.tvTotalPrice.text = formattedPrice

                    } else { // cart is empty
                        cartAdapter.differ.submitList(products)
                        binding.apply {
                            buttonCheckout.visibility = View.INVISIBLE
                            totalBoxContainer.visibility = View.INVISIBLE
                            layoutCartEmpty.visibility = View.VISIBLE
                        }

                    }
                }

                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, response.message.toString())
                    Toast.makeText(activity, "Oops error occurred", Toast.LENGTH_SHORT).show()
                } else -> Unit
            }
        }
    }

    private fun hideLoading() {
        binding.apply {
            progressBar.visibility = View.GONE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckout.visibility = View.VISIBLE
            layoutCartEmpty.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.INVISIBLE
            buttonCheckout.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        val bottomNavigation = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigation?.visibility = View.VISIBLE
    }

    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
            buttonCheckout.visibility = View.GONE
        }
    }

    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckout.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupCartRv() {
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = cartAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}