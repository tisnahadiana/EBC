package id.deeromptech.ebc.ui.shopping.ui.seller.input

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.FragmentInputProductBinding
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

@AndroidEntryPoint
class InputProductFragment : Fragment() {

    private var _binding: FragmentInputProductBinding? = null
    private val binding get() = _binding!!
    private val selectedImages = mutableListOf<Uri>()
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage.reference
    val viewModel by viewModels<InputProductViewModel> ()
    private var selectedImagePosition = 0
    private lateinit var seller: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputProductBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUser()
        observeData()

        updateImages()

        val selectImagesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data

                    // Multiple images selected
                    if (intent?.clipData != null) {
                        val count = intent.clipData?.itemCount ?: 0
                        (0 until count).forEach {
                            val imagesUri = intent.clipData?.getItemAt(it)?.uri
                            imagesUri?.let { selectedImages.add(it) }
                        }
                    } else {
                        // One image was selected
                        val imageUri = intent?.data
                        imageUri?.let { selectedImages.add(it) }
                    }

                    // Update the selectedImagePosition to the last index (newly added image)
                    selectedImagePosition = selectedImages.size - 1

                    // Update the image previews after adding new images
                    updateImages()
                }
            }

        binding.buttonImagesPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            selectImagesActivityResult.launch(intent)
        }
    }

    var user: User?=null
    private fun observeData() {
        viewModel.profile.observe(this) { response ->
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
                        tvSeller.text = user?.storeName
                    }
                    return@observe
                }

                is Resource.Error -> {
                    hideLoading()
                    ToastUtils.showMessage(requireContext(),getString(R.string.error_occurred))
                    return@observe
                } else -> Unit
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveProduct) {
            val productValidation = validateInformation()
            if (!productValidation) {
                ToastUtils.showMessage(requireContext(), "Check your inputs")
                return false
            }
            saveProduct(){
                Log.d("test", it.toString())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateInformation(): Boolean {
        if (selectedImages.isEmpty())
            return false
        if (binding.edName.text.toString().trim().isEmpty())
            return false
        if (binding.edPrice.text.toString().trim().isEmpty())
            return false
        return true
    }

    private fun saveProduct(state: (Boolean) -> Unit) {
        observeData()
        val imagesByteArrays = getImagesByteArrays()
        val name = binding.edName.text.toString().trim()
        val images = mutableListOf<String>()
        val category = binding.spCategory.selectedItem.toString()
        val productDescription = binding.edDescription.text.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.offerPercentage.text.toString().trim()
        val seller = user?.storeName

        val radioGroup = binding.rgStockAvailability
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        val stock: String = when (selectedRadioButtonId) {
            R.id.rbStockReady -> "Ready"
            R.id.rbStockNotReady -> "Not Ready"
            else -> "" // Jika tidak ada RadioButton yang dipilih
        }

        lifecycleScope.launch {
            showLoading()
            try {
                async {
                    imagesByteArrays.forEach {
                        val id = UUID.randomUUID().toString()
                        launch {
                            val imagesStorage = storage.child("products/images/$id")
                            val result = imagesStorage.putBytes(it).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            images.add(downloadUrl)
                        }
                    }
                }.await()
                hideLoading()

                val product = Product(
                    UUID.randomUUID().toString(),
                    name,
                    category,
                    price.toFloat(),
                    if (offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                    if (productDescription.isEmpty()) null else productDescription,
                    stock,
                    seller,
                    images
                )

                firestore.collection("Products").add(product).addOnSuccessListener {
                    state(true)
                    hideLoading()
                    ToastUtils.showMessage(requireActivity(), "Adding Product Success")
                }.addOnFailureListener {
                    Log.e("test2", it.message.toString())
                    state(false)
                    hideLoading()
                    ToastUtils.showMessage(requireActivity(), "Adding Product Failed : ${it.message}")
                }

                ToastUtils.showMessage(requireActivity(), "Adding Product Success")

            } catch (e: Exception) {
                hideLoading()
                ToastUtils.showMessage(requireActivity(), "Adding Product Failed : ${e.message}")
            }
        }
    }

    private fun hideLoading() {
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imagesByteArray = mutableListOf<ByteArray>()
        selectedImages.forEach { imageUri ->
            context?.contentResolver?.openInputStream(imageUri)?.use { inputStream ->
                val byteArray = inputStream.readBytes()
                imagesByteArray.add(byteArray)
            }
        }
        return imagesByteArray
    }

    private fun updateImages() {
        binding.tvSelectedImages.text = selectedImages.size.toString()

        // Update the first image preview (imagePreview1) with the selected image at the current position
        if (selectedImages.isNotEmpty() && selectedImagePosition < selectedImages.size) {
            binding.ivImagePreview1.visibility = View.VISIBLE
            binding.ivImagePreview1.setImageURI(selectedImages[selectedImagePosition])
        } else {
            binding.ivImagePreview1.visibility = View.GONE
        }

        // Update the second image preview (imagePreview2) with the selected image at the next position
        if (selectedImages.size > selectedImagePosition + 1) {
            binding.ivImagePreview2.visibility = View.VISIBLE
            binding.ivImagePreview2.setImageURI(selectedImages[selectedImagePosition + 1])
        } else {
            binding.ivImagePreview2.visibility = View.GONE
        }

        // Update the third image preview (imagePreview3) with the selected image at the position after the next
        if (selectedImages.size > selectedImagePosition + 2) {
            binding.ivImagePreview3.visibility = View.VISIBLE
            binding.ivImagePreview3.setImageURI(selectedImages[selectedImagePosition + 2])
        } else {
            binding.ivImagePreview3.visibility = View.GONE
        }
    }

    private fun List<String>.toHashMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        for ((index, imageUrl) in this.withIndex()) {
            map["image_$index"] = imageUrl
        }
        return map
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}