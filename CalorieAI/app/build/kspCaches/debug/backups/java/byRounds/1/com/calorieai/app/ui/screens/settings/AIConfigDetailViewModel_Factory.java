package com.calorieai.app.ui.screens.settings;

import com.calorieai.app.data.repository.AIConfigRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AIConfigDetailViewModel_Factory implements Factory<AIConfigDetailViewModel> {
  private final Provider<AIConfigRepository> aiConfigRepositoryProvider;

  public AIConfigDetailViewModel_Factory(Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
  }

  @Override
  public AIConfigDetailViewModel get() {
    return newInstance(aiConfigRepositoryProvider.get());
  }

  public static AIConfigDetailViewModel_Factory create(
      Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    return new AIConfigDetailViewModel_Factory(aiConfigRepositoryProvider);
  }

  public static AIConfigDetailViewModel newInstance(AIConfigRepository aiConfigRepository) {
    return new AIConfigDetailViewModel(aiConfigRepository);
  }
}
