package com.calorieai.app.data.repository;

import com.calorieai.app.data.local.FoodRecordDao;
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
public final class FoodRecordRepository_Factory implements Factory<FoodRecordRepository> {
  private final Provider<FoodRecordDao> foodRecordDaoProvider;

  public FoodRecordRepository_Factory(Provider<FoodRecordDao> foodRecordDaoProvider) {
    this.foodRecordDaoProvider = foodRecordDaoProvider;
  }

  @Override
  public FoodRecordRepository get() {
    return newInstance(foodRecordDaoProvider.get());
  }

  public static FoodRecordRepository_Factory create(Provider<FoodRecordDao> foodRecordDaoProvider) {
    return new FoodRecordRepository_Factory(foodRecordDaoProvider);
  }

  public static FoodRecordRepository newInstance(FoodRecordDao foodRecordDao) {
    return new FoodRecordRepository(foodRecordDao);
  }
}
