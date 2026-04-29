package com.example.n.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.n.R
import com.example.n.network.RetrofitClient // Đảm bảo import Retrofit
import com.example.n.utils.TokenManager // Đảm bảo import TokenManager
import kotlin.random.Random

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. LẤY TOKEN
            val token = TokenManager(applicationContext).getToken()

            if (token.isNullOrEmpty()) {
                // Chưa đăng nhập thì không làm phiền người dùng
                return Result.success()
            }

            // 2. GỌI API LẤY DATA
            val decks = RetrofitClient.apiService.getDecks("Bearer $token")

            // 3. TÍNH TOÁN SỐ LƯỢNG THẺ (Hộp nháp & Thẻ đến hạn)
            val inboxDeck = decks.find { it.name.equals("Hộp Nháp", ignoreCase = true) }
            var inboxCardsCount = 0
            if (inboxDeck != null) {
                val inboxCards = RetrofitClient.apiService.getCardsByDeck("Bearer $token", inboxDeck._id)
                inboxCardsCount = inboxCards.size
            }

            var dueCardsCount = 0
            decks.filter { !it.name.equals("Hộp Nháp", ignoreCase = true) }.forEach { deck ->
                val cards = RetrofitClient.apiService.getCardsByDeck("Bearer $token", deck._id)
                // Tạm thời mình cộng dồn tổng thẻ. Nếu sau này Backend có API đếm thẻ FSRS due thì gọi vào đây
                dueCardsCount += cards.size
            }

            // 4. KỊCH BẢN THÔNG BÁO THÔNG MINH
            var notiTitle = ""
            var notiMessage = ""

            if (dueCardsCount > 0 && inboxCardsCount > 0) {
                // Random 50/50 để thông báo không bị nhàm chán mỗi ngày
                val isPriorityDue = Random.nextBoolean()
                if (isPriorityDue) {
                    notiTitle = "Đến giờ ôn tập rồi! 🚀"
                    notiMessage = "Bạn đang có $dueCardsCount thẻ cần xử lý. Dành 5 phút học ngay để không gãy chuỗi nhé!"
                } else {
                    notiTitle = "Hộp nháp đang đầy! 📦"
                    notiMessage = "Bạn có $inboxCardsCount từ vựng mới trong Hộp Nháp. Cất chúng vào trí nhớ ngay thôi!"
                }
            } else if (dueCardsCount > 0) {
                notiTitle = "Đến giờ ôn tập rồi! 🚀"
                notiMessage = "Bạn đang có $dueCardsCount thẻ cần xử lý. Dành 5 phút học ngay để không gãy chuỗi nhé!"
            } else if (inboxCardsCount > 0) {
                notiTitle = "Có từ vựng mới đang chờ! ✨"
                notiMessage = "Bạn có $inboxCardsCount từ vựng trong Hộp Nháp. Bắt đầu học bài mới thôi!"
            } else {
                notiTitle = "Renshuu nhớ bạn! 🥺"
                notiMessage = "Dạo này không thấy bạn. Lướt web và bôi đen vài từ vựng để AI giải nghĩa nhé!"
            }

            // 5. HIỂN THỊ THÔNG BÁO (Gọi lại hàm an toàn của bạn)
            showNotification(notiTitle, notiMessage)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Rất quan trọng: Nếu đứt mạng, vẫn trả về success để WorkManager không bị crash,
            // chu kỳ sau có mạng nó sẽ tự động chạy lại êm ru.
            Result.success()
        }
    }

    // GIỮ NGUYÊN 100% HÀM CỦA BẠN - KHÔNG SỬA GÌ CẢ
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

        // Vẫn giữ ID 1001 của bạn. Việc này rất tốt vì nó giúp thông báo mới sẽ "đè" lên thông báo cũ
        // thay vì đẻ ra chục cái thông báo gây rác màn hình người dùng.
        notificationManager.notify(1001, notification)
    }
}