package dev.gustavo.finance

import android.app.Application
import dev.gustavo.finance.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@FinanceApp)
        }
    }
}
