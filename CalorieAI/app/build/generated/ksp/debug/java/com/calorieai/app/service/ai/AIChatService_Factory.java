package com.calorieai.app.service.ai;

import com.calorieai.app.data.repository.AIConfigRepository;
import com.calorieai.app.data.repository.AITokenUsageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class AIChatService_Factory implements Factory<AIChatService> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<AIConfigRepository> aiConfigRepositoryProvider;

  private final Provider<AITokenUsageRepository> aiTokenUsageRepositoryProvider;

  private final Provider<AIRateLimiter> aiRateLimiterProvider;

  public AIChatService_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<AIConfigRepository> aiConfigRepositoryProvider,
      Provider<AITokenUsageRepository> aiTokenUsageRepositoryProvider,
      Provider<AIRateLimiter> aiRateLimiterProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
    this.aiTokenUsageRepositoryProvider = aiTokenUsageRepositoryProvider;
    this.aiRateLimiterProvider = aiRateLimiterProvider;
  }

  @Override
  public AIChatService get() {
    return newInstance(okHttpClientProvider.get(), aiConfigRepositoryProvider.get(), aiTokenUsageRepositoryProvider.get(), aiRateLimiterProvider.get());
  }

  public static AIChatService_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<AIConfigRepository> aiConfigRepositoryProvider,
      Provider<AITokenUsageRepository> aiTokenUsageRepositoryProvider,
      Provider<AIRateLimiter> aiRateLimiterProvider) {
    return new AIChatService_Factory(okHttpClientProvider, aiConfigRepositoryProvider, aiTokenUsageRepositoryProvider, aiRateLimiterProvider);
  }

  public static AIChatService newInstance(OkHttpClient okHttpClient,
      AIConfigRepository aiConfigRepository, AITokenUsageRepository aiTokenUsageRepository,
      AIRateLimiter aiRateLimiter) {
    return new AIChatService(okHttpClient, aiConfigRepository, aiTokenUsageRepository, aiRateLimiter);
  }
}
