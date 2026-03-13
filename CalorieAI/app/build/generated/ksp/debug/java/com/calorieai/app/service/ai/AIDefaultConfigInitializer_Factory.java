package com.calorieai.app.service.ai;

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
public final class AIDefaultConfigInitializer_Factory implements Factory<AIDefaultConfigInitializer> {
  private final Provider<AIConfigDao> aiConfigDaoProvider;

  public AIDefaultConfigInitializer_Factory(Provider<AIConfigDao> aiConfigDaoProvider) {
    this.aiConfigDaoProvider = aiConfigDaoProvider;
  }

  @Override
  public AIDefaultConfigInitializer get() {
    return newInstance(aiConfigDaoProvider.get());
  }

  public static AIDefaultConfigInitializer_Factory create(
      Provider<AIConfigDao> aiConfigDaoProvider) {
    return new AIDefaultConfigInitializer_Factory(aiConfigDaoProvider);
  }

  public static AIDefaultConfigInitializer newInstance(AIConfigDao aiConfigDao) {
    return new AIDefaultConfigInitializer(aiConfigDao);
  }
}
