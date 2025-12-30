package com.evening.dailylife.app.di

import com.evening.dailylife.app.navigation.AppWidgetNavigationProvider
import com.evening.dailylife.core.ui.navigation.WidgetNavigationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindWidgetNavigationProvider(
        provider: AppWidgetNavigationProvider
    ): WidgetNavigationProvider
}
