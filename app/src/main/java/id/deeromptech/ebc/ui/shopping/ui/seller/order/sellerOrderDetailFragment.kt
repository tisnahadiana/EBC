package id.deeromptech.ebc.ui.shopping.ui.seller.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import id.deeromptech.ebc.databinding.FragmentSellerOrderDetailBinding

class sellerOrderDetailFragment : Fragment() {

    private var _binding: FragmentSellerOrderDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerOrderDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val order = args.order
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}