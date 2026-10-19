package com.s8139858.assignment2.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Qualifies the [kotlinx.coroutines.Dispatchers.IO] dispatcher for injection. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Qualifies the [kotlinx.coroutines.Dispatchers.Default] dispatcher for injection. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Provides coroutine dispatchers so production and test code can inject
 * schedulers instead of referencing [Dispatchers] directly.
 *
 * Custom @Qualifier annotations (@IoDispatcher / @DefaultDispatcher) let Hilt tell
 * apart two dependencies of the same type (CoroutineDispatcher). In unit tests a
 * TestDispatcher is passed to the repository constructor instead, making
 * coroutine timing fully deterministic.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
