package id.deeromptech.ebc.ui.shopping.ui.seller.input

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
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
import java.util.*
import kotlin.collections.HashMap

@AndroidEntryPoint
class InputProductFragment : Fragment() {

    private var _binding: FragmentInputProductBinding? = null
    private val binding get() = _binding!!
    private val selectedImages = mutableListOf<Uri>()
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage.reference
    val viewModel by viewModels<InputProductViewModel>()
    private val args by navArgs<InputProductFragmentArgs>()
    private val MAX_SELECTED_IMAGES = 3
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

        binding.ivImagePreview1.visibility = View.VISIBLE

        val selectImagesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data

                    val clipData = intent?.clipData
                    val singleImageUri = intent?.data

                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            if (selectedImages.size < MAX_SELECTED_IMAGES) {
                                val imagesUri = clipData.getItemAt(i)?.uri
                                imagesUri?.let { selectedImages.add(it) }
                            } else {
                                ToastUtils.showMessage(
                                    requireContext(),
                                    getString(R.string.maximum_image)
                                )
                                break
                            }
                        }
                    } else if (singleImageUri != null && selectedImages.size < MAX_SELECTED_IMAGES) {
                        selectedImages.add(singleImageUri)
                    } else if (selectedImages.size >= MAX_SELECTED_IMAGES) {
                        ToastUtils.showMessage(
                            requireContext(),
                            getString(R.string.maximum_image)
                        )
                    }

                    updateImages()
                    updateImagesEdit()
                }
            }

        binding.buttonImagesPicker.setOnClickListener {
            if (selectedImages.size < MAX_SELECTED_IMAGES) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "image/*"
                selectImagesActivityResult.launch(intent)
            } else {
                ToastUtils.showMessage(requireContext(), getString(R.string.maximum_image))
            }
        }

        val imagePreviews = listOf(
            binding.ivImagePreview1,
            binding.ivImagePreview2,
            binding.ivImagePreview3
        )

        imagePreviews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val selectedImageUri = selectedImages.getOrNull(index)
                selectedImageUri?.let { uri ->
                    showDeleteConfirmationDialog(uri)
                }
            }
        }

        if (args.edit) {
            binding.apply {
                setUserInformation(args.product)
                btnSaveProduct.visibility = View.GONE
                btnEditProduct.visibility = View.VISIBLE

            }
        }

        binding.btnSaveProduct.setOnClickListener {

            if (binding.edName.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name.", Toast.LENGTH_SHORT).show()
            } else if (binding.edDescription.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a description.", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.edPrice.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a price.", Toast.LENGTH_SHORT).show()
            } else if (binding.edWeight.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a weight.", Toast.LENGTH_SHORT)
                    .show()
            } else if (selectedImages.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one image.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                saveProduct { success ->
                    if (success) {
                        Log.d("test", "Product saved successfully.")
                    } else {
                        Log.d("test", "Failed to save product.")
                    }
                }
            }
        }

        binding.btnEditProduct.setOnClickListener {

            if (binding.edName.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name.", Toast.LENGTH_SHORT).show()
            } else if (binding.edDescription.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a description.", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.edPrice.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a price.", Toast.LENGTH_SHORT).show()
            } else if (binding.edWeight.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a weight.", Toast.LENGTH_SHORT)
                    .show()
            } else if (selectedImages.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one image.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                createEditedProduct { success ->
                    if (success) {
                        Log.d("test", "Product saved successfully.")
                    } else {
                        Log.d("test", "Failed to save product.")
                    }
                }
            }
        }

    }

    private fun showDeleteConfirmationDialog(imageUri: Uri) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete") { dialog, _ ->
                selectedImages.remove(imageUri)
                updateImagesEdit()

                val editedProduct = args.product
                val productRef = firestore.collection("Products")
                    .whereEqualTo("id", editedProduct.id)
                    .whereEqualTo("seller", editedProduct.seller)

                productRef.get().addOnSuccessListener { result ->
                    val documents = result.documents
                    if (documents.isNotEmpty()) {
                        val document = documents[0]
                        val imageUrlToDelete = imageUri.toString()

                        val existingImages = editedProduct.images
                        if (existingImages.contains(imageUrlToDelete)) {
                            // Remove the image URL from the images list in Firestore
                            document.reference.update(
                                "images",
                                FieldValue.arrayRemove(imageUrlToDelete)
                            )
                                .addOnSuccessListener {
                                    // Delete the image from Firebase Storage
                                    val storageRef =
                                        Firebase.storage.getReferenceFromUrl(imageUrlToDelete)
                                    storageRef.delete().addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Image deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to delete image",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.addOnFailureListener { exception ->
                                    Log.e(
                                        "FirestoreError",
                                        "Firestore update error: ${exception.message}",
                                        exception
                                    )
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to update product images",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(requireContext(), "Image Deleted", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        // Image doesn't exist in Firestore, remove it from selectedImages
                        Toast.makeText(requireContext(), "Image deleted", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Log.e(
                        "FirestoreError",
                        "Firestore query error: ${exception.message}",
                        exception
                    )
                    Toast.makeText(requireContext(), "Failed to query product", Toast.LENGTH_SHORT)
                        .show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun setUserInformation(product: Product) {
        binding.apply {

            edName.setText(product.name)
            edDescription.setText(product.description)

            val categoryArray = resources.getStringArray(R.array.product_categories)
            val selectedCategoryIndex = categoryArray.indexOf(product.category)
            spCategory.setSelection(selectedCategoryIndex)

            edPrice.setText(product.price.toInt().toString())
            val offerPercentageValue = product.offerPercentage?.toInt()?.toString() ?: ""
            offerPercentage.setText(offerPercentageValue)
            edWeight.setText(product.weight)

            // Set selected stock availability in RadioGroup
            when (product.stock) {
                "Ready" -> rbStockReady.isChecked = true
                "Not Ready" -> rbStockNotReady.isChecked = true
            }

            selectedImages.clear()
            selectedImages.addAll(product.images.map { Uri.parse(it) })
            updateImagesEdit()

        }
    }

    var user: User? = null
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
                    ToastUtils.showMessage(requireContext(), getString(R.string.error_occurred))
                    return@observe
                }
                else -> Unit
            }
        }
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
        val addressStore = user?.addressStore
        val sellerPhone = user?.phone
        val cityStore = user?.cityStore
        val weight = binding.edWeight.text.toString().trim()
        val rekening = user?.rekening

        if (sellerPhone.isNullOrEmpty()) {
            ToastUtils.showMessage(requireContext(), "Lengkapi Profil Pengguna")
            return
        }

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
                    images,
                    addressStore,
                    sellerPhone,
                    cityStore,
                    weight,
                    rekening
                )

                firestore.collection("Products").add(product).addOnSuccessListener {
                    state(true)
                    hideLoading()
                    ToastUtils.showMessage(requireActivity(), "Adding Product Success")
                }.addOnFailureListener {
                    Log.e("test2", it.message.toString())
                    state(false)
                    hideLoading()
                    ToastUtils.showMessage(
                        requireActivity(),
                        "Adding Product Failed : ${it.message}"
                    )
                }

                ToastUtils.showMessage(requireActivity(), "Adding Product Success")

            } catch (e: Exception) {
                hideLoading()
                ToastUtils.showMessage(requireActivity(), "Adding Product Failed : ${e.message}")
            }
        }
    }

    private fun createEditedProduct(state: (Boolean) -> Unit) {
        observeData()
        val newImagesByteArrays = getImagesByteArraysEdit()
        val name = binding.edName.text.toString().trim()
        val images = mutableListOf<String>()
        val category = binding.spCategory.selectedItem.toString()
        val productDescription = binding.edDescription.text.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.offerPercentage.text.toString().trim()
        val seller = user?.storeName
        val addressStore = user?.addressStore
        val sellerPhone = user?.phone
        val cityStore = user?.cityStore
        val weight = binding.edWeight.text.toString().trim()
        val rekening = user?.rekening
        val productId = args.product.id

        if (sellerPhone.isNullOrEmpty()) {
            ToastUtils.showMessage(requireContext(), "Lengkapi Profil Pengguna")
            return
        }

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
                    newImagesByteArrays.forEach {
                        val id = UUID.randomUUID().toString()
                        launch {
                            val imagesStorage = storage.child("products/images/$id")
                            val result = imagesStorage.putBytes(it).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            images.add(downloadUrl)
                        }
                    }
                }.await()

                val combinedImages = selectedImages.map { it.toString() } + images

                val product = Product(
                    productId,
                    name,
                    category,
                    price.toFloat(),
                    if (offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                    if (productDescription.isEmpty()) null else productDescription,
                    stock,
                    seller,
                    images = combinedImages,
                    addressStore,
                    sellerPhone,
                    cityStore,
                    weight,
                    rekening
                )

                firestore.collection("Products")
                    .whereEqualTo("id", product.id)
                    .whereEqualTo("seller", product.seller)
                    .get()
                    .addOnSuccessListener { result ->
                        val documents = result.documents
                        if (documents.isNotEmpty()) {
                            val document = documents[0]
                            document.reference.update(
                                "name", name,
                                "category", product.category,
                                "price", product.price,
                                "offerPercentage", product.offerPercentage,
                                "description", product.description,
                                "stock", product.stock,
                                "images", product.images,
                                "addressStore", product.addressStore,
                                "sellerPhone", product.sellerPhone,
                                "cityStore", product.cityStore,
                                "weight", product.weight,
                                "rekening", product.rekeningSeller
                            ).addOnSuccessListener {
                                state(true)
                                hideLoading()
                            }.addOnFailureListener {
                                state(false)
                                hideLoading()
                            }
                        } else {
                            state(false)
                            hideLoading()
                        }
                    }
                    .addOnFailureListener {
                        state(false)
                        hideLoading()
                    }

                ToastUtils.showMessage(requireActivity(), "Edit Product Success")

            } catch (e: Exception) {
                hideLoading()
                ToastUtils.showMessage(requireActivity(), "Edit Product Failed : ${e.message}")
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

    private fun getImagesByteArraysEdit(): List<ByteArray> {
        val imagesByteArray = mutableListOf<ByteArray>()

        val newSelectedImages = selectedImages.filter { imageUri ->
            !args.product.images.contains(imageUri.toString())
        }

        newSelectedImages.forEach { imageUri ->
            context?.contentResolver?.openInputStream(imageUri)?.use { inputStream ->
                val byteArray = inputStream.readBytes()
                imagesByteArray.add(byteArray)
                selectedImages.remove(imageUri)
            }
        }

        return imagesByteArray
    }

    private fun updateImagesEdit() {
        binding.tvSelectedImages.text = "${selectedImages.size} " + getString(R.string.image_choose)

        val imagePreviews = listOf(
            binding.ivImagePreview1,
            binding.ivImagePreview2,
            binding.ivImagePreview3
        )

        for (i in imagePreviews.indices) {
            val imageView = imagePreviews[i]

            if (i < selectedImages.size) {
                imageView.visibility = View.VISIBLE
                Glide.with(requireView()).load(selectedImages[i]).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }
        }
    }

    private fun updateImages() {
        binding.tvSelectedImages.text = "${selectedImages.size} " + getString(R.string.image_choose)

        val imagePreviews = listOf(
            binding.ivImagePreview1,
            binding.ivImagePreview2,
            binding.ivImagePreview3
        )

        for (i in 0 until imagePreviews.size) {
            val imageView = imagePreviews[i]

            if (i < selectedImages.size) {
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(selectedImages[i])
            } else {
                imageView.visibility = View.GONE
            }
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