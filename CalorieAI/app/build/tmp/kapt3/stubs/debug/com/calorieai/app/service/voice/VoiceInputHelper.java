package com.calorieai.app.service.voice;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\f\u001a\u00020\rJ6\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\r0\u00122\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\r0\u0012J\u0006\u0010\u0015\u001a\u00020\rR\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/calorieai/app/service/voice/VoiceInputHelper;", "", "()V", "_voiceState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/calorieai/app/service/voice/VoiceState;", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "voiceState", "Lkotlinx/coroutines/flow/StateFlow;", "getVoiceState", "()Lkotlinx/coroutines/flow/StateFlow;", "destroy", "", "startListening", "context", "Landroid/content/Context;", "onResult", "Lkotlin/Function1;", "", "onError", "stopListening", "app_debug"})
@javax.inject.Singleton()
public final class VoiceInputHelper {
    private android.speech.SpeechRecognizer speechRecognizer;
    private final kotlinx.coroutines.flow.MutableStateFlow<com.calorieai.app.service.voice.VoiceState> _voiceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.service.voice.VoiceState> voiceState = null;
    
    @javax.inject.Inject()
    public VoiceInputHelper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.service.voice.VoiceState> getVoiceState() {
        return null;
    }
    
    public final void startListening(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onResult, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
    
    public final void stopListening() {
    }
    
    public final void destroy() {
    }
}