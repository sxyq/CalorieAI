package com.calorieai.app.ui.screens.stats;

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
public final class StatsViewModel_Factory implements Factory<StatsViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  private final Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider;

  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  private final Provider<WeightRecordRepository> weightRecordRepositoryProvider;

  public StatsViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider,
      Provider<WeightRecordRepository> weightRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
    this.exerciseRecordRepositoryProvider = exerciseRecordRepositoryProvider;
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
    this.weightRecordRepositoryProvider = weightRecordRepositoryProvider;
  }

  @Override
  public StatsViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get(), exerciseRecordRepositoryProvider.get(), userSettingsRepositoryProvider.get(), weightRecordRepositoryProvider.get());
  }

  public static StatsViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider,
      Provider<WeightRecordRepository> weightRecordRepositoryProvider) {
    return new StatsViewModel_Factory(foodRecordRepositoryProvider, exerciseRecordRepositoryProvider, userSettingsRepositoryProvider, weightRecordRepositoryProvider);
  }

  public static StatsViewModel newInstance(FoodRecordRepository foodRecordRepository,
      ExerciseRecordRepository exerciseRecordRepository,
      UserSettingsRepository userSettingsRepository,
      WeightRecordRepository weightRecordRepository) {
    return new StatsViewModel(foodRecordRepository, exerciseRecordRepository, userSettingsRepository, weightRecordRepository);
  }
}
