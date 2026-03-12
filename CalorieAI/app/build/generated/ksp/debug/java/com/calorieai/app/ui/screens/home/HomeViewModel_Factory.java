package com.calorieai.app.ui.screens.home;

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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  public HomeViewModel_Factory(Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(foodRecordRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<FoodRecordRepository> foodRecordRepositoryProvider) {
    return new HomeViewModel_Factory(foodRecordRepositoryProvider);
  }

  public static HomeViewModel newInstance(FoodRecordRepository foodRecordRepository) {
    return new HomeViewModel(foodRecordRepository);
  }
}
