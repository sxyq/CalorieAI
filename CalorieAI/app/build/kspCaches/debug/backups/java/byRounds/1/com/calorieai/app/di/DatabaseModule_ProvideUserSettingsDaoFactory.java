package com.calorieai.app.di;

import com.calorieai.app.data.local.AppDatabase;
import com.calorieai.app.data.local.UserSettingsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideUserSettingsDaoFactory implements Factory<UserSettingsDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideUserSettingsDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public UserSettingsDao get() {
    return provideUserSettingsDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideUserSettingsDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideUserSettingsDaoFactory(databaseProvider);
  }

  public static UserSettingsDao provideUserSettingsDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideUserSettingsDao(database));
  }
}
