package com.calorieai.app.service.voice;

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
public final class VoiceInputHelper_Factory implements Factory<VoiceInputHelper> {
  @Override
  public VoiceInputHelper get() {
    return newInstance();
  }

  public static VoiceInputHelper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VoiceInputHelper newInstance() {
    return new VoiceInputHelper();
  }

  private static final class InstanceHolder {
    private static final VoiceInputHelper_Factory INSTANCE = new VoiceInputHelper_Factory();
  }
}
