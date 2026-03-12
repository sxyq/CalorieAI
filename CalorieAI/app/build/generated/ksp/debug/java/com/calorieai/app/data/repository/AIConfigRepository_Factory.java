package com.calorieai.app.data.repository;

import com.calorieai.app.data.local.AIConfigDao;
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
public final class AIConfigRepository_Factory implements Factory<AIConfigRepository> {
  private final Provider<AIConfigDao> aiConfigDaoProvider;

  public AIConfigRepository_Factory(Provider<AIConfigDao> aiConfigDaoProvider) {
    this.aiConfigDaoProvider = aiConfigDaoProvider;
  }

  @Override
  public AIConfigRepository get() {
    return newInstance(aiConfigDaoProvider.get());
  }

  public static AIConfigRepository_Factory create(Provider<AIConfigDao> aiConfigDaoProvider) {
    return new AIConfigRepository_Factory(aiConfigDaoProvider);
  }

  public static AIConfigRepository newInstance(AIConfigDao aiConfigDao) {
    return new AIConfigRepository(aiConfigDao);
  }
}
