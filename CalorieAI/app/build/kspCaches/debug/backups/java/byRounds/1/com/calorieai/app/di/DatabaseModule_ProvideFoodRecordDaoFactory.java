package com.calorieai.app.di;

import com.calorieai.app.data.local.AppDatabase;
import com.calorieai.app.data.local.FoodRecordDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideFoodRecordDaoFactory implements Factory<FoodRecordDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideFoodRecordDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public FoodRecordDao get() {
    return provideFoodRecordDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideFoodRecordDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFoodRecordDaoFactory(databaseProvider);
  }

  public static FoodRecordDao provideFoodRecordDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFoodRecordDao(database));
  }
}
