package com.calorieai.app.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WeightRecordRepository_Factory implements Factory<WeightRecordRepository> {
  private final Provider<WeightRecordDao> weightRecordDaoProvider;

  public WeightRecordRepository_Factory(Provider<WeightRecordDao> weightRecordDaoProvider) {
    this.weightRecordDaoProvider = weightRecordDaoProvider;
  }

  @Override
  public WeightRecordRepository get() {
    return newInstance(weightRecordDaoProvider.get());
  }

  public static WeightRecordRepository_Factory create(
      Provider<WeightRecordDao> weightRecordDaoProvider) {
    return new WeightRecordRepository_Factory(weightRecordDaoProvider);
  }

  public static WeightRecordRepository newInstance(WeightRecordDao weightRecordDao) {
    return new WeightRecordRepository(weightRecordDao);
  }
}
