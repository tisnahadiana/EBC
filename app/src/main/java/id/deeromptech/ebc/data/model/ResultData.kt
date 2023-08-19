package id.deeromptech.ebc.data.model

sealed class ResultData<out T> {
    data class Success<out T>(val data: T? = null): ResultData<T>()
    data class Loading(val nothing: Nothing? = null): ResultData<Nothing>()
    data class Failed(val message: String? = null):ResultData<Nothing>()
    data class Exception(val message: String? = null): ResultData<Nothing>()
}
