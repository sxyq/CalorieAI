package com.calorieai.app.ui.screens.add;

import com.calorieai.app.data.repository.FoodRecordRepository;
import com.calorieai.app.service.ai.FoodTextAnalysisService;
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
public final class AddFoodViewModel_Factory implements Factory<AddFoodViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  private final Provider<FoodTextAnalysisService> foodTextAnalysisServiceProvider;

  public AddFoodViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<FoodTextAnalysisService> foodTextAnalysisServiceProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
    this.foodTextAnalysisServiceProvider = foodTextAnalysisServiceProvider;
  }

  @Override
  public AddFoodViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get(), foodTextAnalysisServiceProvider.get());
  }

  public static AddFoodViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<FoodTextAnalysisService> foodTextAnalysisServiceProvider) {
    return new AddFoodViewModel_Factory(foodRecordRepositoryProvider, foodTextAnalysisServiceProvider);
  }

  public static AddFoodViewModel newInstance(FoodRecordRepository foodRecordRepository,
      FoodTextAnalysisService foodTextAnalysisService) {
    return new AddFoodViewModel(foodRecordRepository, foodTextAnalysisService);
  }
}
