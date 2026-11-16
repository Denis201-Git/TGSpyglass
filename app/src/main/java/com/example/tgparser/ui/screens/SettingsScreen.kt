package com.example.tgparser.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import com.example.tgparser.ui.components.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tgparser.utils.AppInfo
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.tgparser.ui.theme.AppTheme
import com.example.tgparser.viewmodel.ThemeViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.RadioButton
import androidx.compose.ui.unit.dp
import com.example.tgparser.viewmodel.LanguageViewModel
import com.example.tgparser.viewmodel.MediaSettingsViewModel
import com.example.tgparser.viewmodel.MessageStyleViewModel
import com.example.tgparser.data.preferences.MessageStyle
import androidx.compose.material3.MaterialTheme
import com.example.tgparser.ui.localization.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    themeViewModel: ThemeViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
    mediaSettingsViewModel: MediaSettingsViewModel = viewModel(),
    messageStyleViewModel: MessageStyleViewModel = viewModel(),
) {

    val currentTheme by themeViewModel.theme.collectAsState()
    val currentLanguage by languageViewModel.language.collectAsState()
    val currentMediaSettings by mediaSettingsViewModel.settings.collectAsState()
    val currentMessageStyle by messageStyleViewModel.style.collectAsState()
    val strings = LocalStrings.current

    var showThemeDialog by remember {
        mutableStateOf(false)
    }

    var showLanguageDialog by remember {
        mutableStateOf(false)
    }

    var showMediaDialog by remember {
        mutableStateOf(false)
    }

    var showMessagesStyleDialog by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text(strings.settings)
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            AppIcons.ArrowBack(),
                            contentDescription = "Back"
                        )

                    }

                }

            )

        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
        ) {

            item {

                ListItem(

                    headlineContent = {
                        Text(strings.appearance)
                    },

                    supportingContent = {
                        Text(strings.themeSettings)
                    },

                    modifier = Modifier.clickable {
                        showThemeDialog = true
                    },

                    leadingContent = {

                        Icon(
                            imageVector = AppIcons.Palette(),
                            contentDescription = null
                        )

                    }

                )

            }


            item {

                ListItem(

                    headlineContent = {
                        Text(strings.messagesStyle)
                    },

                    supportingContent = {
                        Text(
                            when (currentMessageStyle) {
                                MessageStyle.CLASSIC ->
                                    strings.messagesStyleClassic

                                MessageStyle.BORDERLESS ->
                                    strings.messagesStyleBorderless
                            }
                        )
                    },

                    leadingContent = {

                        Icon(
                            imageVector = AppIcons.Chat(),
                            contentDescription = null
                        )

                    },

                    modifier = Modifier.clickable {
                        showMessagesStyleDialog = true
                    }

                )

            }


            item {

                ListItem(

                    headlineContent = {
                        Text(strings.language)
                    },

                    supportingContent = {

                        Text(

                            if (currentLanguage == "ru")
                                "Русский"
                            else
                                "English"

                        )

                    },

                    leadingContent = {

                        Icon(
                            imageVector = AppIcons.Language(),
                            contentDescription = null
                        )

                    },

                    modifier = Modifier.clickable {

                        showLanguageDialog = true

                    },

                )

            }


            item {

                ListItem(

                    headlineContent = {
                        Text(strings.downloadMedia)
                    },

                    supportingContent = {
                        Text(strings.photosVideosFiles)
                    },

                    leadingContent = {

                        Icon(
                            imageVector = AppIcons.VideoLibrary(),
                            contentDescription = null
                        )

                    },

                    modifier = Modifier.clickable {

                        showMediaDialog = true

                    }

                )

            }


            item {

                HorizontalDivider()

            }


            item {

                ListItem(

                    headlineContent = {
                        Text("TGParser")
                    },

                    supportingContent = {
                        Text(strings.version+" "+AppInfo.VERSION)
                    },

                    leadingContent = {

                        Icon(
                            imageVector = AppIcons.Settings(),
                            contentDescription = null
                        )

                    }

                )


                ListItem(

                    headlineContent = {
                        Text(strings.github)
                    },

                    supportingContent = {
                        Text(AppInfo.GITHUB_URL)
                    },

                    leadingContent = {

                        Icon(
                            imageVector = AppIcons.Code(),
                            contentDescription = null
                        )

                    },

                    modifier = Modifier.clickable {

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            AppInfo.GITHUB_URL.toUri()
                        )

                        context.startActivity(intent)

                    }

                )

            }

        }

    }

    if (showLanguageDialog) {

        ModalBottomSheet(

            onDismissRequest = {
                showLanguageDialog = false
            }

        ) {

            Text(
                text = strings.chooseLanguage,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )


            listOf(

                "English" to "en",

                "Русский" to "ru"

            ).forEach { language ->


                ListItem(

                    headlineContent = {

                        Text(
                            text = language.first,
                            color = if (currentLanguage == language.second)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )

                    },

                    trailingContent = {

                        if (currentLanguage == language.second) {

                            Icon(
                                imageVector = AppIcons.Check(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                        }

                    },

                    modifier = Modifier.clickable {

                        languageViewModel.setLanguage(
                            language.second
                        )

                        showLanguageDialog = false

                    }

                )

            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )

        }

    }

    if (showThemeDialog) {

        ModalBottomSheet(
            onDismissRequest = {
                showThemeDialog = false
            }
        ) {

            Text(
                text = "Choose theme",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )


            AppTheme.entries.forEach { theme ->

                ListItem(

                    headlineContent = {
                        Text(
                            when(theme) {
                                AppTheme.SYSTEM ->
                                    strings.systemTheme

                                AppTheme.LIGHT ->
                                    strings.lightTheme

                                AppTheme.DARK ->
                                    strings.darkTheme

                                AppTheme.AMOLED ->
                                    strings.amoledTheme
                            }
                        )
                    },

                    trailingContent = {

                        RadioButton(

                            selected = currentTheme == theme,

                            onClick = {

                                themeViewModel.setTheme(theme)
                                showThemeDialog = false

                            }

                        )

                    },

                    modifier = Modifier.clickable {

                        themeViewModel.setTheme(theme)
                        showThemeDialog = false

                    }

                )

            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

        }

    }

    if (showMessagesStyleDialog) {

        ModalBottomSheet(
            onDismissRequest = {
                showMessagesStyleDialog = false
            }
        ) {

            Text(
                text = strings.chooseMessagesStyle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            MessageStyle.entries.forEach { style ->

                ListItem(

                    headlineContent = {
                        Text(
                            when (style) {
                                MessageStyle.CLASSIC ->
                                    strings.messagesStyleClassic

                                MessageStyle.BORDERLESS ->
                                    strings.messagesStyleBorderless
                            }
                        )
                    },

                    trailingContent = {

                        RadioButton(
                            selected = currentMessageStyle == style,
                            onClick = {
                                messageStyleViewModel.setStyle(style)
                                showMessagesStyleDialog = false
                            }
                        )

                    },

                    modifier = Modifier.clickable {
                        messageStyleViewModel.setStyle(style)
                        showMessagesStyleDialog = false
                    }

                )

            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

        }

    }

    if (showMediaDialog) {

        ModalBottomSheet(
            onDismissRequest = {
                showMediaDialog = false
            }
        ) {

            Text(
                text = strings.downloadMedia,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            ListItem(

                headlineContent = {
                    Text(strings.photos)
                },

                trailingContent = {

                    Checkbox(
                        checked = currentMediaSettings.photos,
                        onCheckedChange = {
                            mediaSettingsViewModel.setPhotos(it)
                        }
                    )

                },

                modifier = Modifier.clickable {
                    mediaSettingsViewModel.setPhotos(
                        !currentMediaSettings.photos
                    )
                }

            )

            ListItem(

                headlineContent = {
                    Text(strings.videos)
                },

                trailingContent = {

                    Checkbox(
                        checked = currentMediaSettings.videos,
                        onCheckedChange = {
                            mediaSettingsViewModel.setVideos(it)
                        }
                    )

                },

                modifier = Modifier.clickable {
                    mediaSettingsViewModel.setVideos(
                        !currentMediaSettings.videos
                    )
                }

            )

            ListItem(

                headlineContent = {
                    Text(strings.files)
                },

                trailingContent = {

                    Checkbox(
                        checked = currentMediaSettings.files,
                        onCheckedChange = {
                            mediaSettingsViewModel.setFiles(it)
                        }
                    )

                },

                modifier = Modifier.clickable {
                    mediaSettingsViewModel.setFiles(
                        !currentMediaSettings.files
                    )
                }

            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

        }

    }
}
