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
import id.deeromptech.ebc.data.local.Category
import id.deeromptech.ebc.databinding.FragmentFashionBinding
import id.deeromptech.ebc.util.Constants
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FashionCategoryFragment : BaseCategoryFragment() {
    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryFactory(firestore, Category.Fashion)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.offerProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showOfferLoading()
                    }
                    is Resource.Success -> {
                        offerAdapter.differ.submitList(it.data)
                        hideOfferLoading()
                    }
                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), "Fetch Data Failed : ${it.message}")
                        hideOfferLoading()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.bestProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showBestLoading()
                    }
                    is Resource.Success -> {
                        bestProductsAdapter.differ.submitList(it.data)
                        hideBestLoading()
                    }
                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), "Fetch Data Failed : ${it.message}")
                        hideBestLoading()
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onBestProductsPagingRequest() {

    }

    override fun onOfferPagingRequest() {

    }
}