package com.calorieai.app.data.repository;

import com.calorieai.app.data.local.UserSettingsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class UserSettingsRepository_Factory implements Factory<UserSettingsRepository> {
  private final Provider<UserSettingsDao> userSettingsDaoProvider;

  public UserSettingsRepository_Factory(Provider<UserSettingsDao> userSettingsDaoProvider) {
    this.userSettingsDaoProvider = userSettingsDaoProvider;
  }

  @Override
  public UserSettingsRepository get() {
    return newInstance(userSettingsDaoProvider.get());
  }

  public static UserSettingsRepository_Factory create(
      Provider<UserSettingsDao> userSettingsDaoProvider) {
    return new UserSettingsRepository_Factory(userSettingsDaoProvider);
  }

  public static UserSettingsRepository newInstance(UserSettingsDao userSettingsDao) {
    return new UserSettingsRepository(userSettingsDao);
  }
}
