package me.freepps.tile

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class RunFreePPSTileService : TileService() {

    private val prefsFileName = "tile_prefs"
    private val tileStateKey = "tile_state"
    private val tileTimeKey = "tile_time"
    private val notificationClosedKey = "notification_closed"
    private val channelId = "freepps_status_channel"

    override fun onStartListening() {
        super.onStartListening()
        try {
            // 实时查询 FreePPS 状态
            val statusMessage = updateTileStateFromFile()
            // 只有在通知未被用户手动关闭时才显示
            if (!isNotificationClosed()) {
                updateNotification(statusMessage)
            }
        } catch (e: Exception) {
            Log.e("onStartListening", "Failed to start listening and restore tile state", e)
            handleError(e, "Failed to start listening and restore tile state")
        }
    }

    override fun onClick() {
        try {
            toggleTileState()
        } catch (e: Exception) {
            Log.e("onClick", "Failed to toggle tile state", e)
            handleError(e, "Failed to toggle tile state")
        }
    }

    private fun toggleTileState() {
        try {
            executeScript("sh /data/adb/modules/FreePPS/action.sh")
            checkAndUpdateFreePPSStatus()
        } catch (e: Exception) {
            Log.e("toggleTileState", "Error toggling tile state", e)
        }
    }
    
    private fun executeScript(script: String) {
        val process = ProcessBuilder("su", "-c", script).start()
        process.waitFor()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return try {
            intent?.let {
                when (it.action) {
                    "me.freepps.tile.FREEPPS_RUN_SCRIPT" -> {
                        checkAndUpdateFreePPSStatus()
                    }
                    "me.freepps.tile.FREEPPS_UPDATE" -> {
                        executeScript("sh /data/adb/modules/FreePPS/action.sh")
                        checkAndUpdateFreePPSStatus()
                    }
                    "me.freepps.tile.FREEPPS_ON" -> {
                        onClick()
                    }
                    "me.freepps.tile.RESTORE_TILE_STATE" -> {
                        restoreTileStateAndNotification()
                    }
                    "me.freepps.tile.FREEPPS_REFRESH_STATE" -> {
                        refreshTileAndNotification()
                    }
                    else -> {
                        Log.w("OnStartCommand", "Unknown action: ${it.action}")
                    }
                }
            } ?: Log.w("OnStartCommand", "Intent was null")

            super.onStartCommand(intent, flags, startId)
        } catch (e: Exception) {
            Log.e("OnStartCommandError", "Failed to process onStartCommand", e)
            super.onStartCommand(intent, flags, startId)
        }
    }

    fun checkAndUpdateFreePPSStatus() {
        try {
            val freeValue = readFreeValue()
            val newState = when (freeValue) {
                "1" -> {
                    showToast(getString(R.string.pps_locked))
                    Tile.STATE_ACTIVE
                }
                else -> {
                    showToast(getString(R.string.pps_paused))
                    Tile.STATE_INACTIVE
                }
            }
            
            qsTile.state = newState
            val currentTime = getCurrentTime()
            saveTileState(newState, currentTime)
            qsTile.updateTile()
            // 状态改变时重新启用通知并显示
            setNotificationClosed(false)
            updateNotification(getLastToastMessage())
        } catch (e: Exception) {
            Log.e("checkAndUpdateFreePPSStatus", "Error checking freepps status", e)
            handleError(e, "Error checking freepps status")
        }
    }

    private fun updateTileStateFromFile(): String {
        return try {
            val freeValue = readFreeValue()
            val (newState, statusMessage) = when (freeValue) {
                "1" -> Pair(Tile.STATE_ACTIVE, getString(R.string.pps_locked))
                else -> Pair(Tile.STATE_INACTIVE, getString(R.string.pps_paused))
            }
            
            qsTile.state = newState
            val currentTime = getCurrentTime()
            saveTileState(newState, currentTime)
            qsTile.updateTile()
            
            // 保存当前状态消息供通知使用
            saveStatusMessage(statusMessage)
            
            Log.d("updateTileStateFromFile", "Tile state updated: $newState (free=$freeValue)")
            statusMessage
        } catch (e: Exception) {
            Log.e("updateTileStateFromFile", "Error updating tile state from file", e)
            // 如果读取失败，加载上次保存的状态作为后备
            val (savedState) = loadTileState()
            qsTile.state = savedState
            qsTile.updateTile()
            getLastToastMessage()
        }
    }
    
    private fun saveStatusMessage(message: String) {
        try {
            val sharedPreferences = getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("last_toast_message", message).apply()
        } catch (e: Exception) {
            Log.e("saveStatusMessage", "Error saving status message", e)
        }
    }

    private fun readFreeValue(): String {
        return try {
            val process = ProcessBuilder("su", "-c", "cat /data/adb/modules/FreePPS/free").start()
            val reader = process.inputStream.bufferedReader()
            val line = reader.readLine() ?: "0"
            process.waitFor()
            line.trim()
        } catch (e: Exception) {
            Log.e("readFreeValue", "Error reading free value", e)
            "0"
        }
    }

    private fun handleError(e: Exception, message: String) {
        Log.e("FreePPS", message, e)
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
        saveTileState(qsTile.state, getCurrentTime())
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            val sharedPreferences = getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("last_toast_message", message).apply()
        } catch (e: Exception) {
            Log.e("ShowToastError", "Failed to show toast message: $message", e)
        }
    }

    private fun getLastToastMessage(): String {
        return try {
            getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
                .getString("last_toast_message", "") ?: ""
        } catch (e: Exception) {
            Log.e("getLastToastMessage", "Error fetching last toast message", e)
            ""
        }
    }

    private fun saveTileState(state: Int, time: String) {
        try {
            val sharedPreferences = getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            sharedPreferences.edit().putInt(tileStateKey, state).putString(tileTimeKey, time).apply()
        } catch (e: Exception) {
            Log.e("SaveTileStateError", "Failed to save tile state", e)
        }
    }

    private fun loadTileState(): Pair<Int, String> {
        return try {
            val sharedPreferences = getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            val state = sharedPreferences.getInt(tileStateKey, Tile.STATE_INACTIVE)
            val time = sharedPreferences.getString(tileTimeKey, "") ?: ""
            Pair(state, time)
        } catch (e: Exception) {
            Log.e("LoadTileStateError", "Failed to load tile state", e)
            Pair(Tile.STATE_INACTIVE, "")
        }
    }

    private fun getCurrentTime(): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM | HH:mm:ss", Locale.getDefault())
            sdf.format(Date())
        } catch (e: Exception) {
            Log.e("GetCurrentTimeError", "Failed to get current time", e)
            "N/A"
        }
    }
    
    private fun isNotificationClosed(): Boolean {
        return try {
            getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
                .getBoolean(notificationClosedKey, false)
        } catch (e: Exception) {
            Log.e("isNotificationClosed", "Error checking notification closed state", e)
            false
        }
    }
    
    private fun setNotificationClosed(closed: Boolean) {
        try {
            getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
                .edit().putBoolean(notificationClosedKey, closed).apply()
        } catch (e: Exception) {
            Log.e("setNotificationClosed", "Error setting notification closed state", e)
        }
    }
    
    private fun restoreTileStateAndNotification() {
        try {
            val (savedState) = loadTileState()
            qsTile.state = savedState
            qsTile.updateTile()
            if (!isNotificationClosed()) {
                updateNotification(getLastToastMessage())
            }
        } catch (e: Exception) {
            Log.e("RestoreTileStateError", "Failed to restore tile state", e)
        }
    }
    
    private fun refreshTileAndNotification() {
        try {
            Log.d("refreshTileAndNotification", "Manual refresh triggered")
            val statusMessage = updateTileStateFromFile()
            updateNotification(statusMessage)
        } catch (e: Exception) {
            Log.e("refreshTileAndNotification", "Error refreshing tile and notification", e)
        }
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.getNotificationChannel(channelId) == null) {
                    val channel = NotificationChannel(
                        channelId,
                        "FreePPS Status",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Shows the current FreePPS status"
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        } catch (e: Exception) {
            Log.e("CreateNotificationError", "Failed to create notification channel", e)
        }
    }

    private fun updateNotification(lastToastMessage: String) {
        try {
            createNotificationChannel()

            val notificationIntent = Intent(this, NotificationClickReceiver::class.java).apply {
                action = "me.freepps.tile.FREEPPS_NOTIFICATION_CLICKED"
            }

            val pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val onIntent = Intent(this, RunFreePPSTileService::class.java).apply {
                action = "me.freepps.tile.FREEPPS_ON"
            }
            val onPendingIntent = PendingIntent.getService(this, 2, onIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val refreshIntent = Intent(this, NotificationClickReceiver::class.java).apply {
                action = "me.freepps.tile.FREEPPS_REFRESH"
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(this, 4, refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val closeIntent = Intent(this, NotificationClickReceiver::class.java).apply {
                action = "me.freepps.tile.FREEPPS_CLOSE_NOTIFICATION"
            }
            val closePendingIntent = PendingIntent.getBroadcast(this, 3, closeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val isLightStatusBar = isLightStatusBar(this)
            val notificationIcon = if (isLightStatusBar) R.drawable.ic_launcher_dark else R.drawable.ic_launcher_light

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.freepps_status))
                .setContentText(lastToastMessage)
                .setSmallIcon(notificationIcon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_on, getString(R.string.toggle), onPendingIntent)
                .addAction(0, getString(R.string.refresh), refreshPendingIntent)
                .addAction(0, getString(R.string.close_notification), closePendingIntent)
                .setOngoing(true)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e("updateNotification", "Error updating notification", e)
        }
    }
    
    private fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, RunFreePPSTileService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    @Suppress("DEPRECATION")
    private fun isLightStatusBar(context: Context): Boolean {
        return try {
            val activity = context as? Activity ?: return false
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    val window = activity.window
                    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                    windowInsetsController.isAppearanceLightStatusBars == true
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    val decorView = activity.window.decorView
                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e("isLightStatusBar", "Error checking light status bar", e)
            false
        }
    }
}

class NotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                "me.freepps.tile.FREEPPS_NOTIFICATION_CLICKED" -> {
                    Log.d("NotificationClickReceiver", "Notification clicked")
                }
                "me.freepps.tile.FREEPPS_REFRESH" -> {
                    Log.d("NotificationClickReceiver", "Refresh notification clicked")
                    // 触发刷新服务
                    val refreshIntent = Intent(context, RunFreePPSTileService::class.java).apply {
                        action = "me.freepps.tile.FREEPPS_REFRESH_STATE"
                    }
                    context.startService(refreshIntent)
                }
                "me.freepps.tile.FREEPPS_CLOSE_NOTIFICATION" -> {
                    Log.d("NotificationClickReceiver", "Close notification clicked")
                    // 记录用户手动关闭了通知
                    val sharedPreferences = context.getSharedPreferences("tile_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("notification_closed", true).apply()
                    // 取消通知
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationClickReceiver", "Error handling notification click", e)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == android.content.Intent.ACTION_BOOT_COMPLETED ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON") {
                Log.d("BootReceiver", "Boot completed, FreePPS tile service ready")
            }
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error handling boot broadcast", e)
        }
    }
}
