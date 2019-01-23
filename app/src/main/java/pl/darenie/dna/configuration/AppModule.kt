package pl.darenie.dna.configuration

import android.app.Application
import dagger.Module
import dagger.Provides
import pl.darenie.dna.service.DareInstanceIDService
import javax.inject.Singleton

@Module class AppModule(val app: App) {

    @Provides
    @Singleton
    fun provideApp() : Application {
        return app
    }
}
