package com.calorieai.app.di;

import com.calorieai.app.data.local.AIConfigDao;
import com.calorieai.app.data.local.AppDatabase;
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
public final class DatabaseModule_ProvideAIConfigDaoFactory implements Factory<AIConfigDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideAIConfigDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AIConfigDao get() {
    return provideAIConfigDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideAIConfigDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideAIConfigDaoFactory(databaseProvider);
  }

  public static AIConfigDao provideAIConfigDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAIConfigDao(database));
  }
}
