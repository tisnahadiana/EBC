package id.deeromptech.ebc.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoginPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    fun isFirstTime(): Flow<Boolean> {
        return dataStore.data.map {
            it[FIRST_TIME_KEY] ?: true
        }
    }

    suspend fun setFirstTime(firstTime: Boolean) {
        dataStore.edit {
            it[FIRST_TIME_KEY] = firstTime
        }
    }

    companion object {
        private val FIRST_TIME_KEY = booleanPreferencesKey("first_time")
    }
}