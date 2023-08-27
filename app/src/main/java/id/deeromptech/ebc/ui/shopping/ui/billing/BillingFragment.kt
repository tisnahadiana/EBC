package id.deeromptech.ebc.ui.shopping.ui.billing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.AddressAdapter
import id.deeromptech.ebc.adapter.BillingProductsAdapter
import id.deeromptech.ebc.data.local.*
import id.deeromptech.ebc.data.model.CostPostageFee
import id.deeromptech.ebc.data.model.ResultData
import id.deeromptech.ebc.data.model.city.CityResult
import id.deeromptech.ebc.data.model.cost.CostRajaOngkir
import id.deeromptech.ebc.data.model.cost.DestinationDetails
import id.deeromptech.ebc.data.model.cost.OriginDetails
import id.deeromptech.ebc.databinding.FragmentBillingBinding
import id.deeromptech.ebc.dialog.DialogResult
import id.deeromptech.ebc.ui.shopping.ui.order.OrderViewModel
import id.deeromptech.ebc.ui.shopping.ui.shippingcost.PostageFeeAdapter
import id.deeromptech.ebc.ui.shopping.ui.shippingcost.ShippingCostViewModel
import id.deeromptech.ebc.util.*
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@AndroidEntryPoint
class BillingFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<Cart>()
    private var totalPrice = 0f
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    private var selectedAddress: Address? = null

    private val orderViewModel by viewModels<OrderViewModel>()
    private val mainViewModel by viewModels<ShippingCostViewModel>()
    private lateinit var listCity: List<CityResult?>

    private val postageFeeAdapter: PostageFeeAdapter by lazy { PostageFeeAdapter() }

    var user: User? = null

    val TAG = "BillingFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingViewModel.getUser()

        products = args.products.toList()
        totalPrice = args.totalPrice

        loadCities()
    }

    override fun onResume() {
        super.onResume()

        if (user?.addressUser != null) {
            binding.originAutoCompleteTV.setText(user?.cityUser)
        }
        loadCities()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        observeAddress()
//        setupAddressRv()

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        binding.imageAddAddress.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("user", user)
            }
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment, bundle)
        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        ToastUtils.showMessage(requireContext(), "Your order was placed")
                    }

                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        ToastUtils.showMessage(requireContext(), it.message.toString())
                    }
                    else -> Unit
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        postageFeeAdapter.onClick = { selectedPostageFee ->

            binding.materialCardView.visibility = View.VISIBLE

            val formattedOldPrice = "Rp. ${decimalFormat.format(selectedPostageFee.value)}"
            binding.tvTotalOngkir.text = formattedOldPrice

            val totalPriceWithoutOngkir =
                totalPrice // Get the original total price without shipping cost
            val totalPriceWithOngkir =
                totalPriceWithoutOngkir + selectedPostageFee.value!! // Calculate total price with shipping cost

            val formattedTotalPriceWithOngkir = "Rp. ${decimalFormat.format(totalPriceWithOngkir)}"
            binding.tvTotalPrice.text =
                formattedTotalPriceWithOngkir // Update the total price including shipping cost

            binding.codeTV.text = selectedPostageFee.code
            binding.estimationTV.text = selectedPostageFee.etd
            binding.serviceTV.text = selectedPostageFee.service
            binding.serviceDescriptionTV.text = selectedPostageFee.description
            binding.costValueTV.text = selectedPostageFee.value.toRupiah()

            binding.costListRV.visibility = View.GONE

        }

        val formattedPrice = "Rp. ${decimalFormat.format(totalPrice)}"
        binding.tvTotalPrice.text = formattedPrice

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (user?.phone.isNullOrEmpty()) {
                ToastUtils.showMessage(requireContext(), getString(R.string.inform_user_empty))
                return@setOnClickListener
            }
            if (user?.addressUser == null) {
                ToastUtils.showMessage(requireContext(), getString(R.string.order_addressUser_message))
                return@setOnClickListener
            }

            if (binding.tvTotalOngkir.text.isNullOrBlank()) {
                ToastUtils.showMessage(requireContext(), getString(R.string.order_totalongkir_blank))
                return@setOnClickListener
            }

            if (binding.edNoteOrder.text.isNullOrBlank()) {
                ToastUtils.showMessage(requireContext(), getString(R.string.order_edNoteOrder_blank))
                return@setOnClickListener
            }
            if (binding.textShippingAddress.text.isNullOrEmpty()) {
                ToastUtils.showMessage(requireContext(), getString(R.string.order_addressDetail_blank))
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }

    }

    private fun observeAddress() {
        billingViewModel.profile.observe(viewLifecycleOwner) { response ->
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
                        textShippingAddress.text = user?.addressUser
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
                }
                else -> Unit
            }
        }
    }

    fun showLoading() {
        binding.progressbarAddress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.progressbarAddress.visibility = View.GONE
    }

    private fun showOrderConfirmationDialog() {
        val address = binding.textShippingAddress.text.toString().trim()
        val orderNote = binding.edNoteOrder.text.toString().trim()
        val codeTV = binding.codeTV.text.toString().trim()
        val estimationTV = binding.estimationTV.text.toString().trim()
        val serviceTV = binding.serviceTV.text.toString().trim()
        val serviceDescriptionTV = binding.serviceDescriptionTV.text.toString()
        val costValueTV = binding.costValueTV.text.toString()
        val totalPriceData = binding.tvTotalPrice.text.toString().trim()
        val totalPriceFloat = totalPriceData.replace("Rp. ", "").replace(".", "").toFloat()

        val dialogResult = DialogResult(requireContext())
        dialogResult.setTitle("Order Items")
        dialogResult.setImage(R.drawable.ic_order)
        dialogResult.setMessage("Do you want to order your cart item?")
        dialogResult.setPositiveButton("Yes", onClickListener = {
            val order = Order(
                OrderStatus.Ordered.status,
                totalPriceFloat,
                products,
                address,
                user!!.name,
                user!!.phone,
                user!!.email,
                orderNote,
                codeTV,
                estimationTV,
                serviceTV,
                serviceDescriptionTV,
                costValueTV
            )
            orderViewModel.placeOrder(order)
            dialogResult.dismiss()
        })
        dialogResult.setNegativeButton("No", onClickListener = {
            dialogResult.dismiss()
        })
        dialogResult.show()
    }

