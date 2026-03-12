package com.calorieai.app.ui.screens.result;

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
public final class ResultViewModel_Factory implements Factory<ResultViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  public ResultViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
  }

  @Override
  public ResultViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get());
  }

  public static ResultViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    return new ResultViewModel_Factory(foodRecordRepositoryProvider);
  }

  public static ResultViewModel newInstance(FoodRecordRepository foodRecordRepository) {
    return new ResultViewModel(foodRecordRepository);
  }
}
