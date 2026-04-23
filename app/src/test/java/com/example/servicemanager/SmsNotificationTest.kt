package com.example.servicemanager

import com.example.servicemanager.core.notification.SmsTemplateEngine
import com.example.servicemanager.core.notification.SmsTemplates
import com.example.servicemanager.core.notification.shouldAttemptSms
import com.example.servicemanager.features.AddServiceUiState
import com.example.servicemanager.features.StatusUpdateUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsNotificationTest {
    @Test
    fun interpolateCreateTemplate_replacesAllVariables() {
        val message = SmsTemplateEngine.interpolate(
            SmsTemplates.CREATE_SERVICE_SMS_TEMPLATE,
            mapOf(
                "customer_name" to "Arun",
                "device_brand" to "Samsung",
                "device_model" to "S23",
                "device_type" to "Phone",
                "status" to "QUEUED",
                "problem" to "No display",
            ),
        )

        assertTrue(message.contains("Arun"))
        assertTrue(message.contains("Samsung"))
        assertTrue(message.contains("S23"))
        assertTrue(message.contains("Phone"))
        assertTrue(message.contains("QUEUED"))
        assertTrue(message.contains("No display"))
    }

    @Test
    fun interpolateStatusTemplate_replacesNoteWithFallbackDash() {
        val message = SmsTemplateEngine.interpolate(
            SmsTemplates.STATUS_UPDATE_SMS_TEMPLATE,
            mapOf(
                "service_code" to "SN-1234-A",
                "customer_name" to "Arun",
                "old_status" to "IN PROGRESS",
                "new_status" to "WAITING FOR SPARE",
                "note" to "-",
            ),
        )

        assertTrue(message.contains("SN-1234-A"))
        assertTrue(message.contains("Arun"))
        assertTrue(message.contains("IN PROGRESS"))
        assertTrue(message.contains("WAITING FOR SPARE"))
        assertTrue(message.contains("Note: -"))
    }

    @Test
    fun notifySms_defaultIsChecked_inCreateAndStatusUpdate() {
        assertEquals(true, AddServiceUiState().notifyCustomerInSms)
        assertEquals(true, StatusUpdateUiState().notifyCustomerInSms)
    }

    @Test
    fun shouldAttemptSms_falseWhenCheckboxOff() {
        assertFalse(shouldAttemptSms(notifyCustomerInSms = false, phone = "9999999999"))
        assertFalse(shouldAttemptSms(notifyCustomerInSms = true, phone = ""))
        assertTrue(shouldAttemptSms(notifyCustomerInSms = true, phone = "9999999999"))
    }
}
