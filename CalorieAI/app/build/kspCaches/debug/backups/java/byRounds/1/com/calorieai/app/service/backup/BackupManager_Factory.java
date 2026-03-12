package com.calorieai.app.service.backup;

import android.content.Context;
import com.calorieai.app.data.local.FoodRecordDao;
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
public final class BackupManager_Factory implements Factory<BackupManager> {
  private final Provider<Context> contextProvider;

  private final Provider<FoodRecordDao> foodRecordDaoProvider;

  public BackupManager_Factory(Provider<Context> contextProvider,
      Provider<FoodRecordDao> foodRecordDaoProvider) {
    this.contextProvider = contextProvider;
    this.foodRecordDaoProvider = foodRecordDaoProvider;
  }

  @Override
  public BackupManager get() {
    return newInstance(contextProvider.get(), foodRecordDaoProvider.get());
  }

  public static BackupManager_Factory create(Provider<Context> contextProvider,
      Provider<FoodRecordDao> foodRecordDaoProvider) {
    return new BackupManager_Factory(contextProvider, foodRecordDaoProvider);
  }

  public static BackupManager newInstance(Context context, FoodRecordDao foodRecordDao) {
    return new BackupManager(context, foodRecordDao);
  }
}
