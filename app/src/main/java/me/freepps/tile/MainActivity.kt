package me.freepps.tile

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.widget.Toast
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        } else {
            triggerNotificationService()
        }

        val rootStatusText: TextView = findViewById(R.id.root_status)
        if (isRootAvailable()) {
            rootStatusText.text = "Root 权限: 已启用"
        } else {
            rootStatusText.text = "Root 权限: 未启用"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            val notificationStatusText: TextView = findViewById(R.id.notification_status)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationStatusText.text = "通知权限: 已授予"
                triggerNotificationService()
            } else {
                notificationStatusText.text = "通知权限: 被拒绝"
            }
        }
    }

    private fun triggerNotificationService() {
        Toast.makeText(this, "请在下拉面板中添加 FreePPS 磁贴", Toast.LENGTH_LONG).show()
        Toast.makeText(this, "点击磁贴以切换 PPS 支持状态", Toast.LENGTH_LONG).show()
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
}
