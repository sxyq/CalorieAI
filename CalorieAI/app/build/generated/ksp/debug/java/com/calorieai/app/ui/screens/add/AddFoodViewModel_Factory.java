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
public final class AddFoodViewModel_Factory implements Factory<AddFoodViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  public AddFoodViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
  }

  @Override
  public AddFoodViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get());
  }

  public static AddFoodViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    return new AddFoodViewModel_Factory(foodRecordRepositoryProvider);
  }

  public static AddFoodViewModel newInstance(FoodRecordRepository foodRecordRepository) {
    return new AddFoodViewModel(foodRecordRepository);
  }
}
