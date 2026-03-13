package com.calorieai.app.ui.screens.settings;

import com.calorieai.app.data.repository.AIConfigRepository;
import com.calorieai.app.data.repository.AITokenUsageRepository;
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
public final class AISettingsViewModel_Factory implements Factory<AISettingsViewModel> {
  private final Provider<AIConfigRepository> aiConfigRepositoryProvider;

  private final Provider<AITokenUsageRepository> aiTokenUsageRepositoryProvider;

  public AISettingsViewModel_Factory(Provider<AIConfigRepository> aiConfigRepositoryProvider,
      Provider<AITokenUsageRepository> aiTokenUsageRepositoryProvider) {
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
    this.aiTokenUsageRepositoryProvider = aiTokenUsageRepositoryProvider;
  }

  @Override
  public AISettingsViewModel get() {
    return newInstance(aiConfigRepositoryProvider.get(), aiTokenUsageRepositoryProvider.get());
  }

  public static AISettingsViewModel_Factory create(
      Provider<AIConfigRepository> aiConfigRepositoryProvider,
      Provider<AITokenUsageRepository> aiTokenUsageRepositoryProvider) {
    return new AISettingsViewModel_Factory(aiConfigRepositoryProvider, aiTokenUsageRepositoryProvider);
  }

  public static AISettingsViewModel newInstance(AIConfigRepository aiConfigRepository,
      AITokenUsageRepository aiTokenUsageRepository) {
    return new AISettingsViewModel(aiConfigRepository, aiTokenUsageRepository);
  }
}
