package id.deeromptech.ebc.ui.shopping.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.deeromptech.ebc.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgEmail.setOnClickListener { openEmailApp() }
        binding.imgCloseHelp.setOnClickListener { findNavController().navigateUp() }
    }

    private fun openEmailApp() {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("deeromptech@gmail.com")) // recipients
            putExtra(Intent.EXTRA_SUBJECT, "")
            putExtra(Intent.EXTRA_TEXT, "")
            putExtra(Intent.EXTRA_STREAM, Uri.parse("content://path/to/email/attachment"))
        }.also { startActivity(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}