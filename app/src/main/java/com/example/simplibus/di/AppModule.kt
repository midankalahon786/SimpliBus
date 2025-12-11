package com.example.simplibus.di

import com.example.simplibus.data.passenger.BusRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/*
 * This file is a placeholder to show best practice.
 * To make this work, you would add Hilt dependencies to your build.gradle
 * and an @HiltAndroidApp annotation to your Application class.
 *
 * For now, you can just create these objects manually in your MapsActivity,
 * but this 'di' (Dependency Injection) structure is what you should aim for.
 */

// @Module
// @InstallIn(SingletonComponent::class)
object AppModule {

    // @Singleton
    // @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // @Singleton
    // @Provides
    fun provideBusRepository(client: OkHttpClient): BusRepository {
        return BusRepository(client)
    }
}
