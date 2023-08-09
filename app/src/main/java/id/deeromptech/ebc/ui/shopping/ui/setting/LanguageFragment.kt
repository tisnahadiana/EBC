package id.deeromptech.ebc.ui.shopping.ui.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.deeromptech.ebc.databinding.FragmentLanguageBinding
import id.deeromptech.ebc.ui.shopping.ShoppingActivity
import java.util.*

class LanguageFragment : Fragment() {
    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentLanguage = Locale.getDefault().language

        binding.imgCloseLanguage.setOnClickListener {
            findNavController().navigateUp()
        }
        Log.d("test", currentLanguage!!)
        when (currentLanguage) {
            "en" -> {
                changeToEnglish()
            }

            "id" -> {
                changeToIndonesia()
            }
        }

        binding.linearIndonesia.setOnClickListener {
            changeLanguage("id")
        }

        binding.linearEnglish.setOnClickListener {
            changeLanguage("en")
        }

    }

    private fun changeToIndonesia() {
        binding.apply {
            imgIndonesia.visibility = View.VISIBLE
            imgEnglish.visibility = View.INVISIBLE
        }
    }

    private fun changeToEnglish() {
        binding.apply {
            imgIndonesia.visibility = View.INVISIBLE
            imgEnglish.visibility = View.VISIBLE
        }
    }

    private fun changeLanguage(code: String) {
        val intent = Intent(requireActivity(),ShoppingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val sharedPref = activity?.getSharedPreferences("Language", Context.MODE_PRIVATE)
        sharedPref?.edit()?.putString("language", "en")?.apply()
        if (code == "en") {
            setLocal(requireActivity(),"en")
            changeToEnglish()
            sharedPref?.edit()?.putString("language", "en")?.apply()
            startActivity(intent)
        } else if (code == "id") {
            setLocal(requireActivity(),"id")
            changeToIndonesia()
            sharedPref?.edit()?.putString("language", "id")?.apply()
            startActivity(intent)
        }
    }

    private fun setLocal(activity: Activity, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val resources = context?.resources
        val config = resources?.configuration
        config?.locale = locale
        resources?.updateConfiguration(config,resources.displayMetrics)
    }


}