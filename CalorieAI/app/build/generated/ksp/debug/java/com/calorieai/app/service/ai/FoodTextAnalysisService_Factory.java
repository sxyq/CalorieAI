package com.calorieai.app.service.ai;

import com.calorieai.app.data.repository.AIConfigRepository;
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
public final class FoodTextAnalysisService_Factory implements Factory<FoodTextAnalysisService> {
  private final Provider<AIConfigRepository> aiConfigRepositoryProvider;

  public FoodTextAnalysisService_Factory(Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
  }

  @Override
  public FoodTextAnalysisService get() {
    return newInstance(aiConfigRepositoryProvider.get());
  }

  public static FoodTextAnalysisService_Factory create(
      Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    return new FoodTextAnalysisService_Factory(aiConfigRepositoryProvider);
  }

  public static FoodTextAnalysisService newInstance(AIConfigRepository aiConfigRepository) {
    return new FoodTextAnalysisService(aiConfigRepository);
  }
}
