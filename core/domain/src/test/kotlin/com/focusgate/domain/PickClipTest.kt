package com.focusgate.domain

import com.focusgate.domain.model.Clip
import com.focusgate.domain.usecase.PickClip
import com.focusgate.domain.usecase.continueSecondsRemaining
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PickClipTest {
    private val clips = (1L..3L).map { Clip(it, "u$it", "t$it") }

    @Test fun neverRepeatsLastWhenAlternativesExist() {
        repeat(100) { assert(PickClip()(clips, lastPlayedId = 2)!!.id != 2L) }
    }

    @Test fun repeatsWhenOnlyOne() = assertEquals(1L, PickClip()(clips.take(1), 1)!!.id)
    @Test fun emptyGivesNull() = assertNull(PickClip()(emptyList(), null))
    @Test fun countdown() {
        assertEquals(30, continueSecondsRemaining(30_000, 0))
        assertEquals(1, continueSecondsRemaining(30_000, 29_500))
        assertEquals(0, continueSecondsRemaining(30_000, 31_000))
    }
}
