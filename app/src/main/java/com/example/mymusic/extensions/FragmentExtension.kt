package com.example.mymusic.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

inline val Fragment.safeActivity: FragmentActivity
    get() = activity ?: throw IllegalStateException("Fragment not attached")

fun <T> Fragment.argument(name: String): T {
    return arguments?.get(name) as? T ?: throw IllegalStateException("Argument $name not found.")
}

fun <T> Fragment.argumentOr(name: String, default: T): T {
    return arguments?.get(name) as? T ?: default
}

fun Fragment.argumentOrEmpty(name: String): String {
    return arguments?.get(name) as? String ?: ""
}