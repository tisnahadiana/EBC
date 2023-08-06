package id.deeromptech.ebc.ui.shopping.ui.detail

import android.annotation.SuppressLint
import android.graphics.Paint
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
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.ViewPager2Images
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.databinding.FragmentProductDetailBinding
import id.deeromptech.ebc.util.Constants.IMAGES
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.hideBottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    val args by navArgs<ProductDetailFragmentArgs>()
    val TAG = "ProductPreviewFragment"

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val viewModel by viewModels<ProductDetailViewModel>()
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideBottomNavigationView()
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigation =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigation.visibility = View.GONE

        val product = args.product

        setupViewpager()
        setProductInformation(product)

        onImageCloseClick()
        onBtnAddToCartClick()

        observeAddToCart()

    }

    private fun setupViewpager() {
        binding.viewpager2Images.adapter = viewPagerAdapter
    }

    private fun onBtnAddToCartClick() {
        binding.btnAddToCart.apply {
            setOnClickListener {

                val product = args.product
                val image = (product.images?.get(IMAGES) as List<String>)[0]
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
                setBackgroundResource(R.color.black)
            }
        }
    }

    private fun onImageCloseClick() {
        binding.imgClose.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setProductInformation(product: Product) {
        val imagesList = product.images!![IMAGES] as List<String>
        binding.apply {
            viewPagerAdapter.differ.submitList(imagesList)
            tvProductName.text = product.title
            tvProductDescription.text = product.description
            tvProductPrice.text = "$${product.price}"
            tvProductOfferPrice.visibility = View.GONE
            product.newPrice?.let {
                if (product.newPrice.isNotEmpty() && product.newPrice != "0") {
                    tvProductPrice.paintFlags =
                        tvProductPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvProductOfferPrice.text = "$${product.newPrice}"
                    tvProductOfferPrice.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeAddToCart() {
        viewModel.addToCart.observe(viewLifecycleOwner) { response ->
            val btn = binding.btnAddToCart
            when (response) {
                is Resource.Loading -> {
                    startLoading()
                }

                is Resource.Success -> {
                    stopLoading()
                    viewModel.addToCart.value = null
                }

                is Resource.Error -> {
                    Toast.makeText(activity, "Oops! error occurred", Toast.LENGTH_SHORT).show()
                    viewModel.addToCart.value = null
                    Log.e(TAG, response.message.toString())
                } else -> Unit
            }
        }
    }

    private fun stopLoading() {
        binding.apply {
            btnAddToCart.visibility = View.VISIBLE
            progressbar.visibility = View.INVISIBLE
        }
    }

    private fun startLoading() {
        binding.apply {
            btnAddToCart.visibility = View.INVISIBLE
            progressbar.visibility = View.VISIBLE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}