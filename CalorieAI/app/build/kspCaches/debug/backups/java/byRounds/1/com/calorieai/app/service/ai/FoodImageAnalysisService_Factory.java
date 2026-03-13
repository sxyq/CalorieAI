package com.calorieai.app.service.ai;

import com.calorieai.app.data.repository.AIConfigRepository;
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
public final class FoodImageAnalysisService_Factory implements Factory<FoodImageAnalysisService> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<AIConfigRepository> aiConfigRepositoryProvider;

  public FoodImageAnalysisService_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
  }

  @Override
  public FoodImageAnalysisService get() {
    return newInstance(okHttpClientProvider.get(), aiConfigRepositoryProvider.get());
  }

  public static FoodImageAnalysisService_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<AIConfigRepository> aiConfigRepositoryProvider) {
    return new FoodImageAnalysisService_Factory(okHttpClientProvider, aiConfigRepositoryProvider);
  }

  public static FoodImageAnalysisService newInstance(OkHttpClient okHttpClient,
      AIConfigRepository aiConfigRepository) {
    return new FoodImageAnalysisService(okHttpClient, aiConfigRepository);
  }
}
