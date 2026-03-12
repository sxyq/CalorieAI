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
public final class AppearanceSettingsViewModel_Factory implements Factory<AppearanceSettingsViewModel> {
  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public AppearanceSettingsViewModel_Factory(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  @Override
  public AppearanceSettingsViewModel get() {
    return newInstance(userSettingsRepositoryProvider.get());
  }

  public static AppearanceSettingsViewModel_Factory create(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new AppearanceSettingsViewModel_Factory(userSettingsRepositoryProvider);
  }

  public static AppearanceSettingsViewModel newInstance(
      UserSettingsRepository userSettingsRepository) {
    return new AppearanceSettingsViewModel(userSettingsRepository);
  }
}
