package com.notionwidgets.di

import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.provider.notion.NotionApiService
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(authManager: Lazy<AuthManager>): Interceptor {
        return Interceptor { chain ->
            val token = authManager.get().getAccessToken()
            val request = chain.request().newBuilder().apply {
                addHeader("Notion-Version", "2022-06-28")
                if (token != null) {
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(authManager: Lazy<AuthManager>): Authenticator {
        return Authenticator { _, response ->
            if (response.code == 401) {
                authManager.get().onTokenExpired()
            }
            null
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        tokenAuthenticator: Authenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.notion.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNotionApiService(retrofit: Retrofit): NotionApiService {
        return retrofit.create(NotionApiService::class.java)
    }
}
