package com.calorieai.app.ui.screens.camera;

import com.calorieai.app.data.repository.FoodRecordRepository;
import com.calorieai.app.service.ai.FoodImageAnalysisService;
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
public final class PhotoAnalysisViewModel_Factory implements Factory<PhotoAnalysisViewModel> {
  private final Provider<FoodImageAnalysisService> foodImageAnalysisServiceProvider;

  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  public PhotoAnalysisViewModel_Factory(
      Provider<FoodImageAnalysisService> foodImageAnalysisServiceProvider,
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    this.foodImageAnalysisServiceProvider = foodImageAnalysisServiceProvider;
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
  }

  @Override
  public PhotoAnalysisViewModel get() {
    return newInstance(foodImageAnalysisServiceProvider.get(), foodRecordRepositoryProvider.get());
  }

  public static PhotoAnalysisViewModel_Factory create(
      Provider<FoodImageAnalysisService> foodImageAnalysisServiceProvider,
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    return new PhotoAnalysisViewModel_Factory(foodImageAnalysisServiceProvider, foodRecordRepositoryProvider);
  }

  public static PhotoAnalysisViewModel newInstance(
      FoodImageAnalysisService foodImageAnalysisService,
      FoodRecordRepository foodRecordRepository) {
    return new PhotoAnalysisViewModel(foodImageAnalysisService, foodRecordRepository);
  }
}
