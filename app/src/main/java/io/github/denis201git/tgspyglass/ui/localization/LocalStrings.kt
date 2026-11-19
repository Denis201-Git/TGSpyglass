package io.github.denis201git.tgspyglass.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf


val LocalStrings =
    staticCompositionLocalOf<AppStrings> {

        EnglishStrings

    }


@Composable
fun currentStrings(
    language: AppLanguage
): AppStrings {

    return when (language) {

        AppLanguage.ENGLISH ->
            EnglishStrings

        AppLanguage.RUSSIAN ->
            RussianStrings

    }

}