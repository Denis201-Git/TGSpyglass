package io.github.denis201git.tgspyglass.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}