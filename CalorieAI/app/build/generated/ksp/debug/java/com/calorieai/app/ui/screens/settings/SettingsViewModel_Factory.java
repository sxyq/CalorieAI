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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public SettingsViewModel_Factory(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userSettingsRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new SettingsViewModel_Factory(userSettingsRepositoryProvider);
  }

  public static SettingsViewModel newInstance(UserSettingsRepository userSettingsRepository) {
    return new SettingsViewModel(userSettingsRepository);
  }
}
