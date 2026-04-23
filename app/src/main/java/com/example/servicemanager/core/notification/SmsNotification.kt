package com.example.servicemanager.core.notification

import android.telephony.SmsManager

object SmsTemplates {
    // Base variables used by default template. The app supports additional placeholders from settings.
    const val CREATE_SERVICE_SMS_TEMPLATE =
        "Hi {customer_name}, your {device_brand} {device_model} ({device_type}) service is created with status {status}. Issue: {problem}."

    // Base variables used by default template. The app supports additional placeholders from settings.
    const val STATUS_UPDATE_SMS_TEMPLATE =
        "Hi {customer_name}, service {service_code} moved from {old_status} to {new_status}. Note: {note}"

    // Dedicated template used when service is moved to DELIVERED status.
    const val DELIVERED_STATUS_SMS_TEMPLATE =
        "Hi {customer_name}, your service {service_code} has been delivered. Thank you."
}

object SmsTemplateEngine {
    fun interpolate(template: String, variables: Map<String, String>): String {
        var result = template
        variables.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result
    }
}

fun shouldAttemptSms(notifyCustomerInSms: Boolean, phone: String): Boolean =
    notifyCustomerInSms && phone.isNotBlank()

data class SmsSendResult(
    val success: Boolean,
    val errorMessage: String? = null,
)

interface SmsSender {
    fun send(phone: String, message: String): SmsSendResult
}

object SimSmsSender : SmsSender {
    override fun send(phone: String, message: String): SmsSendResult {
        if (phone.isBlank()) {
            return SmsSendResult(success = false, errorMessage = "Customer phone number is empty.")
        }
        if (message.isBlank()) {
            return SmsSendResult(success = false, errorMessage = "SMS message is empty.")
        }
        return runCatching {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
        }.fold(
            onSuccess = { SmsSendResult(success = true) },
            onFailure = { SmsSendResult(success = false, errorMessage = it.message ?: "Failed to send SMS.") },
        )
    }
}
