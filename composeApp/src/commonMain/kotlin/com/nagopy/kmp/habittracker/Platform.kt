package com.nagopy.kmp.habittracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform