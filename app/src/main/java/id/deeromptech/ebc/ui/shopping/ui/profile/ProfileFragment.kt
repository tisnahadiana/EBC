package id.deeromptech.ebc.ui.shopping.ui.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.BuildConfig
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentProfileBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.ui.auth.login.LoginActivity
import id.deeromptech.ebc.ui.shopping.ui.shippingcost.ShippingCostActivity
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.showBottomNavigationView

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val viewModel by viewModels<ProfileViewModel>()

    private val binding get() = _binding!!
    val TAG = "ProfileFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        googleSignInClient =
            GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)

        binding.linearLogOut.setOnClickListener {
            signOut()
        }

        binding.tvVersion.text = "Version ${BuildConfig.VERSION_CODE}"

        onBillingAndAddressesClick()
        onProfileClick()
        onAllOrderClick()
        onShippingCost()
        onLanguageClick()
        onHelpClick()

        observeProfile()

        onTobeSellerClick()
        onMyStoreClick()
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }

    private fun onHelpClick() {
        binding.linearHelp.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_helpFragment)
        }
    }

    private fun onLanguageClick() {
        binding.linearLanguage.setOnClickListener {
//            findNavController().navigate(R.id.action_navigation_profile_to_languageFragment)
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun onShippingCost() {
        binding.linearShippingCosts.setOnClickListener {
            val intent = Intent(requireContext(), ShippingCostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onAllOrderClick() {
        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_allOrdersFragment)
        }
    }

    private fun onProfileClick() {
        binding.constraintProfile.setOnClickListener {
            user?.let {
                val bundle = Bundle()
                bundle.putParcelable("user", user)
                findNavController().navigate(
                    R.id.action_navigation_profile_to_userAccountFragment,
                    bundle
                )
            }
        }
    }

    private fun onTobeSellerClick() {
        binding.linearTobeSeller.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("user", user)
                putBoolean("edit", false)
            }
            findNavController().navigate(
                R.id.action_navigation_profile_to_sellerVerificationFragment,
                bundle
            )
        }
    }

    private fun onMyStoreClick() {
        binding.linearMystore.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_sellerFragment)
        }
    }

    var user: User? = null
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
                        tvUserName.text = user?.name
                        Glide.with(requireView()).load(user?.imagePath)
                            .error(R.drawable.ic_profile_black).into(binding.imageUser)
                        if (user?.role == "seller") {
                            linearMystore.visibility = View.VISIBLE
                            linearTobeSeller.visibility = View.GONE
                        } else {
                            linearMystore.visibility = View.GONE
                            linearTobeSeller.visibility = View.VISIBLE
                        }
                    }
                    return@observe
                }

                is Resource.Error -> {
                    hideLoading()
//                    Toast.makeText(
//                        activity,
//                        resources.getText(R.string.error_occurred),
//                        Toast.LENGTH_SHORT
//                    ).show()
                    Log.e(TAG, response.message.toString())
                    return@observe
                }
                else -> Unit
            }
        }
    }

    private fun onBillingAndAddressesClick() {
        binding.linearBilling.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavigationProfileToBillingFragment(
                0f,
                emptyArray(),
                false
            )
            findNavController().navigate(action)
        }
    }

    private fun hideLoading() {
        binding.apply {
            binding.progressbarSettings.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.apply {
            binding.progressbarSettings.visibility = View.VISIBLE
        }
    }

    private fun signOut() {
        val dialogResult = DialogResult(requireContext())
        dialogResult.setTitle(getString(R.string.logout_title_dialog))
        dialogResult.setImage(R.drawable.logout)
        dialogResult.setMessage(getString(R.string.logout_dialog_message))
        dialogResult.setPositiveButton(getString(R.string.g_yes), onClickListener = {
//            auth.signOut()
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut()
            dialogResult.dismiss()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        })
        dialogResult.setNegativeButton(getString(R.string.g_no), onClickListener = {
            dialogResult.dismiss()
        })
        dialogResult.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}