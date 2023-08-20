package id.deeromptech.ebc.ui.shopping.ui.setting

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentUserAccountBinding
import id.deeromptech.ebc.util.Resource
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class UserAccountFragment : Fragment() {
    val TAG = "EditUserInformation"
    private val IMAGE_REQUEST_CODE = 1
    private var _binding: FragmentUserAccountBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<UserAccountFragmentArgs>()
    private var isPicked = false
    var imageArray: ByteArray? = null
    private lateinit var userRole : String
    private lateinit var addressUser : String
    private lateinit var storeName : String
    private lateinit var addressStore : String
    private lateinit var rekening : String

    private val viewModel by viewModels<UserAccountViewModel> ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRole = args.user.role.toString()
        addressUser = args.user.addressUser.toString()
        storeName = args.user.storeName.toString()
        addressStore = args.user.addressStore.toString()
        rekening = args.user.rekening.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUserInformation(args.user)
        onCloseClick()
        onSaveClick()
        onEditImageClick()
        observeUploadImage()
        onEmailClick()
        onForgotPasswordClick()
        observeResetPassword()

        observeUpdateInformation()
    }

    private fun setUserInformation(user: User) {

        binding.apply {
            Glide.with(requireView()).load(user.imagePath)
                .error(R.drawable.ic_profile_black).into(imageUser)

            edNameUser.setText(user.name)
            edPhone.setText(user.phone)
            edEmail.setText(FirebaseAuth.getInstance().currentUser?.email)
        }
    }

    private fun onCloseClick() {
        binding.imageCloseUserAccount.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    private fun onSaveClick() {
        binding.buttonSave.setOnClickListener {
            if (isPicked)
                imageArray?.let { viewModel.uploadProfileImage(it) }
            else {
                val name = binding.edNameUser.text.toString()
                val email = binding.edEmail.text.toString()
                val phone = binding.edPhone.text.toString()
                val image=""
                val role = userRole
                val addressUserData = addressUser
                val storeNameData = storeName
                val addressStoreData = addressStore
                val rekeningData = rekening

                viewModel.updateInformation(name,email,phone,image, role, addressUserData, storeNameData, addressStoreData, rekeningData)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val imageUri = data.data
                isPicked = true
                Glide.with(this).load(imageUri).error(R.drawable.ic_profile_black)
                    .into(binding.imageUser)
                val imageByteArray: ByteArray = compressImage(imageUri)
                imageArray = imageByteArray
            }
        }
    }

    private fun compressImage(imageUri: Uri?): ByteArray {
        val imageInBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
        val imageByteArray = ByteArrayOutputStream()
        imageInBitmap.compress(Bitmap.CompressFormat.JPEG, 20, imageByteArray)
        return imageByteArray.toByteArray()
    }

    private fun onEditImageClick() {
        binding.imageEdit.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(
                Intent.createChooser(
                    intent,
                    resources.getText(R.string.select_profile_image)
                ), IMAGE_REQUEST_CODE
            )
        }
    }

    private fun observeUploadImage() {
        viewModel.uploadProfileImage.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    val firstName = binding.edNameUser.text.toString()
                    val email = binding.edEmail.text.toString()
                    val phone = binding.edPhone.text.toString()

                    viewModel.updateInformation(firstName, email, phone, response.data!!, userRole, addressUser, storeName, addressStore, rekening)
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

    private fun showLoading() {
        binding.apply {
            progressbarAccount.visibility = View.VISIBLE
            buttonSave.visibility = View.INVISIBLE
        }
    }

    private fun hideLoading() {
        binding.apply {
            progressbarAccount.visibility = View.GONE
            buttonSave.visibility = View.VISIBLE
        }
    }

    private fun onEmailClick() {
        binding.edEmail.setOnClickListener {
            binding.edEmail.apply {
                Snackbar.make(requireView(),resources.getText(R.string.g_cant_change_email_message),4500).show()
            }
        }
    }

    private fun onForgotPasswordClick() {
        binding.tvUpdatePassword.setOnClickListener {
            setupAlertDialog()
        }
    }

    private fun setupAlertDialog() {

        val alertDialog = AlertDialog.Builder(context).create()
        val view = LayoutInflater.from(context).inflate(R.layout.delete_alert_dialog,null,false)
        alertDialog.setView(view)
        val title = view.findViewById<TextView>(R.id.tv_delete_item)
        val message = view.findViewById<TextView>(R.id.tv_delete_message)
        val btnConfirm = view.findViewById<Button>(R.id.btn_yes)
        val btnCancel = view.findViewById<Button>(R.id.btn_no)
        title.text = resources.getText(R.string.g_reset_password)
        message.text = resources.getText(R.string.g_reset_password_message).toString().plus("\n ${args.user.email}")
        btnConfirm.text = resources.getText(R.string.g_send)
        btnCancel.text = resources.getText(R.string.g_cancel)


        btnConfirm.setOnClickListener {
            viewModel.resetPassword(args.user.email.trim())
            alertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()

    }

    private fun observeResetPassword() {
        viewModel.passwordReset.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    hideLoading()
                    Snackbar.make(
                        requireView(),
                        resources.getText(R.string.password_reset).toString()
                            .plus("\n ${response.data}"), 4000
                    ).show()
                    viewModel.passwordReset.postValue(null)
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

    private fun observeUpdateInformation() {
        viewModel.updateUserInformation.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showLoading()
                    return@observe
                }

                is Resource.Success -> {
                    hideLoading()
                    findNavController().navigateUp()
                    viewModel.updateUserInformation.postValue(null)
                    viewModel.uploadProfileImage.postValue(null)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}