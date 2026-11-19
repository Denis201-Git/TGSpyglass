package io.github.denis201git.tgspyglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.denis201git.tgspyglass.navigation.AppNavGraph
import io.github.denis201git.tgspyglass.ui.localization.LocalStrings
import io.github.denis201git.tgspyglass.ui.localization.currentStrings
import io.github.denis201git.tgspyglass.ui.theme.TGParserTheme
import io.github.denis201git.tgspyglass.viewmodel.LanguageViewModel
import io.github.denis201git.tgspyglass.viewmodel.ThemeViewModel
import androidx.compose.runtime.CompositionLocalProvider
import io.github.denis201git.tgspyglass.data.preferences.ThemePreferences
import io.github.denis201git.tgspyglass.ui.theme.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private fun applyInitialTheme() {
        val theme = try {
            runBlocking { ThemePreferences(applicationContext).theme.first() }
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }

        setTheme(
            when (theme) {
                AppTheme.LIGHT -> R.style.SplashTheme_Light
                AppTheme.DARK -> R.style.SplashTheme_Dark
                AppTheme.AMOLED -> R.style.SplashTheme_Amoled
                AppTheme.SYSTEM -> R.style.SplashTheme_System
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyInitialTheme()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val themeViewModel: ThemeViewModel = viewModel()

            val languageViewModel: LanguageViewModel = viewModel()


            val theme =
                themeViewModel.theme.collectAsState().value


            val language =
                languageViewModel.language.collectAsState().value


            val strings =
                currentStrings(
                    language = if (language == "ru")
                        io.github.denis201git.tgspyglass.ui.localization.AppLanguage.RUSSIAN
                    else
                        io.github.denis201git.tgspyglass.ui.localization.AppLanguage.ENGLISH
                )


            TGParserTheme(
                appTheme = theme
            ) {

                CompositionLocalProvider(

                    LocalStrings provides strings

                ) {

                    AppNavGraph()

                }

            }

        }

    }

}