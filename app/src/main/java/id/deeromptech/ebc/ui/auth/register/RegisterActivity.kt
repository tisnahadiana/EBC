package id.deeromptech.ebc.ui.auth.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.User
import id.deeromptech.ebc.databinding.ActivityRegisterBinding
import id.deeromptech.ebc.ui.auth.login.LoginActivity
import id.deeromptech.ebc.util.RegisterValidation
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val TAG = "RegisterActivity"
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private val viewModel by viewModels<RegisterViewModel>()
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

         onRegisterBtnClick()
         observeSaveUserInformation()
         onLoginClick()


    }

    private fun observeSaveUserInformation() {
        viewModel.register.observe(this, Observer { response ->
            when (response) {
                is Resource.Loading -> {
                    Log.d(TAG, "EmailRegister:Loading")
                    binding.btnRegisterActivity.startAnimation()
                }

                is Resource.Success -> {
                    Log.d(TAG, "EmailRegister:Successful")
                    binding.btnRegisterActivity.stopAnimation()
                    ToastUtils.showMessage(this@RegisterActivity, getString(R.string.success_message_register))
                    viewModel.logOut()
                    viewModel.register.postValue(null)
                    val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(loginIntent)
                    finish()
                }

                is Resource.Error -> {
                    binding.btnRegisterActivity.stopAnimation()
                    Log.e(TAG, "EmailRegister:Error ${response.message.toString()}")
                    ToastUtils.showMessage(this@RegisterActivity, "Register Failed : ${response.message}")
                }
                else -> Unit
            }
        })
    }

    private fun onLoginClick() {
        binding.btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun onRegisterBtnClick() {
        binding.btnRegisterActivity.setOnClickListener {
            binding.btnRegisterActivity.spinningBarColor = resources.getColor(R.color.white)
            binding.btnRegisterActivity.spinningBarWidth = resources.getDimension(com.intuit.sdp.R.dimen._3sdp)
            val user = getUser()
            val password = getPassword()
            user?.let { user ->
                password?.let { password ->
                    viewModel.registerNewUser(user, password)
                    binding.btnRegisterActivity.startAnimation()
                }
            }
        }
    }

    private fun getUser(): User? {
        val userName = binding.edRegisterName.text.toString().trim()
        val phone = binding.edRegisterPhone.text.toString().trim()
        val email = binding.edRegisterEmail.text.toString().trim()
        val role = "user"

        if (userName.isEmpty()) {
            binding.edRegisterName.apply {
                error = resources.getString(R.string.name_cannot_empty)
                requestFocus()
            }
            return null
        }

        if (phone.isEmpty()) {
            binding.edRegisterPhone.apply {
                error = resources.getString(R.string.phone_cannot_empty)
                requestFocus()
            }
            return null
        }

        if (email.isEmpty()) {
            binding.edRegisterEmail.apply {
                error = resources.getString(R.string.email_cannot_empty)
                requestFocus()
            }
            return null
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edRegisterEmail.apply {
                error = resources.getString(R.string.valid_email)
                requestFocus()
            }
            return null
        }


        return User(userName, email, phone, role = role)
    }

    private fun getPassword(): String? {
        val password = binding.edRegisterPassword.text.toString().trim()
        if (password.isEmpty()) {
            binding.edRegisterPassword.apply {
                error = resources.getString(R.string.password_cannot_empty)
                requestFocus()
            }
            return null
        }

        if (password.length < 8) {
            binding.edRegisterPassword.apply {
                error = resources.getString(R.string.password_minimum)
                requestFocus()
            }
            return null
        }
        return password
    }
}