package com.calorieai.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.calorieai.app.data.local.AIConfigDao;
import com.calorieai.app.data.local.AITokenUsageDao;
import com.calorieai.app.data.local.AppDatabase;
import com.calorieai.app.data.local.ExerciseRecordDao;
import com.calorieai.app.data.local.FoodRecordDao;
import com.calorieai.app.data.local.UserSettingsDao;
import com.calorieai.app.data.repository.AIConfigRepository;
import com.calorieai.app.data.repository.AITokenUsageRepository;
import com.calorieai.app.data.repository.ExerciseRecordRepository;
import com.calorieai.app.data.repository.FoodRecordRepository;
import com.calorieai.app.data.repository.UserSettingsRepository;
import com.calorieai.app.data.repository.WeightRecordDao;
import com.calorieai.app.data.repository.WeightRecordRepository;
import com.calorieai.app.di.AppModule_ProvideOkHttpClientFactory;
import com.calorieai.app.di.DatabaseModule_ProvideAIConfigDaoFactory;
import com.calorieai.app.di.DatabaseModule_ProvideAITokenUsageDaoFactory;
import com.calorieai.app.di.DatabaseModule_ProvideAppDatabaseFactory;
import com.calorieai.app.di.DatabaseModule_ProvideExerciseRecordDaoFactory;
import com.calorieai.app.di.DatabaseModule_ProvideFoodRecordDaoFactory;
import com.calorieai.app.di.DatabaseModule_ProvideUserSettingsDaoFactory;
import com.calorieai.app.di.DatabaseModule_ProvideWeightRecordDaoFactory;
import com.calorieai.app.service.ai.AIChatService;
import com.calorieai.app.service.ai.AIDefaultConfigInitializer;
import com.calorieai.app.service.ai.AIRateLimiter;
import com.calorieai.app.service.ai.FoodImageAnalysisService;
import com.calorieai.app.service.backup.BackupService;
import com.calorieai.app.ui.screens.add.AddFoodViewModel;
import com.calorieai.app.ui.screens.add.AddFoodViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.add.ManualAddViewModel;
import com.calorieai.app.ui.screens.add.ManualAddViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.ai.AIChatViewModel;
import com.calorieai.app.ui.screens.ai.AIChatViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.camera.CameraViewModel;
import com.calorieai.app.ui.screens.camera.CameraViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.camera.PhotoAnalysisViewModel;
import com.calorieai.app.ui.screens.camera.PhotoAnalysisViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.home.HomeViewModel;
import com.calorieai.app.ui.screens.home.HomeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.result.ResultViewModel;
import com.calorieai.app.ui.screens.result.ResultViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.AIConfigDetailViewModel;
import com.calorieai.app.ui.screens.settings.AIConfigDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.AISettingsViewModel;
import com.calorieai.app.ui.screens.settings.AISettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.AppearanceSettingsViewModel;
import com.calorieai.app.ui.screens.settings.AppearanceSettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.BackupSettingsViewModel;
import com.calorieai.app.ui.screens.settings.BackupSettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.InteractionSettingsViewModel;
import com.calorieai.app.ui.screens.settings.InteractionSettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.NotificationSettingsViewModel;
import com.calorieai.app.ui.screens.settings.NotificationSettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.ProfileViewModel;
import com.calorieai.app.ui.screens.settings.ProfileViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.settings.SettingsViewModel;
import com.calorieai.app.ui.screens.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.calorieai.app.ui.screens.stats.StatsViewModel;
import com.calorieai.app.ui.screens.stats.StatsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SetBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class DaggerCalorieAIApplication_HiltComponents_SingletonC {
  private DaggerCalorieAIApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CalorieAIApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CalorieAIApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CalorieAIApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CalorieAIApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CalorieAIApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CalorieAIApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CalorieAIApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CalorieAIApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CalorieAIApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CalorieAIApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CalorieAIApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CalorieAIApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CalorieAIApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return SetBuilder.<String>newSetBuilder(16).add(AIChatViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(AIConfigDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(AISettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(AddFoodViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(AppearanceSettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(BackupSettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(CameraViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(HomeViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(InteractionSettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(ManualAddViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(NotificationSettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(PhotoAnalysisViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(ProfileViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(ResultViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(StatsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).build();
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectUserSettingsRepository(instance, singletonCImpl.userSettingsRepositoryProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends CalorieAIApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AIChatViewModel> aIChatViewModelProvider;

    private Provider<AIConfigDetailViewModel> aIConfigDetailViewModelProvider;

    private Provider<AISettingsViewModel> aISettingsViewModelProvider;

    private Provider<AddFoodViewModel> addFoodViewModelProvider;

    private Provider<AppearanceSettingsViewModel> appearanceSettingsViewModelProvider;

    private Provider<BackupSettingsViewModel> backupSettingsViewModelProvider;

    private Provider<CameraViewModel> cameraViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<InteractionSettingsViewModel> interactionSettingsViewModelProvider;

    private Provider<ManualAddViewModel> manualAddViewModelProvider;

    private Provider<NotificationSettingsViewModel> notificationSettingsViewModelProvider;

    private Provider<PhotoAnalysisViewModel> photoAnalysisViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<ResultViewModel> resultViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StatsViewModel> statsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.aIChatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.aIConfigDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.aISettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.addFoodViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.appearanceSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.backupSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.cameraViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.interactionSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.manualAddViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.notificationSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.photoAnalysisViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.resultViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.statsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(16).put("com.calorieai.app.ui.screens.ai.AIChatViewModel", ((Provider) aIChatViewModelProvider)).put("com.calorieai.app.ui.screens.settings.AIConfigDetailViewModel", ((Provider) aIConfigDetailViewModelProvider)).put("com.calorieai.app.ui.screens.settings.AISettingsViewModel", ((Provider) aISettingsViewModelProvider)).put("com.calorieai.app.ui.screens.add.AddFoodViewModel", ((Provider) addFoodViewModelProvider)).put("com.calorieai.app.ui.screens.settings.AppearanceSettingsViewModel", ((Provider) appearanceSettingsViewModelProvider)).put("com.calorieai.app.ui.screens.settings.BackupSettingsViewModel", ((Provider) backupSettingsViewModelProvider)).put("com.calorieai.app.ui.screens.camera.CameraViewModel", ((Provider) cameraViewModelProvider)).put("com.calorieai.app.ui.screens.home.HomeViewModel", ((Provider) homeViewModelProvider)).put("com.calorieai.app.ui.screens.settings.InteractionSettingsViewModel", ((Provider) interactionSettingsViewModelProvider)).put("com.calorieai.app.ui.screens.add.ManualAddViewModel", ((Provider) manualAddViewModelProvider)).put("com.calorieai.app.ui.screens.settings.NotificationSettingsViewModel", ((Provider) notificationSettingsViewModelProvider)).put("com.calorieai.app.ui.screens.camera.PhotoAnalysisViewModel", ((Provider) photoAnalysisViewModelProvider)).put("com.calorieai.app.ui.screens.settings.ProfileViewModel", ((Provider) profileViewModelProvider)).put("com.calorieai.app.ui.screens.result.ResultViewModel", ((Provider) resultViewModelProvider)).put("com.calorieai.app.ui.screens.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.calorieai.app.ui.screens.stats.StatsViewModel", ((Provider) statsViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return Collections.<String, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.calorieai.app.ui.screens.ai.AIChatViewModel 
          return (T) new AIChatViewModel(singletonCImpl.foodRecordRepositoryProvider.get(), singletonCImpl.userSettingsRepositoryProvider.get(), singletonCImpl.aIChatServiceProvider.get());

          case 1: // com.calorieai.app.ui.screens.settings.AIConfigDetailViewModel 
          return (T) new AIConfigDetailViewModel(singletonCImpl.aIConfigRepositoryProvider.get());

          case 2: // com.calorieai.app.ui.screens.settings.AISettingsViewModel 
          return (T) new AISettingsViewModel(singletonCImpl.aIConfigRepositoryProvider.get(), singletonCImpl.aITokenUsageRepositoryProvider.get());

          case 3: // com.calorieai.app.ui.screens.add.AddFoodViewModel 
          return (T) new AddFoodViewModel(singletonCImpl.foodRecordRepositoryProvider.get());

          case 4: // com.calorieai.app.ui.screens.settings.AppearanceSettingsViewModel 
          return (T) new AppearanceSettingsViewModel(singletonCImpl.userSettingsRepositoryProvider.get());

          case 5: // com.calorieai.app.ui.screens.settings.BackupSettingsViewModel 
          return (T) new BackupSettingsViewModel(singletonCImpl.backupServiceProvider.get());

          case 6: // com.calorieai.app.ui.screens.camera.CameraViewModel 
          return (T) new CameraViewModel();

          case 7: // com.calorieai.app.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.foodRecordRepositoryProvider.get(), singletonCImpl.userSettingsRepositoryProvider.get(), singletonCImpl.exerciseRecordRepositoryProvider.get(), singletonCImpl.weightRecordRepositoryProvider.get());

          case 8: // com.calorieai.app.ui.screens.settings.InteractionSettingsViewModel 
          return (T) new InteractionSettingsViewModel(singletonCImpl.userSettingsRepositoryProvider.get());

          case 9: // com.calorieai.app.ui.screens.add.ManualAddViewModel 
          return (T) new ManualAddViewModel(singletonCImpl.foodRecordRepositoryProvider.get());

          case 10: // com.calorieai.app.ui.screens.settings.NotificationSettingsViewModel 
          return (T) new NotificationSettingsViewModel(singletonCImpl.userSettingsRepositoryProvider.get());

          case 11: // com.calorieai.app.ui.screens.camera.PhotoAnalysisViewModel 
          return (T) new PhotoAnalysisViewModel(singletonCImpl.foodImageAnalysisServiceProvider.get(), singletonCImpl.foodRecordRepositoryProvider.get());

          case 12: // com.calorieai.app.ui.screens.settings.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.userSettingsRepositoryProvider.get());

          case 13: // com.calorieai.app.ui.screens.result.ResultViewModel 
          return (T) new ResultViewModel(singletonCImpl.foodRecordRepositoryProvider.get());

          case 14: // com.calorieai.app.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.userSettingsRepositoryProvider.get());

          case 15: // com.calorieai.app.ui.screens.stats.StatsViewModel 
          return (T) new StatsViewModel(singletonCImpl.foodRecordRepositoryProvider.get(), singletonCImpl.exerciseRecordRepositoryProvider.get(), singletonCImpl.userSettingsRepositoryProvider.get(), singletonCImpl.weightRecordRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CalorieAIApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CalorieAIApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CalorieAIApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<AIDefaultConfigInitializer> aIDefaultConfigInitializerProvider;

    private Provider<UserSettingsRepository> userSettingsRepositoryProvider;

    private Provider<FoodRecordRepository> foodRecordRepositoryProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<AIConfigRepository> aIConfigRepositoryProvider;

    private Provider<AITokenUsageRepository> aITokenUsageRepositoryProvider;

    private Provider<AIRateLimiter> aIRateLimiterProvider;

    private Provider<AIChatService> aIChatServiceProvider;

    private Provider<ExerciseRecordRepository> exerciseRecordRepositoryProvider;

    private Provider<BackupService> backupServiceProvider;

    private Provider<WeightRecordRepository> weightRecordRepositoryProvider;

    private Provider<FoodImageAnalysisService> foodImageAnalysisServiceProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private AIConfigDao aIConfigDao() {
      return DatabaseModule_ProvideAIConfigDaoFactory.provideAIConfigDao(provideAppDatabaseProvider.get());
    }

    private UserSettingsDao userSettingsDao() {
      return DatabaseModule_ProvideUserSettingsDaoFactory.provideUserSettingsDao(provideAppDatabaseProvider.get());
    }

    private FoodRecordDao foodRecordDao() {
      return DatabaseModule_ProvideFoodRecordDaoFactory.provideFoodRecordDao(provideAppDatabaseProvider.get());
    }

    private AITokenUsageDao aITokenUsageDao() {
      return DatabaseModule_ProvideAITokenUsageDaoFactory.provideAITokenUsageDao(provideAppDatabaseProvider.get());
    }

    private ExerciseRecordDao exerciseRecordDao() {
      return DatabaseModule_ProvideExerciseRecordDaoFactory.provideExerciseRecordDao(provideAppDatabaseProvider.get());
    }

    private WeightRecordDao weightRecordDao() {
      return DatabaseModule_ProvideWeightRecordDaoFactory.provideWeightRecordDao(provideAppDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.aIDefaultConfigInitializerProvider = DoubleCheck.provider(new SwitchingProvider<AIDefaultConfigInitializer>(singletonCImpl, 0));
      this.userSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserSettingsRepository>(singletonCImpl, 2));
      this.foodRecordRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FoodRecordRepository>(singletonCImpl, 3));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.aIConfigRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AIConfigRepository>(singletonCImpl, 6));
      this.aITokenUsageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AITokenUsageRepository>(singletonCImpl, 7));
      this.aIRateLimiterProvider = DoubleCheck.provider(new SwitchingProvider<AIRateLimiter>(singletonCImpl, 8));
      this.aIChatServiceProvider = DoubleCheck.provider(new SwitchingProvider<AIChatService>(singletonCImpl, 4));
      this.exerciseRecordRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ExerciseRecordRepository>(singletonCImpl, 10));
      this.backupServiceProvider = DoubleCheck.provider(new SwitchingProvider<BackupService>(singletonCImpl, 9));
      this.weightRecordRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<WeightRecordRepository>(singletonCImpl, 11));
      this.foodImageAnalysisServiceProvider = DoubleCheck.provider(new SwitchingProvider<FoodImageAnalysisService>(singletonCImpl, 12));
    }

    @Override
    public void injectCalorieAIApplication(CalorieAIApplication calorieAIApplication) {
      injectCalorieAIApplication2(calorieAIApplication);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private CalorieAIApplication injectCalorieAIApplication2(CalorieAIApplication instance) {
      CalorieAIApplication_MembersInjector.injectAiDefaultConfigInitializer(instance, aIDefaultConfigInitializerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.calorieai.app.service.ai.AIDefaultConfigInitializer 
          return (T) new AIDefaultConfigInitializer(singletonCImpl.aIConfigDao());

          case 1: // com.calorieai.app.data.local.AppDatabase 
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.calorieai.app.data.repository.UserSettingsRepository 
          return (T) new UserSettingsRepository(singletonCImpl.userSettingsDao());

          case 3: // com.calorieai.app.data.repository.FoodRecordRepository 
          return (T) new FoodRecordRepository(singletonCImpl.foodRecordDao());

          case 4: // com.calorieai.app.service.ai.AIChatService 
          return (T) new AIChatService(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.aIConfigRepositoryProvider.get(), singletonCImpl.aITokenUsageRepositoryProvider.get(), singletonCImpl.aIRateLimiterProvider.get());

          case 5: // okhttp3.OkHttpClient 
          return (T) AppModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 6: // com.calorieai.app.data.repository.AIConfigRepository 
          return (T) new AIConfigRepository(singletonCImpl.aIConfigDao());

          case 7: // com.calorieai.app.data.repository.AITokenUsageRepository 
          return (T) new AITokenUsageRepository(singletonCImpl.aITokenUsageDao());

          case 8: // com.calorieai.app.service.ai.AIRateLimiter 
          return (T) new AIRateLimiter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.calorieai.app.service.backup.BackupService 
          return (T) new BackupService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.foodRecordRepositoryProvider.get(), singletonCImpl.exerciseRecordRepositoryProvider.get(), singletonCImpl.userSettingsRepositoryProvider.get(), singletonCImpl.aIConfigDao());

          case 10: // com.calorieai.app.data.repository.ExerciseRecordRepository 
          return (T) new ExerciseRecordRepository(singletonCImpl.exerciseRecordDao());

          case 11: // com.calorieai.app.data.repository.WeightRecordRepository 
          return (T) new WeightRecordRepository(singletonCImpl.weightRecordDao());

          case 12: // com.calorieai.app.service.ai.FoodImageAnalysisService 
          return (T) new FoodImageAnalysisService(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.aIConfigRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
