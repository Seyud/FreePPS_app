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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import me.freepps.tile.icon.Info
import me.freepps.tile.icon.MiuixIcons
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    var showAboutScreen by remember { mutableStateOf(false) }

    var notificationStatus by remember { mutableStateOf("待检测") }
    var rootStatus by remember { mutableStateOf("待检测") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationStatus = if (isGranted) "已授予" else "被拒绝"
        if (isGranted) {
            showTileHint(context)
        }
    }

    // 初始化检测
    LaunchedEffect(Unit) {
        // 检测通知权限
        notificationStatus = if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 只在首次进入时显示提示
            if (shouldShowTileHint(context)) {
                showTileHint(context)
            }
            "已授予"
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            "待授权"
        }

        // 检测 Root 权限
        rootStatus = if (isRootAvailable()) "已启用" else "未启用"
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
                title = "FreePPS",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = { showAboutScreen = true },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Info,
                            contentDescription = "关于"
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
                SmallTitle(text = "权限信息")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    BasicComponent(
                        title = "通知权限",
                        summary = notificationStatus
                    )
                    BasicComponent(
                        title = "Root 权限",
                        summary = rootStatus
                    )
                }
            }

            item {
                SmallTitle(text = "操作")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        text = "检查权限状态",
                        onClick = {
                            // 重新检测通知权限
                            notificationStatus = if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) "已授予" else "未授予"

                            // 重新检测 Root 权限
                            rootStatus = if (isRootAvailable()) "已启用" else "未启用"

                            Toast.makeText(context, "权限已更新", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                SmallTitle(text = "使用说明")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    BasicComponent(
                        title = "添加磁贴",
                        summary = "请在控制中心下拉面板中添加 FreePPS 磁贴"
                    )
                    BasicComponent(
                        title = "切换状态",
                        summary = "点击磁贴以切换 PPS 支持状态"
                    )
                }
            }
        }
    }
    }  // 关闭AnimatedVisibility
}

private fun shouldShowTileHint(context: Context): Boolean {
    val prefs = context.getSharedPreferences("freepps_prefs", Context.MODE_PRIVATE)
    return !prefs.getBoolean("tile_hint_shown", false)
}

private fun showTileHint(context: Context) {
    Toast.makeText(context, "请在控制中心下拉面板中添加 FreePPS 磁贴", Toast.LENGTH_LONG).show()
    Toast.makeText(context, "点击磁贴以切换 PPS 支持状态", Toast.LENGTH_LONG).show()
    
    // 标记为已显示
    val prefs = context.getSharedPreferences("freepps_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("tile_hint_shown", true).apply()
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
