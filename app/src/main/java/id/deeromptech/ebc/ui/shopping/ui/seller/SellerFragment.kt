package id.deeromptech.ebc.ui.shopping.ui.seller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentSellerBinding
import id.deeromptech.ebc.util.Resource

@AndroidEntryPoint
class SellerFragment : Fragment() {

    private var _binding: FragmentSellerBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<SellerViewModel>()
    val TAG = "SellerFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeProfile()
        onProfileClick()

        binding.inputProductButton.setOnClickListener {
            val product = Product(
                id = "",
                name = "",
                category = "",
                price = 0f,
                stock = "",
                images = listOf()
            )
            val bundle = Bundle().apply {
                putParcelable("product", product)
                putBoolean("edit", false)
            }
            findNavController().navigate(R.id.action_sellerFragment_to_inputProductFragment, bundle)
        }

        binding.seeOrderProduct.setOnClickListener {
            findNavController().navigate(R.id.action_sellerFragment_to_sellerOrderFragment)
        }

        binding.seeProductButton.setOnClickListener {
            findNavController().navigate(R.id.action_sellerFragment_to_sellerProductFragment)
        }
    }

    private fun onProfileClick() {
        binding.userCardView.setOnClickListener {
            user?.let {
                val bundle = Bundle().apply {
                    putParcelable("user", user)
                    putBoolean("edit", true)
                }
                findNavController().navigate(R.id.action_sellerFragment_to_sellerVerificationFragment,bundle)
            }
        }
    }

    var user: User?=null

    private fun observeProfile() {
        viewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    hideLoading()
                    val user = response.data
                    this.user = user
                    binding.apply {
                        tvStoreName.text = user?.storeName
                        Glide.with(requireView()).load(user?.imagePath)
                            .error(R.drawable.ic_profile_black).into(binding.imageUser)
                    }
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

    private fun hideLoading() {
        binding.apply {
            binding.progressUserInfo.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.apply {
            binding.progressUserInfo.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}