//    private fun setupAddressRv() {
//        binding.rvAddress.apply {
//            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
//            adapter = addressAdapter
//            addItemDecoration(HorizontalItemDecoration())
//        }
//    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    //ShippingCost

    private fun initClick() {
        binding.checkPostageMB.setOnClickListener { checkedPostageFee() }
    }

    private fun loadCities() {
        val listCity = mainViewModel.getCities()
        listCity.observe(this) {
            when (it) {
                is ResultData.Success -> {
                    binding.loadingPB.gone()
                    initSpinner(it.data?.rajaOngkir?.results ?: return@observe)

                    if (user != null) {
                        binding.originAutoCompleteTV.setText(user?.cityUser)
                    }

                    if (products.isNotEmpty()) {
                        val cityStore: String? = products[0].product.cityStore
                        val weight: String? = products[0].product.weight
                        binding.destinationAutoCompleteTV.setText(cityStore)
                        binding.weightTIET.setText(weight)
                    }
                }
                is ResultData.Loading -> binding.loadingPB.visible()
                is ResultData.Failed -> showErrorMessage(it.message.toString())
                is ResultData.Exception -> showErrorMessage(it.message.toString())
            }
        }
    }

    private fun initSpinner(cityResults: List<CityResult?>) {
        listCity = cityResults
        val cities = mutableListOf<String>()
        val couriers = resources.getStringArray(R.array.list_courier)
        for (i in cityResults.indices) cities.add(cityResults[i]?.cityName ?: "")
        val cityAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, cities)
        val courierAdapter =
            ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, couriers)

        binding.destinationAutoCompleteTV.setAdapter(cityAdapter)
        binding.originAutoCompleteTV.setAdapter(cityAdapter)
        binding.courierAutoCompleteTV.setAdapter(courierAdapter)
        initClick()
    }

    private fun checkedForm(selectedCity: String): CityResult? {
        var city: CityResult? = null
        val dataCity = listCity.filter {
            it?.cityName?.contains(selectedCity) ?: false
        }

        for (i in dataCity.indices) {
            val matchedCity = dataCity[i]?.cityName ?: ""
            if (selectedCity == matchedCity) city = dataCity[i]
        }
        return city
    }

    private fun checkedPostageFee() {
        val strOriginCity = binding.originAutoCompleteTV.text.toString()
        val strDestinationCity = binding.destinationAutoCompleteTV.text.toString()
        val strCourier = binding.courierAutoCompleteTV.text.toString()
        val strWeight = binding.weightTIET.text.toString()

        if (strDestinationCity.isNotEmpty() && strOriginCity.isNotEmpty() && strCourier.isNotEmpty() && strWeight.isNotEmpty()) {
            val originCity = checkedForm(strOriginCity)
            val destinationCity = checkedForm(strDestinationCity)
            val courier = strCourier.lowercase(localID())
            val weight = strWeight.toInt()
            val idOriginCity = originCity?.cityId ?: ""
            val idDestinationCity = destinationCity?.cityId ?: ""

            checkedCost(idOriginCity, idDestinationCity, weight, courier)
        } else {
            ToastUtils.showMessage(requireContext(), getString(R.string.form_empty))
        }

    }

    private fun checkedCost(origin: String, destination: String, weight: Int, courier: String) {
        val costData = mainViewModel.getCost(origin, destination, weight, courier)
        costData.observe(this) {
            when (it) {
                is ResultData.Success -> {
                    binding.loadingPB.gone()
                    setupAdapter(it.data?.rajaOngkir)
                }
                is ResultData.Loading -> binding.loadingPB.visible()
                is ResultData.Failed -> showErrorMessage(it.message.toString())
                is ResultData.Exception -> showErrorMessage(it.message.toString())
            }
        }
    }

    private fun setupAdapter(rajaOngkir: CostRajaOngkir?) {
        val listPostageFee = arrayListOf<CostPostageFee>()
        for (i in rajaOngkir?.results?.indices ?: return) {
            val costs = rajaOngkir.results[i].costs
            for (j in costs?.indices ?: return) {
                val cost = rajaOngkir.results[i].costs?.get(j)?.cost
                for (k in cost?.indices ?: return) {
                    val code = rajaOngkir.results[i].code
                    val name = rajaOngkir.results[i].name
                    val service = costs[j].service
                    val description = costs[j].description
                    val value = cost[k].value
                    val etd = cost[k].etd
                    listPostageFee.add(CostPostageFee(code, name, service, description, etd, value))
                }
            }
        }

        initView(rajaOngkir.originDetails, rajaOngkir.destinationDetails, rajaOngkir.query?.courier)
        setupAdapter(listPostageFee)

        binding.originTIL.visibility = View.GONE
        binding.destinationTIL.visibility = View.GONE
        binding.courierTIL.visibility = View.GONE
        binding.weightTIL.visibility = View.GONE
        binding.checkPostageMB.visibility = View.GONE
    }

    private fun showErrorMessage(message: String) {
        binding.loadingPB.gone()
        ToastUtils.showMessage(requireContext(), getString(R.string.message_error, message))
    }

    //PostageFee

    private fun initView(
        originCity: OriginDetails?,
        destinationCity: DestinationDetails?,
        courierName: String?
    ) {
        val strTransportationLine = "${originCity?.cityName} - ${destinationCity?.cityName}"
        binding.transportationLineTV.text = strTransportationLine
        binding.noDataTV.text =
            getString(R.string.courier_not_available, courierName?.uppercase(localID()))
    }

    private fun setupAdapter(listPostageFee: ArrayList<CostPostageFee>) {
        if (listPostageFee.size > 0) binding.noDataTV.gone() else binding.noDataTV.visible()
        postageFeeAdapter.submitList(listPostageFee)
        binding.costListRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = postageFeeAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}