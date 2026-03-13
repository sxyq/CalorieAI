package com.calorieai.app.ui.screens.home;

import com.calorieai.app.data.repository.ExerciseRecordRepository;
import com.calorieai.app.data.repository.FoodRecordRepository;
import com.calorieai.app.data.repository.UserSettingsRepository;
import com.calorieai.app.data.repository.WeightRecordRepository;
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

  private final Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider;

  private final Provider<WeightRecordRepository> weightRecordRepositoryProvider;

  public HomeViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider,
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider,
      Provider<WeightRecordRepository> weightRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
    this.exerciseRecordRepositoryProvider = exerciseRecordRepositoryProvider;
    this.weightRecordRepositoryProvider = weightRecordRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get(), userSettingsRepositoryProvider.get(), exerciseRecordRepositoryProvider.get(), weightRecordRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider,
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider,
      Provider<WeightRecordRepository> weightRecordRepositoryProvider) {
    return new HomeViewModel_Factory(foodRecordRepositoryProvider, userSettingsRepositoryProvider, exerciseRecordRepositoryProvider, weightRecordRepositoryProvider);
  }

  public static HomeViewModel newInstance(FoodRecordRepository foodRecordRepository,
      UserSettingsRepository userSettingsRepository,
      ExerciseRecordRepository exerciseRecordRepository,
      WeightRecordRepository weightRecordRepository) {
    return new HomeViewModel(foodRecordRepository, userSettingsRepository, exerciseRecordRepository, weightRecordRepository);
  }
}
