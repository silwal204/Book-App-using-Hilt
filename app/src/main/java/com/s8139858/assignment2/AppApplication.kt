package com.s8139858.assignment2

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point that boots the Hilt dependency graph.
 *
 * @HiltAndroidApp triggers Hilt's code generation (via KSP) and creates the
 * application-scoped component that owns every @Singleton. Registered in the
 * manifest via android:name — without this, no injection would work anywhere.
 */
@HiltAndroidApp
class AppApplication : Application()
