package com.nagopy.kmp.habittracker

import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString

class Greeting {
    private val platform = getPlatform()

    suspend fun greet(): String {
        return getString(Res.string.hello_platform, platform.name)
    }
}