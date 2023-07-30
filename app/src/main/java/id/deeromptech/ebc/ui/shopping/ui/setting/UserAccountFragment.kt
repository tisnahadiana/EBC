package id.deeromptech.ebc.ui.shopping.ui.setting

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentUserAccountBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class UserAccountFragment : Fragment() {
    private var _binding: FragmentUserAccountBinding? = null
    private val binding get() = _binding!!
    private val viewmodel by viewModels<UserAccountViewModel> ()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            imageUri = it.data?.data
            Glide.with(this).load(imageUri).into(binding.imageUser)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewmodel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showUserLoading()
                    }

                    is Resource.Success -> {
                        hideUserLoading()
                        showUserInformation(it.data!!)
                    }

                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), it.message.toString())
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewmodel.updateInfo.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonSave.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonSave.revertAnimation()
                        findNavController().navigateUp()
                    }

                    is Resource.Error -> {
                        ToastUtils.showMessage(requireContext(), it.message.toString())
                    }
                    else -> Unit
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            binding.apply {
                val userName = edNameUser.text.toString().trim()
                val phone = edPhone.text.toString().trim()
                val email = edEmail.text.toString().trim()
                val user = User(userName, phone, email)
                viewmodel.updateUser(user, imageUri)
            }
        }

        binding.imageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }
    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@UserAccountFragment).load(data.imagePath).error(ColorDrawable(Color.BLACK)).into(imageUser)
            edNameUser.setText(data.name)
            edEmail.setText(data.email)
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            imageEdit.visibility = View.VISIBLE
            edPhone.visibility = View.VISIBLE
            edEmail.visibility = View.VISIBLE
            tvUpdatePassword.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
        }
    }

    private fun showUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            imageEdit.visibility = View.INVISIBLE
            edPhone.visibility = View.INVISIBLE
            edEmail.visibility = View.INVISIBLE
            tvUpdatePassword.visibility = View.INVISIBLE
            buttonSave.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}