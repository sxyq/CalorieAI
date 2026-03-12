package com.calorieai.app.ui.screens.settings;

import com.calorieai.app.data.repository.UserSettingsRepository;
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
  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public BackupSettingsViewModel_Factory(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  @Override
  public BackupSettingsViewModel get() {
    return newInstance(userSettingsRepositoryProvider.get());
  }

  public static BackupSettingsViewModel_Factory create(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new BackupSettingsViewModel_Factory(userSettingsRepositoryProvider);
  }

  public static BackupSettingsViewModel newInstance(UserSettingsRepository userSettingsRepository) {
    return new BackupSettingsViewModel(userSettingsRepository);
  }
}
