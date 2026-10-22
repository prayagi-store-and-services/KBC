package com.example

import com.example.util.PhoneHealth
import com.example.util.PhoneHealth.Reading
import com.example.util.PhoneHealth.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneHealthTest {
    @Test fun hotPhoneIsHeld() = assertEquals(Verdict.HOT, PhoneHealth.decide(Reading(80, 43.5f, false)))
    @Test fun exactlyAtLimitIsHeld() = assertEquals(Verdict.HOT, PhoneHealth.decide(Reading(80, 42f, true)))
    @Test fun normalPhoneStarts() = assertEquals(Verdict.OK, PhoneHealth.decide(Reading(60, 33f, false)))
    @Test fun unknownTemperatureNeverBlocks() = assertEquals(Verdict.OK, PhoneHealth.decide(Reading(60, null, false)))
    @Test fun lowBatteryOnlyWarnsAndNotWhenCharging() {
        assertEquals(Verdict.LOW_BATTERY, PhoneHealth.decide(Reading(10, 30f, false)))
        assertEquals(Verdict.OK, PhoneHealth.decide(Reading(10, 30f, true)))
    }
    @Test fun unknownBatteryDoesNotBlock() = assertEquals(Verdict.OK, PhoneHealth.decide(Reading(null, null, false)))
    @Test fun wordingIsHonest() {
        assertTrue(PhoneHealth.testText(Reading(55, 44f, false)).startsWith("TEST (no game started)"))
        assertTrue(PhoneHealth.testText(Reading(null, null, false)).contains("unavailable"))
        assertTrue(PhoneHealth.hotMessage(Reading(55, 44f, false)).contains("44.0 C"))
    }
}
