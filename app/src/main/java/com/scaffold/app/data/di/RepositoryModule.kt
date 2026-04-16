package com.scaffold.app.data.di

import com.scaffold.app.data.repositories.AuthRepositoryImpl
import com.scaffold.app.data.repositories.SampleRepositoryImpl
import com.scaffold.app.domain.repositories.AuthRepository
import com.scaffold.app.domain.repositories.SampleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSampleRepository(impl: SampleRepositoryImpl): SampleRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
