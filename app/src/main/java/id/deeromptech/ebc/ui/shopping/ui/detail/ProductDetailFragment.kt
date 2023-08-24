package id.deeromptech.ebc.ui.shopping.ui.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.adapter.ViewPager2Images
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.FragmentProductDetailBinding
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        if (args.seller){
            binding.apply {
                btnAddToCart.visibility  = View.GONE
                btnBuynow.visibility = View.GONE
            }
        }

        setupViewpager()

        binding.btnAddToCart.setOnClickListener {
            viewModel.addUpdateProductInCart(Cart(product, 1))
        }

        binding.btnBuynow.setOnClickListener {
            viewModel.addUpdateProductInCart(Cart(product, 1))
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.btnBuynow.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.btnBuynow.stopAnimation()
                        ToastUtils.showMessage(requireContext(), "Product Was Added!" )
                    }

                    is Resource.Error -> {
                        binding.btnBuynow.stopAnimation()
                        ToastUtils.showMessage(requireContext(), it.message.toString() )
                    }
                    else -> Unit
                }
            }
        }

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.apply {
            tvProductName.text = product.name

            val discountedPrice = product.price - (product.price * (product.offerPercentage!! / 100))
            val formattedPrice = "Rp. ${decimalFormat.format(discountedPrice)}"
            tvProductPrice.text = formattedPrice

//            tvProductPrice.text = "Rp. ${product.price}"
            tvProductDescription.text = product.description
            tvProductSeller.text = "Store : ${product.seller}"
            tvProductStock.text = "Stock : ${product.stock}"
            tvStoreAddress.text = "Address : ${product.addressStore}"
        }

        viewPagerAdapter.differ.submitList(product.images)

        val arrowLeft = binding.arrowLeft
        val arrowRight = binding.arrowRight

        arrowLeft.setOnClickListener {
            binding.viewpagerProductImages.currentItem -= 1
        }

        arrowRight.setOnClickListener {
            binding.viewpagerProductImages.currentItem += 1
        }

        binding.viewpagerProductImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val totalImages = viewPagerAdapter.differ.currentList.size

                // Hide both arrows if there's only one image
                if (totalImages == 1) {
                    binding.arrowLeft.visibility = View.GONE
                    binding.arrowRight.visibility = View.GONE
                } else {
                    // Show left arrow if not at the beginning
                    binding.arrowLeft.visibility = if (position > 0) View.VISIBLE else View.GONE

                    // Show right arrow if not at the end
                    binding.arrowRight.visibility = if (position < totalImages - 1) View.VISIBLE else View.GONE
                }
            }
        })
    }

    private fun setupViewpager() {
        binding.apply {
            viewpagerProductImages.adapter = viewPagerAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}