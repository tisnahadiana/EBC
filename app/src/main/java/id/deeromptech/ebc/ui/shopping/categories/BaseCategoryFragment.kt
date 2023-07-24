package id.deeromptech.ebc.ui.shopping.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.BestProductsAdapter
import id.deeromptech.ebc.databinding.FragmentBaseCategoryBinding

open class BaseCategoryFragment: Fragment(R.layout.fragment_base_category) {

    private lateinit var binding : FragmentBaseCategoryBinding
    private lateinit var offerAdapter: BestProductsAdapter
    private lateinit var bestProductsAdapter: BestProductsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOfferRv()
        setupBestRv()
    }

    private fun setupBestRv() {
        bestProductsAdapter = BestProductsAdapter()
        binding.rvBestProducts.apply {
            layoutManager = GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductsAdapter
        }
    }

    private fun setupOfferRv() {
        offerAdapter = BestProductsAdapter()
        binding.rvBestProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = offerAdapter
        }
    }
}