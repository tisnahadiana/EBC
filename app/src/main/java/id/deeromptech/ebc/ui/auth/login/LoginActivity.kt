package id.deeromptech.ebc.ui.auth.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import id.deeromptech.ebc.R
import id.deeromptech.ebc.databinding.ActivityLoginBinding
import id.deeromptech.ebc.ui.auth.register.RegisterActivity
import id.deeromptech.ebc.util.Resource
import id.deeromptech.ebc.util.ToastUtils
import id.deeromptech.ebc.ui.auth.register.RegisterViewModel
import id.deeromptech.ebc.ui.shopping.ShoppingActivity

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    val TAG: String = "LoginFragment"
    val GOOGLE_REQ_CODE = 13

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private val viewModelRegister by viewModels<RegisterViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        onLoginClick()
        observerLogin()
        observerLoginError()
        onDontHaveAccountClick()
        onForgotPasswordClick()
        observeResetPassword()
        observeSaveUserInformation()

        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        onFacebookSignIn()
        observeSaveUserInformation()

    }

    private fun onFacebookSignIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_REQ_CODE)
        }
    }

    private fun observeResetPassword() {
        viewModelRegister.resetPassword.observe(this, Observer { response ->
            when (response) {
                is Resource.Loading -> {

                    return@Observer
                }

                is Resource.Success -> {
                    ToastUtils.showMessage(this@LoginActivity, getString(R.string.success_message_login))
                    viewModelRegister.resetPassword.postValue(null)
                    return@Observer
                }

                is Resource.Error -> {
                    ToastUtils.showMessage(this, "Reset Password Failed : ${response.message}")
                    Log.e(TAG, response.message.toString())

                    return@Observer
                } else -> Unit
            }
        })
    }
    private fun onForgotPasswordClick() {
        binding.tvForgotPassword.setOnClickListener {
            setupBottomSheetDialogLogin()
        }
    }

    private fun setupBottomSheetDialogLogin() {
        val dialog = BottomSheetDialog(this, R.style.DialogStyle)
        val view = layoutInflater.inflate(R.layout.reset_password_dialog, null)
        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()

        val edEmail = view.findViewById<EditText>(R.id.edEmail)
        val btnSend = view.findViewById<Button>(R.id.btn_send)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        btnSend.setOnClickListener {
            val email = edEmail.text.toString().trim()
            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches()
            ) {
                viewModelRegister.resetPassword(email)
                dialog.dismiss()
            } else {
                edEmail.requestFocus()
                edEmail.error = resources.getText(R.string.g_check_your_email)
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun onDontHaveAccountClick() {
        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun observerLoginError() {
        viewModelRegister.loginError.observe(this, Observer { error ->
            Log.e(TAG, error)
            ToastUtils.showMessage(this,"Please check your information")
            binding.btnLoginActivity.revertAnimation()

        })
    }

    private fun observerLogin() {
        viewModelRegister.login.observe(this, Observer {
            if (it == true) {
                binding.btnLoginActivity.revertAnimation()
                startActivity(Intent(this, ShoppingActivity::class.java))
                finish()
            }
        })
    }

    private fun onLoginClick() {
        binding.btnLoginActivity.setOnClickListener {
            binding.btnLoginActivity.spinningBarColor = resources.getColor(R.color.white)
            binding.btnLoginActivity.spinningBarWidth = resources.getDimension(com.intuit.sdp.R.dimen._3sdp)

            val email = getEmail()?.trim()
            val password = getPassword()
            email?.let {
                password?.let {
                    binding.btnLoginActivity.startAnimation()
                    viewModelRegister.loginUser(email, password)
                }
            }
        }
    }

    private fun getPassword(): String? {
        val password = binding.edLoginPassword.text.toString()

        if (password.isEmpty()) {
            binding.edLoginPassword.apply {
                error = resources.getString(R.string.password_cannot_empty)
                requestFocus()
            }
            return null
        }

        if (password.length < 8) {
            binding.edLoginPassword.apply {
                error = resources.getString(R.string.password_minimum)
                requestFocus()
            }
            return null
        }
        return password
    }

    private fun getEmail(): String? {
        val email = binding.edLoginEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.edLoginEmail.apply {
                error = resources.getString(R.string.email_cannot_empty)
                requestFocus()
            }
            return null
        }


        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edLoginEmail.apply {
                error = resources.getString(R.string.valid_email)
                requestFocus()
            }
            return null
        }


        return email

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_REQ_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("test,",account.email.toString())
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                viewModelRegister.signInWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun observeSaveUserInformation(){
        viewModelRegister.saveUserInformationGoogleSignIn.observe(this, Observer { response->
            when(response){
                is Resource.Loading -> {
                    Log.d(TAG,"GoogleSignIn:Loading")
                    binding.btnLoginActivity.startAnimation()
                    return@Observer
                }

                is Resource.Success -> {
                    Log.d(TAG,"GoogleSignIn:Successful")
                    binding.btnLoginActivity.stopAnimation()
                    startActivity(Intent(this, ShoppingActivity::class.java))
                    finish()
                    return@Observer
                }

                is Resource.Error ->{
                    Log.e(TAG,"GoogleSignIn:Error ${response.message.toString()}")
                    ToastUtils.showMessage(this@LoginActivity,getString(R.string.error_occurred))
                    return@Observer
                }
                else -> Unit
            }

        })
    }

}