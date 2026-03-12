package com.calorieai.app.ui.screens.add;

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
public final class ManualAddViewModel_Factory implements Factory<ManualAddViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  public ManualAddViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
  }

  @Override
  public ManualAddViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get());
  }

  public static ManualAddViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    return new ManualAddViewModel_Factory(foodRecordRepositoryProvider);
  }

  public static ManualAddViewModel newInstance(FoodRecordRepository foodRecordRepository) {
    return new ManualAddViewModel(foodRecordRepository);
  }
}
