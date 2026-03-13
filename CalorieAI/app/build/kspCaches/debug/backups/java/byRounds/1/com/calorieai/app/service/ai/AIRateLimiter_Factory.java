package com.calorieai.app.service.ai;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AIRateLimiter_Factory implements Factory<AIRateLimiter> {
  private final Provider<Context> contextProvider;

  public AIRateLimiter_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AIRateLimiter get() {
    return newInstance(contextProvider.get());
  }

  public static AIRateLimiter_Factory create(Provider<Context> contextProvider) {
    return new AIRateLimiter_Factory(contextProvider);
  }

  public static AIRateLimiter newInstance(Context context) {
    return new AIRateLimiter(context);
  }
}
