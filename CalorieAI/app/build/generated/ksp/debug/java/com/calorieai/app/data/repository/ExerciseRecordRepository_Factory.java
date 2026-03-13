package com.calorieai.app.data.repository;

import com.calorieai.app.data.local.ExerciseRecordDao;
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
public final class ExerciseRecordRepository_Factory implements Factory<ExerciseRecordRepository> {
  private final Provider<ExerciseRecordDao> exerciseRecordDaoProvider;

  public ExerciseRecordRepository_Factory(Provider<ExerciseRecordDao> exerciseRecordDaoProvider) {
    this.exerciseRecordDaoProvider = exerciseRecordDaoProvider;
  }

  @Override
  public ExerciseRecordRepository get() {
    return newInstance(exerciseRecordDaoProvider.get());
  }

  public static ExerciseRecordRepository_Factory create(
      Provider<ExerciseRecordDao> exerciseRecordDaoProvider) {
    return new ExerciseRecordRepository_Factory(exerciseRecordDaoProvider);
  }

  public static ExerciseRecordRepository newInstance(ExerciseRecordDao exerciseRecordDao) {
    return new ExerciseRecordRepository(exerciseRecordDao);
  }
}
