package com.focusgate.domain.usecase

import com.focusgate.domain.model.Clip
import kotlin.random.Random

/** Picks a random clip, avoiding [lastPlayedId] unless it is the only one. */
class PickClip(private val random: Random = Random.Default) {
    operator fun invoke(clips: List<Clip>, lastPlayedId: Long?): Clip? {
        if (clips.isEmpty()) return null
        val candidates = clips.filter { it.id != lastPlayedId }.ifEmpty { clips }
        return candidates[random.nextInt(candidates.size)]
    }
}
