package com.calorieai.app.service.ai;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NutritionRecognitionService_Factory implements Factory<NutritionRecognitionService> {
  @Override
  public NutritionRecognitionService get() {
    return newInstance();
  }

  public static NutritionRecognitionService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NutritionRecognitionService newInstance() {
    return new NutritionRecognitionService();
  }

  private static final class InstanceHolder {
    private static final NutritionRecognitionService_Factory INSTANCE = new NutritionRecognitionService_Factory();
  }
}
