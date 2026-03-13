package com.calorieai.app.di;

import com.calorieai.app.data.local.AppDatabase;
import com.calorieai.app.data.repository.WeightRecordDao;
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
public final class DatabaseModule_ProvideWeightRecordDaoFactory implements Factory<WeightRecordDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideWeightRecordDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public WeightRecordDao get() {
    return provideWeightRecordDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideWeightRecordDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideWeightRecordDaoFactory(databaseProvider);
  }

  public static WeightRecordDao provideWeightRecordDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideWeightRecordDao(database));
  }
}
