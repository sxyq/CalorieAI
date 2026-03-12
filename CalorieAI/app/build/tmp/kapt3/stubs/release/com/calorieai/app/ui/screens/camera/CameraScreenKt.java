package com.calorieai.app.ui.screens.camera;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 2, d1 = {"\u00004\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001c\u0010\u0000\u001a\u00020\u00012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a4\u0010\u0005\u001a\u00020\u00012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001a\u0016\u0010\n\u001a\u00020\u00012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a6\u0010\f\u001a\u00020\u00012\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u0003H\u0002\u00a8\u0006\u0013"}, d2 = {"CameraPreview", "", "onPhotoTaken", "Lkotlin/Function1;", "Landroid/net/Uri;", "CameraScreen", "onNavigateBack", "Lkotlin/Function0;", "viewModel", "Lcom/calorieai/app/ui/screens/camera/CameraViewModel;", "PermissionDeniedContent", "onRequestPermission", "takePhoto", "imageCapture", "Landroidx/camera/core/ImageCapture;", "executor", "Ljava/util/concurrent/Executor;", "context", "Landroid/content/Context;", "app_release"})
public final class CameraScreenKt {
    
    @androidx.compose.runtime.Composable()
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    public static final void CameraScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onPhotoTaken, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.ui.screens.camera.CameraViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PermissionDeniedContent(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRequestPermission) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CameraPreview(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onPhotoTaken) {
    }
    
    private static final void takePhoto(androidx.camera.core.ImageCapture imageCapture, java.util.concurrent.Executor executor, android.content.Context context, kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onPhotoTaken) {
    }
}