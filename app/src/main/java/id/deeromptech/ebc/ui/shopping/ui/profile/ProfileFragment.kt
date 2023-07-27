package id.deeromptech.ebc.ui.shopping.ui.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.deeromptech.ebc.BuildConfig
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.FragmentProfileBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.ui.auth.login.LoginActivity
import id.deeromptech.ebc.ui.shopping.ui.profile.seller.SellerVerificationActivity
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.util.showBottomNavigationView
import kotlinx.coroutines.flow.collectLatest

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val viewModel by viewModels<ProfileViewModel> ()

    private val binding get() = _binding!!

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

        auth = Firebase.auth
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.constraintProfile.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_userAccountFragment)
        }

        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_allOrdersFragment)
        }

        binding.linearBilling.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavigationProfileToBillingFragment(0f, emptyArray(), false)
            findNavController().navigate(action)
        }

        binding.linearLogOut.setOnClickListener {
            signOut()
        }

        binding.tvVersion.text = "Version ${BuildConfig.VERSION_CODE}"

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE
                        Glide.with(requireView()).load(it.data!!.imagePath).error(ColorDrawable(
                            Color.BLACK)).into(binding.imageUser)
                        binding.tvUserName.text = "${it.data.name}"
                    }

                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), it.message.toString())
                        binding.progressbarSettings.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }
//        binding.btnTobeSeller.setOnClickListener {
//            startActivity(Intent(requireContext(), SellerVerificationActivity::class.java))
//        }
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // User is logged in, set the profile image and name
//            binding.txtNameUser.text = currentUser.displayName
//
//            // Check if the user has a profile photo URL
//            val photoUrl = currentUser.photoUrl
//            if (photoUrl != null) {
//                // Load and display the profile image using Glide
//                Glide.with(this)
//                    .load(photoUrl)
//                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
//                    .into(binding.imgProfile)
//            } else {
//                // If there is no profile photo URL, you can set a default image
//                // binding.imgProfile.setImageResource(R.drawable.default_profile_image)
//                // Or hide the image view if you prefer
//                // binding.imgProfile.visibility = View.GONE
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signOut() {
        val dialogResult = DialogResult(requireContext())
        dialogResult.setTitle("Logout")
        dialogResult.setImage(R.drawable.logout)
        dialogResult.setMessage("Apakah anda yakin ingin logout?")
        dialogResult.setPositiveButton("Ya", onClickListener = {
            auth.signOut()
            googleSignInClient.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        })
        dialogResult.setNegativeButton("Tidak", onClickListener = {
            dialogResult.dismiss()
        })
        dialogResult.show()
    }

}