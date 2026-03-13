package com.calorieai.app.ui.screens.settings;

import com.calorieai.app.service.backup.BackupService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class BackupSettingsViewModel_Factory implements Factory<BackupSettingsViewModel> {
  private final Provider<BackupService> backupServiceProvider;

  public BackupSettingsViewModel_Factory(Provider<BackupService> backupServiceProvider) {
    this.backupServiceProvider = backupServiceProvider;
  }

  @Override
  public BackupSettingsViewModel get() {
    return newInstance(backupServiceProvider.get());
  }

  public static BackupSettingsViewModel_Factory create(
      Provider<BackupService> backupServiceProvider) {
    return new BackupSettingsViewModel_Factory(backupServiceProvider);
  }

  public static BackupSettingsViewModel newInstance(BackupService backupService) {
    return new BackupSettingsViewModel(backupService);
  }
}
