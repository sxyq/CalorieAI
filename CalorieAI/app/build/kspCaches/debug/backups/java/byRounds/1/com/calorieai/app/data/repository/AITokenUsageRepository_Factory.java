package com.calorieai.app.data.repository;

import com.calorieai.app.data.local.AITokenUsageDao;
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
public final class AITokenUsageRepository_Factory implements Factory<AITokenUsageRepository> {
  private final Provider<AITokenUsageDao> aiTokenUsageDaoProvider;

  public AITokenUsageRepository_Factory(Provider<AITokenUsageDao> aiTokenUsageDaoProvider) {
    this.aiTokenUsageDaoProvider = aiTokenUsageDaoProvider;
  }

  @Override
  public AITokenUsageRepository get() {
    return newInstance(aiTokenUsageDaoProvider.get());
  }

  public static AITokenUsageRepository_Factory create(
      Provider<AITokenUsageDao> aiTokenUsageDaoProvider) {
    return new AITokenUsageRepository_Factory(aiTokenUsageDaoProvider);
  }

  public static AITokenUsageRepository newInstance(AITokenUsageDao aiTokenUsageDao) {
    return new AITokenUsageRepository(aiTokenUsageDao);
  }
}
