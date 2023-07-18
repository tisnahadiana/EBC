package id.deeromptech.ebc.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.LoginPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val loginPreferences: LoginPreferences
) : ViewModel() {
    fun setFirstTime(firstTime: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            loginPreferences.setFirstTime(firstTime)
        }
    }
}