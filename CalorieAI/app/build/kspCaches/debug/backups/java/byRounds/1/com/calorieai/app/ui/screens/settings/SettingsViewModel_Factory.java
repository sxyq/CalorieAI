package com.calorieai.app.ui.screens.settings;

import com.calorieai.app.service.backup.BackupManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<BackupManager> backupManagerProvider;

  public SettingsViewModel_Factory(Provider<BackupManager> backupManagerProvider) {
    this.backupManagerProvider = backupManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(backupManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<BackupManager> backupManagerProvider) {
    return new SettingsViewModel_Factory(backupManagerProvider);
  }

  public static SettingsViewModel newInstance(BackupManager backupManager) {
    return new SettingsViewModel(backupManager);
  }
}
