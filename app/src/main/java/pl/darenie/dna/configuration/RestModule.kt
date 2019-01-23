package pl.darenie.dna.configuration

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import pl.darenie.dna.service.DareInstanceIDService
import pl.darenie.dna.ui.main.MainActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import javax.inject.Singleton

@Module
open class RestModule {

//    private val API_BASE_URL = "http://10.0.2.2:8080/dns-rest-api/"
//    private val API_BASE_URL = "http://localhost:8080/dns-rest-api/"
    private val API_BASE_URL = AppConstants.API_URL

    private lateinit var sharedPreferences : SharedPreferences

    @Provides
    @Singleton
    open fun provideHttpCache(application: Application) : Cache {
        val cacheSize = 10 * 1024 * 1024
        sharedPreferences = application.getSharedPreferences(ApplicationParameter.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        return Cache(application.cacheDir, cacheSize.toLong())
    }

    @Provides
    @Singleton
    open fun provideGson() : Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    }

    @Provides
    @Singleton
    open fun provideOkhttpClient(cache: Cache) : OkHttpClient {
        return OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor({ chain ->
                    val request = chain.request().newBuilder()
                            .addHeader(ApplicationParameter.TOKEN_HEADER, sharedPreferences.getString(ApplicationParameter.TOKEN_HEADER,""))
                            .build()
                    chain.proceed(request)
                })
                .build()
    }

    @Provides
    @Singleton
    open fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .build()
    }


}