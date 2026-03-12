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
public final class NotificationSettingsViewModel_Factory implements Factory<NotificationSettingsViewModel> {
  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public NotificationSettingsViewModel_Factory(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  @Override
  public NotificationSettingsViewModel get() {
    return newInstance(userSettingsRepositoryProvider.get());
  }

  public static NotificationSettingsViewModel_Factory create(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new NotificationSettingsViewModel_Factory(userSettingsRepositoryProvider);
  }

  public static NotificationSettingsViewModel newInstance(
      UserSettingsRepository userSettingsRepository) {
    return new NotificationSettingsViewModel(userSettingsRepository);
  }
}
