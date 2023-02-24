/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hippo.ehviewer.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.hippo.ehviewer.EhApplication
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import rikka.core.res.resolveColor
import rikka.material.app.MaterialActivity

abstract class EhActivity : MaterialActivity() {
    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        theme.applyStyle(getThemeStyleRes(this), true)
    }

    override fun computeUserThemeKey(): String {
        return getTheme(this)
    }

    @StyleRes
    fun getThemeStyleRes(context: Context): Int {
        return if (THEME_BLACK == getTheme(context)) {
            R.style.ThemeOverlay_Black
        } else R.style.ThemeOverlay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as EhApplication).registerActivity(this)
    }

    override fun onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars()
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.post {
            val rootWindowInsets = window.decorView.rootWindowInsets
            if (rootWindowInsets != null && rootWindowInsets.systemWindowInsetBottom >= Resources.getSystem().displayMetrics.density * 40) {
                window.navigationBarColor =
                    theme.resolveColor(android.R.attr.navigationBarColor) and 0x00ffffff or -0x20000000
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            } else {
                window.navigationBarColor = Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as EhApplication).unregisterActivity(this)
    }

    override fun onResume() {
        super.onResume()
        if (Settings.getEnabledSecurity()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        Settings.putNotificationRequired()
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (Settings.getNotificationRequired())
            return
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private const val THEME_DEFAULT = "DEFAULT"
        private const val THEME_BLACK = "BLACK"
        private val isBlackNightTheme: Boolean
            get() = Settings.getBoolean("black_dark_theme", false)

        fun getTheme(context: Context): String {
            return if (isBlackNightTheme && isNightMode(context.resources.configuration)) THEME_BLACK else THEME_DEFAULT
        }

        private fun isNightMode(configuration: Configuration): Boolean {
            return configuration.uiMode and Configuration.UI_MODE_NIGHT_YES > 0
        }
    }
}