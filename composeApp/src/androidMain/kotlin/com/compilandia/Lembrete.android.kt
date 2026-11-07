package com.compilandia.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.compilandia.content.Lembretes
import com.compilandia.learning.Learner
import com.compilandia.textos.Txt
import java.util.Calendar

/**
 * R4: o alarme diário fica no AlarmManager, inexato (não precisa de permissão especial).
 * Quando ele toca, [LembreteReceiver] lê o save e decide: sem revisão pendente, não notifica.
 */
actual object Lembrete {
    var ctx: Context? = null
    /** A MainActivity guarda aqui o pedido de permissão (precisa de uma Activity). */
    var pedir: ((onResultado: (Boolean) -> Unit) -> Unit)? = null

    actual val disponivel: Boolean = true

    private fun intent(c: Context) = PendingIntent.getBroadcast(
        c, 7, Intent(c, LembreteReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    actual fun agenda(ligado: Boolean, hora: Int) {
        val c = ctx ?: return
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = intent(c)
        am.cancel(pi)
        if (!ligado) return
        val quando = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, quando.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }

    actual fun pedePermissao(onResultado: (Boolean) -> Unit) {
        val c = ctx ?: return onResultado(false)
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            onResultado(true)
            return
        }
        pedir?.invoke(onResultado) ?: onResultado(false)
    }

    const val CANAL = "revisao"

    /** Monta e mostra a notificação, se houver revisão pendente e o lembrete estiver ligado. */
    fun dispara(c: Context) {
        Storage.dir = c.filesDir
        val salvo = Storage.load() ?: return
        Txt.idioma = Txt.idiomaInicial(salvo, java.util.Locale.getDefault().language)
        val l = Learner.load(salvo)
        if (!l.state.lembreteLigado) return
        val texto = Lembretes.texto(l, System.currentTimeMillis()) ?: return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = c.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CANAL, Lembretes.canal, NotificationManager.IMPORTANCE_DEFAULT))
        }
        val abre = c.packageManager.getLaunchIntentForPackage(c.packageName)?.let {
            PendingIntent.getActivity(c, 8, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val n = NotificationCompat.Builder(c, CANAL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Compilândia")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .apply { if (abre != null) setContentIntent(abre) }
            .build()
        try { NotificationManagerCompat.from(c).notify(1, n) } catch (e: SecurityException) { }
    }
}

/** Toca uma vez por dia. Também religa o alarme depois que o celular reinicia. */
class LembreteReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED) {
            Storage.dir = c.filesDir
            val l = Learner.load(Storage.load())
            Lembrete.ctx = c.applicationContext
            Lembrete.agenda(l.state.lembreteLigado, l.state.lembreteHora)
            return
        }
        Lembrete.dispara(c)
    }
}
