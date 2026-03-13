package com.calorieai.app.data.repository;

import com.calorieai.app.data.local.AIChatHistoryDao;
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
public final class AIChatHistoryRepository_Factory implements Factory<AIChatHistoryRepository> {
  private final Provider<AIChatHistoryDao> aiChatHistoryDaoProvider;

  public AIChatHistoryRepository_Factory(Provider<AIChatHistoryDao> aiChatHistoryDaoProvider) {
    this.aiChatHistoryDaoProvider = aiChatHistoryDaoProvider;
  }

  @Override
  public AIChatHistoryRepository get() {
    return newInstance(aiChatHistoryDaoProvider.get());
  }

  public static AIChatHistoryRepository_Factory create(
      Provider<AIChatHistoryDao> aiChatHistoryDaoProvider) {
    return new AIChatHistoryRepository_Factory(aiChatHistoryDaoProvider);
  }

  public static AIChatHistoryRepository newInstance(AIChatHistoryDao aiChatHistoryDao) {
    return new AIChatHistoryRepository(aiChatHistoryDao);
  }
}
