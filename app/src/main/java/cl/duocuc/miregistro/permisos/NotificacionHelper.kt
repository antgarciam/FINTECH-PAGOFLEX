package cl.duocuc.miregistro.permisos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import cl.duocuc.miregistro.R

object NotificacionHelper {
    private const val CANAL_ID = "canal_pagoflex_promos"
    private const val CANAL_NOMBRE = "Promociones PagoFlex"

    fun enviarNotificacionDescuento(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                CANAL_NOMBRE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para promociones y descuentos de PagoFlex"
            }
            notificationManager.createNotificationChannel(canal)
        }

        val builder = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("Oferta especial PagoFlex!!")
            .setContentText("Paga hoy y obten un 20% de descuento en el total de tu pago")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())

    }


}