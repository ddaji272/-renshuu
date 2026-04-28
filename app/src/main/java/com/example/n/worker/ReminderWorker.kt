package com.example.n.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.n.R // Đảm bảo import R để lấy icon

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Giả lập có 15 thẻ cần học
        val dueCardsCount = 15

        if (dueCardsCount > 0) {
            showNotification(
                title = "Đến giờ ôn tập rồi! 🚀",
                message = "Bạn đang có $dueCardsCount thẻ cần xử lý. Dành 5 phút học ngay để không gãy chuỗi nhé!"
            )
        }
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "renshuu_study_reminders"

        // Tạo Channel (Bắt buộc từ Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở học tập Renshuu",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Kênh thông báo nhắc nhở ôn tập từ vựng hàng ngày"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build nội dung thông báo
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher) // Dùng icon app mặc định làm icon thông báo
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}