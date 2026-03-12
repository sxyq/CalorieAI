package com.calorieai.app.ui.screens.stats;

import com.calorieai.app.data.repository.FoodRecordRepository;
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

  public StatsViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
  }

  @Override
  public StatsViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get());
  }

  public static StatsViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    return new StatsViewModel_Factory(foodRecordRepositoryProvider);
  }

  public static StatsViewModel newInstance(FoodRecordRepository foodRecordRepository) {
    return new StatsViewModel(foodRecordRepository);
  }
}
