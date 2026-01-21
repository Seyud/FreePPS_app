package me.freepps.tile.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import me.freepps.tile.R
import me.freepps.tile.icon.Info
import me.freepps.tile.icon.MiuixIcons
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun RootPermissionDialog(
    showDialog: MutableState<Boolean>,
    onCheckPermission: () -> Unit
) {
    SuperDialog(
        title = stringResource(R.string.root_permission_check),
        summary = stringResource(R.string.root_permission_summary),
        show = showDialog,
        onDismissRequest = { }
    ) {
        TextButton(
            text = stringResource(R.string.check_permission),
            onClick = onCheckPermission,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary()
        )
    }
}

private fun isRootAvailable(): Boolean {
    return try {
        val process = Runtime.getRuntime().exec("su -c whoami")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val line = reader.readLine()
        line != null && line.trim().equals("root", ignoreCase = true)
    } catch (e: Exception) {
        false
    }
}

private fun shouldShowTileHint(context: Context): Boolean {
    val prefs = context.getSharedPreferences("freepps_prefs", Context.MODE_PRIVATE)
    return !prefs.getBoolean("tile_hint_shown", false)
}

private fun showTileHint(context: Context) {
    Toast.makeText(context, context.getString(R.string.add_tile_hint), Toast.LENGTH_LONG).show()
    Toast.makeText(context, context.getString(R.string.toggle_tile_hint), Toast.LENGTH_LONG).show()
    
    val prefs = context.getSharedPreferences("freepps_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("tile_hint_shown", true).apply()
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    var showAboutScreen by remember { mutableStateOf(false) }

    var notificationStatus by remember { mutableStateOf(context.getString(R.string.pending_check)) }
    var rootStatus by remember { mutableStateOf(context.getString(R.string.pending_check)) }
    var showRootDialog: MutableState<Boolean> = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationStatus = if (isGranted) context.getString(R.string.granted) else context.getString(R.string.denied)
        if (!showRootDialog.value && shouldShowTileHint(context)) {
            showTileHint(context)
        }
    }

    fun checkPermissions() {
        notificationStatus = if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) context.getString(R.string.granted) else context.getString(R.string.not_granted)

        rootStatus = if (isRootAvailable()) context.getString(R.string.enabled) else context.getString(R.string.disabled)
    }

    LaunchedEffect(Unit) {
        val rootAvailable = isRootAvailable()
        rootStatus = if (rootAvailable) context.getString(R.string.enabled) else context.getString(R.string.disabled)
        showRootDialog.value = !rootAvailable

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationStatus = context.getString(R.string.granted)
        } else {
            notificationStatus = context.getString(R.string.pending_auth)
        }

        if (!showRootDialog.value && shouldShowTileHint(context)) {
            showTileHint(context)
        }
    }

    // 关于页面动画
    AnimatedVisibility(
        visible = showAboutScreen,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        AboutScreen(onBack = { showAboutScreen = false })
    }

    // 主页面动画
    AnimatedVisibility(
        visible = !showAboutScreen,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.app_name),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = { showAboutScreen = true },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Info,
                            contentDescription = stringResource(R.string.about)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SmallTitle(text = stringResource(R.string.permission_info))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    BasicComponent(
                        title = stringResource(R.string.notification_permission),
                        summary = notificationStatus
                    )
                    BasicComponent(
                        title = stringResource(R.string.root_permission),
                        summary = rootStatus
                    )
                }
            }

            item {
                SmallTitle(text = stringResource(R.string.operations))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        text = stringResource(R.string.refresh_permission_status),
                        onClick = { 
                            checkPermissions()
                            Toast.makeText(context, context.getString(R.string.permission_updated), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }

            item {
                SmallTitle(text = stringResource(R.string.usage_guide))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    BasicComponent(
                        title = stringResource(R.string.add_tile),
                        summary = stringResource(R.string.add_tile_hint)
                    )
                    BasicComponent(
                        title = stringResource(R.string.toggle_status),
                        summary = stringResource(R.string.toggle_tile_hint)
                    )
                }
            }
        }
    }
    }

    RootPermissionDialog(
        showDialog = showRootDialog,
        onCheckPermission = {
            checkPermissions()
            if (rootStatus == context.getString(R.string.enabled)) {
                showRootDialog.value = false
            }
        }
    )
}
