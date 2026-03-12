package com.calorieai.app.service.notification;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MealReminderWorker_AssistedFactory_Impl implements MealReminderWorker_AssistedFactory {
  private final MealReminderWorker_Factory delegateFactory;

  MealReminderWorker_AssistedFactory_Impl(MealReminderWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public MealReminderWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<MealReminderWorker_AssistedFactory> create(
      MealReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MealReminderWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<MealReminderWorker_AssistedFactory> createFactoryProvider(
      MealReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MealReminderWorker_AssistedFactory_Impl(delegateFactory));
  }
}
