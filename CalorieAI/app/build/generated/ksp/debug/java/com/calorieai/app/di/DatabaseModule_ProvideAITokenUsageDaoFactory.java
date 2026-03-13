package com.calorieai.app.di;

import com.calorieai.app.data.local.AITokenUsageDao;
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
public final class DatabaseModule_ProvideAITokenUsageDaoFactory implements Factory<AITokenUsageDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideAITokenUsageDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AITokenUsageDao get() {
    return provideAITokenUsageDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideAITokenUsageDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideAITokenUsageDaoFactory(databaseProvider);
  }

  public static AITokenUsageDao provideAITokenUsageDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAITokenUsageDao(database));
  }
}
