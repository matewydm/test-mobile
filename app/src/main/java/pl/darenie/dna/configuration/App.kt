package pl.darenie.dna.configuration

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.facebook.FacebookSdk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App : Application() {

    var restComponent: RestComponent? = null

    override fun onCreate() {
        super.onCreate()
        restComponent = DaggerRestComponent.builder()
            .appModule(AppModule(this))
            .restModule(RestModule())
            .build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}