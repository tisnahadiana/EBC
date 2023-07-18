package id.deeromptech.ebc.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import id.deeromptech.ebc.data.local.LoginPreferences
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val loginPreferences: LoginPreferences
) : ViewModel() {
    fun checkIfFirstTime(): LiveData<Boolean> {
        return loginPreferences.isFirstTime().asLiveData()
    }
}