package com.nagopy.kmp.habittracker.di

import com.nagopy.kmp.habittracker.data.dataModule
import com.nagopy.kmp.habittracker.domain.domainModule
import com.nagopy.kmp.habittracker.presentation.presentationModule
import org.koin.dsl.module

/**
 * Main DI module that aggregates all layer modules
 */
val appModule = module {
    includes(dataModule, domainModule, presentationModule)
}