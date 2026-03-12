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
public final class AISettingsViewModel_Factory implements Factory<AISettingsViewModel> {
  private final Provider<AIConfigRepository> aiConfigRepositoryProvider;

  public AISettingsViewModel_Factory(Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
  }

  @Override
  public AISettingsViewModel get() {
    return newInstance(aiConfigRepositoryProvider.get());
  }

  public static AISettingsViewModel_Factory create(
      Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    return new AISettingsViewModel_Factory(aiConfigRepositoryProvider);
  }

  public static AISettingsViewModel newInstance(AIConfigRepository aiConfigRepository) {
    return new AISettingsViewModel(aiConfigRepository);
  }
}
