package com.s8139858.assignment2.di

import com.s8139858.assignment2.data.repository.AppRepositoryImpl
import com.s8139858.assignment2.domain.repository.AppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their implementations.
 *
 * @Binds is used (instead of @Provides) because Hilt only needs to map the
 * interface to a concrete @Inject-annotated class — no factory body required.
 * This indirection is what lets unit tests swap in a fake repository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository
}
