package com.calorieai.app;

import com.calorieai.app.data.repository.UserSettingsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public MainActivity_MembersInjector(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new MainActivity_MembersInjector(userSettingsRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectUserSettingsRepository(instance, userSettingsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.calorieai.app.MainActivity.userSettingsRepository")
  public static void injectUserSettingsRepository(MainActivity instance,
      UserSettingsRepository userSettingsRepository) {
    instance.userSettingsRepository = userSettingsRepository;
  }
}
