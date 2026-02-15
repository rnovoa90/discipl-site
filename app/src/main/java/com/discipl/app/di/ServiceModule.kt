package com.discipl.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * All services use constructor injection with @Singleton + @Inject,
 * so Hilt provides them automatically. This module exists for any
 * future bindings that can't use constructor injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule
