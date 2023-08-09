package id.deeromptech.ebc.ui.shopping.ui.seller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.FragmentProfileBinding
import id.deeromptech.ebc.databinding.FragmentSellerBinding

class SellerFragment : Fragment() {

    private var _binding: FragmentSellerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSellerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inputProductButton.setOnClickListener {
            findNavController().navigate(R.id.action_sellerFragment_to_inputProductFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}