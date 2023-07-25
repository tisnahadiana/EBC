package id.deeromptech.ebc.ui.shopping.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.deeromptech.ebc.adapter.ViewPager2Images
import id.deeromptech.ebc.databinding.FragmentProductDetailBinding
import id.deeromptech.ebc.util.hideBottomNavigationView

class ProductDetailFragment : Fragment() {

    private val args by navArgs<ProductDetailFragmentArgs>()
    private lateinit var binding: FragmentProductDetailBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideBottomNavigationView()
        binding = FragmentProductDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.Product

        setupViewpager()

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "Rp. ${product.price}"
            tvProductDescription.text = product.description
            tvProductDetail.text = product.detail
            tvProductStock.text = product.stock
        }

        viewPagerAdapter.differ.submitList(product.images)

    }

    private fun setupViewpager() {
        binding.apply {
            viewpagerProductImages.adapter = viewPagerAdapter
        }
    }
}