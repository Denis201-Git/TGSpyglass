package com.example.tgparser.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.tgparser.R

object AppIcons {
    @Composable fun Add(): ImageVector = ImageVector.vectorResource(R.drawable.ic_add)
    @Composable fun Close(): ImageVector = ImageVector.vectorResource(R.drawable.ic_close)
    @Composable fun Delete(): ImageVector = ImageVector.vectorResource(R.drawable.ic_delete)
    @Composable fun Refresh(): ImageVector = ImageVector.vectorResource(R.drawable.ic_refresh)
    @Composable fun Settings(): ImageVector = ImageVector.vectorResource(R.drawable.ic_settings)
    @Composable fun ArrowBack(): ImageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back)
    @Composable fun KeyboardArrowLeft(): ImageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_left)
    @Composable fun KeyboardArrowRight(): ImageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_right)
    @Composable fun Check(): ImageVector = ImageVector.vectorResource(R.drawable.ic_check)
    @Composable fun Palette(): ImageVector = ImageVector.vectorResource(R.drawable.ic_palette)
    @Composable fun Language(): ImageVector = ImageVector.vectorResource(R.drawable.ic_language)
    @Composable fun VideoLibrary(): ImageVector = ImageVector.vectorResource(R.drawable.ic_video_library)
    @Composable fun Code(): ImageVector = ImageVector.vectorResource(R.drawable.ic_code)
}
