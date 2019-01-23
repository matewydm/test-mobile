package pl.darenie.dna.test

//package pl.darenie.dna
//
//import android.content.SharedPreferences
//import com.google.gson.Gson
//import dagger.Provides
//import okhttp3.OkHttpClient
//import org.mockito.Mockito.`when`
//import org.mockito.Mockito.mock
//import pl.darenie.dna.configuration.RestModule
//import retrofit2.Retrofit
//import javax.inject.Singleton
//
//class MockRestModule : RestModule() {
//    private val API_BASE_URL = "http://192.168.1.7:8080/dns-rest-api/"
//
//    @Provides
//    @Singleton
//    override fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
//        return mock(Retrofit::class.java)
//    }
//
//}