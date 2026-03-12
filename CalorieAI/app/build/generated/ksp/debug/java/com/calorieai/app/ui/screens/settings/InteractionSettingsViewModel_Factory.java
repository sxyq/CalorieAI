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
public final class InteractionSettingsViewModel_Factory implements Factory<InteractionSettingsViewModel> {
  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public InteractionSettingsViewModel_Factory(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  @Override
  public InteractionSettingsViewModel get() {
    return newInstance(userSettingsRepositoryProvider.get());
  }

  public static InteractionSettingsViewModel_Factory create(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new InteractionSettingsViewModel_Factory(userSettingsRepositoryProvider);
  }

  public static InteractionSettingsViewModel newInstance(
      UserSettingsRepository userSettingsRepository) {
    return new InteractionSettingsViewModel(userSettingsRepository);
  }
}
