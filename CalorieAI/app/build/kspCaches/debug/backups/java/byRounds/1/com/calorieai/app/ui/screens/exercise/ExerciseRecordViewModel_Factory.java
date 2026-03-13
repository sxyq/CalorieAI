package com.calorieai.app.ui.screens.exercise;

import com.calorieai.app.data.repository.ExerciseRecordRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ExerciseRecordViewModel_Factory implements Factory<ExerciseRecordViewModel> {
  private final Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider;

  public ExerciseRecordViewModel_Factory(
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider) {
    this.exerciseRecordRepositoryProvider = exerciseRecordRepositoryProvider;
  }

  @Override
  public ExerciseRecordViewModel get() {
    return newInstance(exerciseRecordRepositoryProvider.get());
  }

  public static ExerciseRecordViewModel_Factory create(
      Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider) {
    return new ExerciseRecordViewModel_Factory(exerciseRecordRepositoryProvider);
  }

  public static ExerciseRecordViewModel newInstance(
      ExerciseRecordRepository exerciseRecordRepository) {
    return new ExerciseRecordViewModel(exerciseRecordRepository);
  }
}
