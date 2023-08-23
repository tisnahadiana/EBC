package id.deeromptech.ebc.ui.shopping.ui.shippingcost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.model.CostPostageFee
import id.deeromptech.ebc.data.model.ResultData
import id.deeromptech.ebc.data.model.city.CityResult
import id.deeromptech.ebc.data.model.cost.CostRajaOngkir
import id.deeromptech.ebc.databinding.FragmentShippingCostBinding
import id.deeromptech.ebc.util.*

@AndroidEntryPoint
class ShippingCostFragment : Fragment() {

    private var _binding: FragmentShippingCostBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel by viewModels<ShippingCostViewModel>()
    private lateinit var listCity: List<CityResult?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShippingCostBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCities()
    }

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
        val courierAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, couriers)

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

        val b = Bundle().apply {
            putParcelableArrayList("listPostageFee", listPostageFee)
            putParcelable("originDetails", rajaOngkir.originDetails)
            putParcelable("destinationDetails", rajaOngkir.destinationDetails)
            putString("courierName", rajaOngkir.query?.courier)
        }
        findNavController().navigate(R.id.action_shippingCostFragment_to_postageFeeFragment, b)
    }

    private fun showErrorMessage(message: String) {
        binding.loadingPB.gone()
        ToastUtils.showMessage(requireContext(),getString(R.string.message_error, message))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}