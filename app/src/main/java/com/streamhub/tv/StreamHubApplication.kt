package com.streamhub.tv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point - bootstraps Hilt's dependency graph. */
@HiltAndroidApp
class StreamHubApplication : Application()
