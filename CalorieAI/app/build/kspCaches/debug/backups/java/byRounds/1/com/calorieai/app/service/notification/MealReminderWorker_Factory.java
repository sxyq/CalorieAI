package com.calorieai.app.service.notification;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
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
public final class MealReminderWorker_Factory {
  private final Provider<NotificationHelper> notificationHelperProvider;

  public MealReminderWorker_Factory(Provider<NotificationHelper> notificationHelperProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public MealReminderWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, notificationHelperProvider.get());
  }

  public static MealReminderWorker_Factory create(
      Provider<NotificationHelper> notificationHelperProvider) {
    return new MealReminderWorker_Factory(notificationHelperProvider);
  }

  public static MealReminderWorker newInstance(Context context, WorkerParameters params,
      NotificationHelper notificationHelper) {
    return new MealReminderWorker(context, params, notificationHelper);
  }
}
