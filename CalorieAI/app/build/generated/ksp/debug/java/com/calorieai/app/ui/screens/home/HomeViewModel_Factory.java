package com.calorieai.app.ui.screens.home;

import com.calorieai.app.data.repository.FoodRecordRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  public HomeViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get(), userSettingsRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider) {
    return new HomeViewModel_Factory(foodRecordRepositoryProvider, userSettingsRepositoryProvider);
  }

  public static HomeViewModel newInstance(FoodRecordRepository foodRecordRepository,
      UserSettingsRepository userSettingsRepository) {
    return new HomeViewModel(foodRecordRepository, userSettingsRepository);
  }
}
