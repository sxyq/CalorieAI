package com.calorieai.app.ui.screens.weight;

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
public final class WeightRecordViewModel_Factory implements Factory<WeightRecordViewModel> {
  private final Provider<WeightRecordRepository> weightRecordRepositoryProvider;

  public WeightRecordViewModel_Factory(
      Provider<WeightRecordRepository> weightRecordRepositoryProvider) {
    this.weightRecordRepositoryProvider = weightRecordRepositoryProvider;
  }

  @Override
  public WeightRecordViewModel get() {
    return newInstance(weightRecordRepositoryProvider.get());
  }

  public static WeightRecordViewModel_Factory create(
      Provider<WeightRecordRepository> weightRecordRepositoryProvider) {
    return new WeightRecordViewModel_Factory(weightRecordRepositoryProvider);
  }

  public static WeightRecordViewModel newInstance(WeightRecordRepository weightRecordRepository) {
    return new WeightRecordViewModel(weightRecordRepository);
  }
}
