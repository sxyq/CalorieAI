package com.calorieai.app.service.backup;

import android.content.Context;
import com.calorieai.app.data.local.AIConfigDao;
import com.calorieai.app.data.repository.ExerciseRecordRepository;
import com.calorieai.app.data.repository.FoodRecordRepository;
import com.calorieai.app.data.repository.UserSettingsRepository;
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
public final class BackupService_Factory implements Factory<BackupService> {
  private final Provider<Context> contextProvider;

  private final Provider<FoodRecordRepository> foodRecordRepositoryProvider;

  private final Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider;

  private final Provider<UserSettingsRepository> userSettingsRepositoryProvider;

  private final Provider<AIConfigDao> aiConfigRepositoryProvider;

  public BackupService_Factory(Provider<Context> contextProvider,
      Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider,
      Provider<AIConfigDao> aiConfigRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.foodRecordRepositoryProvider = foodRecordRepositoryProvider;
    this.exerciseRecordRepositoryProvider = exerciseRecordRepositoryProvider;
    this.userSettingsRepositoryProvider = userSettingsRepositoryProvider;
    this.aiConfigRepositoryProvider = aiConfigRepositoryProvider;
  }

  @Override
  public BackupService get() {
    return newInstance(contextProvider.get(), foodRecordRepositoryProvider.get(), exerciseRecordRepositoryProvider.get(), userSettingsRepositoryProvider.get(), aiConfigRepositoryProvider.get());
  }

  public static BackupService_Factory create(Provider<Context> contextProvider,
      Provider<FoodRecordRepository> foodRecordRepositoryProvider,
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider,
      Provider<UserSettingsRepository> userSettingsRepositoryProvider,
      Provider<AIConfigDao> aiConfigRepositoryProvider) {
    return new BackupService_Factory(contextProvider, foodRecordRepositoryProvider, exerciseRecordRepositoryProvider, userSettingsRepositoryProvider, aiConfigRepositoryProvider);
  }

  public static BackupService newInstance(Context context,
      FoodRecordRepository foodRecordRepository, ExerciseRecordRepository exerciseRecordRepository,
      UserSettingsRepository userSettingsRepository, AIConfigDao aiConfigRepository) {
    return new BackupService(context, foodRecordRepository, exerciseRecordRepository, userSettingsRepository, aiConfigRepository);
  }
}
