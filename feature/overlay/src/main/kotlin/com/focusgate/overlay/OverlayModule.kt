package com.focusgate.overlay

import com.focusgate.domain.detect.InterruptPresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OverlayModule {
    @Binds abstract fun presenter(i: WindowInterruptPresenter): InterruptPresenter
}
