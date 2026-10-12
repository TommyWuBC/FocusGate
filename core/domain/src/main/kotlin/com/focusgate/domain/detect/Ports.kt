package com.focusgate.domain.detect

import com.focusgate.domain.model.Clip
import com.focusgate.domain.model.InterruptConfig

/** What the user chose on the interrupt screen. */
sealed interface InterruptResult {
    data object GoBack : InterruptResult
    data class Continue(val reason: String?) : InterruptResult
}

/** Shows the interrupt UI and suspends until the user decides. Implemented by the overlay module. */
interface InterruptPresenter {
    suspend fun present(packageName: String, clip: Clip?, config: InterruptConfig): InterruptResult
}

/** Sends the user to the home screen. Implemented by the detection service (needs a live service). */
fun interface HomeNavigator {
    fun goHome()
}

fun interface Clock {
    fun nowMs(): Long
}
