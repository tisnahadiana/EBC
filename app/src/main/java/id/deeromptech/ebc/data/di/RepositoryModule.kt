package id.deeromptech.ebc.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import id.deeromptech.ebc.data.network.ApiRajaOngkir
import id.deeromptech.ebc.data.repositories.DataRepository

@InstallIn(ActivityRetainedComponent::class)
@Module
object RepositoryModule {
    @Provides
    fun providesDataRepository(apiRajaOngkir: ApiRajaOngkir): DataRepository {
        return DataRepository(apiRajaOngkir)
    }
}