package es.jarroyo.tddweatherapp.data.source.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.microhealth.lmc.utils.NetworkSystemAbstract
import es.jarroyo.tddweatherapp.BuildConfig
import es.jarroyo.tddweatherapp.domain.model.Response
import es.jarroyo.tddweatherapp.domain.model.currentWeather.CurrentWeather
import es.jarroyo.tddweatherapp.domain.model.forecast.Forecast
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class NetworkDataSource(private val networkSystem: NetworkSystemAbstract) : INetworkDataSource(networkSystem) {


    private fun initRetrofitOpenWateherAPI(): OpenWeatherAPI {
        val retrofit = Retrofit.Builder().apply {
            baseUrl(BuildConfig.OPEN_WEATHER_API_URL_BASE)
            client(okHttpClient)
            addConverterFactory(GsonConverterFactory.create())
            addCallAdapterFactory(CoroutineCallAdapterFactory())
        }.build()

        val openWeatherAPI = retrofit.create(OpenWeatherAPI::class.java)
        return openWeatherAPI
    }

    /**
     * GET CURRENT WEATHER BY ID
     */
    override suspend fun getCurrentWeatherByName(cityName:String): Response<CurrentWeather> {
        val openWeatherAPI = initRetrofitOpenWateherAPI()
        try {
            val currentWeather =
                openWeatherAPI.currentWeatherByName(cityName)
                    .await()

            return Response.Success(currentWeather)
        } catch (e: Exception) {
            return Response.Error(e)
        }
    }

    /**
     * FORECAST
     */
    override suspend fun getForecast(cityName: String): Response<Forecast> {
        val openWeatherAPI = initRetrofitOpenWateherAPI()
        try {
            val forecast =
                openWeatherAPI.forecastByName(cityName)
                    .await()

            return Response.Success(forecast)
        } catch (e: Exception) {
            return Response.Error(e)
        }
    }

    var okHttpClient = OkHttpClient.Builder()
        .addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val request = chain.request()
                val response = chain.proceed(request)

                // todo deal with the issues the way you need to
                if (response.code() == 500) {
                    return response
                }

                return response
            }
        })
        .build()

}