package com.example.tgparser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tgparser.navigation.AppNavGraph
import com.example.tgparser.ui.localization.LocalStrings
import com.example.tgparser.ui.localization.currentStrings
import com.example.tgparser.ui.theme.TGParserTheme
import com.example.tgparser.viewmodel.LanguageViewModel
import com.example.tgparser.viewmodel.ThemeViewModel
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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
                        com.example.tgparser.ui.localization.AppLanguage.RUSSIAN
                    else
                        com.example.tgparser.ui.localization.AppLanguage.ENGLISH
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