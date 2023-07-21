package id.deeromptech.ebc.ui.auth.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
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

        binding.btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.apply {
            btnRegisterActivity.setOnClickListener {
                val user    = User(
                    edRegisterName.text.toString().trim(),
                    edRegisterEmail.text.toString().trim(),
                    edRegisterPhone.text.toString().trim()
                )
                val password = edRegisterPassword.text.toString()
                viewModel.createAccount(user, password)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.register.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.btnRegisterActivity.startAnimation()
                    }

                    is Resource.Success -> {
                        Log.d("test", it.message.toString())
                        binding.btnRegisterActivity.revertAnimation()
                        ToastUtils.showMessage(this@RegisterActivity, getString(R.string.success_message_register))
                        val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(loginIntent)
                        finish()
                    }

                    is Resource.Error -> {
                        Log.e(TAG, it.message.toString())
                        binding.btnRegisterActivity.revertAnimation()
                        ToastUtils.showMessage(this@RegisterActivity, "Register Failed : ${it.message}")
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect{ validation ->
                if (validation.email is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edRegisterEmail.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }

                if (validation.password is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edRegisterPassword.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
            }
        }

    }
}