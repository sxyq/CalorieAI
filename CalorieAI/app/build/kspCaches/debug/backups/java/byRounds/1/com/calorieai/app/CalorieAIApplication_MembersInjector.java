package com.calorieai.app;

import com.calorieai.app.service.ai.AIDefaultConfigInitializer;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class CalorieAIApplication_MembersInjector implements MembersInjector<CalorieAIApplication> {
  private final Provider<AIDefaultConfigInitializer> aiDefaultConfigInitializerProvider;

  public CalorieAIApplication_MembersInjector(
      Provider<AIDefaultConfigInitializer> aiDefaultConfigInitializerProvider) {
    this.aiDefaultConfigInitializerProvider = aiDefaultConfigInitializerProvider;
  }

  public static MembersInjector<CalorieAIApplication> create(
      Provider<AIDefaultConfigInitializer> aiDefaultConfigInitializerProvider) {
    return new CalorieAIApplication_MembersInjector(aiDefaultConfigInitializerProvider);
  }

  @Override
  public void injectMembers(CalorieAIApplication instance) {
    injectAiDefaultConfigInitializer(instance, aiDefaultConfigInitializerProvider.get());
  }

  @InjectedFieldSignature("com.calorieai.app.CalorieAIApplication.aiDefaultConfigInitializer")
  public static void injectAiDefaultConfigInitializer(CalorieAIApplication instance,
      AIDefaultConfigInitializer aiDefaultConfigInitializer) {
    instance.aiDefaultConfigInitializer = aiDefaultConfigInitializer;
  }
}
