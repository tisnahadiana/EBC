package id.deeromptech.ebc.ui.shopping.ui.seller.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.FragmentSellerOrderBinding
import id.deeromptech.ebc.databinding.FragmentSellerProductBinding

class SellerOrderFragment : Fragment() {

    private var _binding: FragmentSellerOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerOrderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}