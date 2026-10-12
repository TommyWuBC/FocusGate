package com.focusgate.domain.usecase

/** Seconds remaining on the "continue anyway" button; 0 means it is enabled. */
fun continueSecondsRemaining(delayMs: Long, elapsedMs: Long): Int =
    ((delayMs - elapsedMs).coerceAtLeast(0) + 999).div(1000).toInt()
