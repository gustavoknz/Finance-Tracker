package dev.gustavo.groceries

import android.app.Application
import dev.gustavo.groceries.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class GroceryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@GroceryApp)
        }
    }
}
