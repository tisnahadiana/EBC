package id.deeromptech.ebc.ui.shopping.ui.shippingcost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.model.CostPostageFee
import id.deeromptech.ebc.data.model.cost.DestinationDetails
import id.deeromptech.ebc.data.model.cost.OriginDetails
import id.deeromptech.ebc.databinding.FragmentPostageFeeBinding
import id.deeromptech.ebc.util.gone
import id.deeromptech.ebc.util.localID
import id.deeromptech.ebc.util.visible


class PostageFeeFragment : Fragment() {

    companion object {
        const val DATA_POSTAGE_FEE = "DATA_POSTAGE_FEE"
        const val DATA_ORIGIN = "DATA_ORIGIN"
        const val DATA_DESTINATION = "DATA_DESTINATION"
        const val DATA_COURIER_NAME = "DATA_COURIER_NAME"
    }

    private var _binding: FragmentPostageFeeBinding? = null
    private val binding get() = _binding!!

    private val postageFeeAdapter: PostageFeeAdapter by lazy { PostageFeeAdapter() }
    private var costPostageFee: java.util.ArrayList<CostPostageFee>? = null
    private var originCity: OriginDetails? = null
    private var destinationCity: DestinationDetails? = null
    private var courierName: String? = null
    val args: PostageFeeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostageFeeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        costPostageFee = args.listPostageFee as ArrayList<CostPostageFee>
        originCity = args.originDetails
        destinationCity = args.destinationDetails
        courierName = args.courierName.toString()

        initView(originCity, destinationCity, courierName.toString())
        setupAdapter(costPostageFee ?: return)

    }

    private fun initView(originCity: OriginDetails?, destinationCity: DestinationDetails?, courierName: String?) {
        val strTransportationLine = "${originCity?.cityName} - ${destinationCity?.cityName}"
        binding.transportationLineTV.text = strTransportationLine
        binding.noDataTV.text = getString(
            R.string.courier_not_available, courierName?.uppercase(
                localID()
            ))
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