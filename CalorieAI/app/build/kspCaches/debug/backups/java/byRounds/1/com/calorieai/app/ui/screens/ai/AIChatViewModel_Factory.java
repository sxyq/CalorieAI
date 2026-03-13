package com.calorieai.app.ui.screens.ai;

import com.calorieai.app.data.repository.AIChatHistoryRepository;
import com.calorieai.app.service.ai.AIChatService;
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
public final class AIChatViewModel_Factory implements Factory<AIChatViewModel> {
  private final Provider<AIChatService> aiChatServiceProvider;

  private final Provider<AIChatHistoryRepository> aiChatHistoryRepositoryProvider;

  public AIChatViewModel_Factory(Provider<AIChatService> aiChatServiceProvider,
      Provider<AIChatHistoryRepository> aiChatHistoryRepositoryProvider) {
    this.aiChatServiceProvider = aiChatServiceProvider;
    this.aiChatHistoryRepositoryProvider = aiChatHistoryRepositoryProvider;
  }

  @Override
  public AIChatViewModel get() {
    return newInstance(aiChatServiceProvider.get(), aiChatHistoryRepositoryProvider.get());
  }

  public static AIChatViewModel_Factory create(Provider<AIChatService> aiChatServiceProvider,
      Provider<AIChatHistoryRepository> aiChatHistoryRepositoryProvider) {
    return new AIChatViewModel_Factory(aiChatServiceProvider, aiChatHistoryRepositoryProvider);
  }

  public static AIChatViewModel newInstance(AIChatService aiChatService,
      AIChatHistoryRepository aiChatHistoryRepository) {
    return new AIChatViewModel(aiChatService, aiChatHistoryRepository);
  }
}
