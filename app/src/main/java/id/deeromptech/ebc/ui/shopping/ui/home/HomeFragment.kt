package id.deeromptech.ebc.ui.shopping.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.adapter.HomeViewPagerAdapter
import id.deeromptech.ebc.databinding.FragmentHomeBinding
import id.deeromptech.ebc.ui.shopping.categories.MainCategory

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnHelp.setOnClickListener {
            sendEmailToAdmin()
        }
        binding.btnNotification.setOnClickListener {

        }

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategory()
        )

        val viewPagerAdapter = HomeViewPagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewPagerHome.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Main"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sendEmailToAdmin() {
        val adminEmail = getString(R.string.admin_email)
        val emailSubject = getString(R.string.email_subject)

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$adminEmail")
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        }

        if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(emailIntent)
        }
    }

}