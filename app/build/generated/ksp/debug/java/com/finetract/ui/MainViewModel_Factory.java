package com.finetract.ui;

import androidx.lifecycle.SavedStateHandle;
import com.finetract.data.repository.FinanceRepository;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<FinanceRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public MainViewModel_Factory(Provider<FinanceRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static MainViewModel_Factory create(Provider<FinanceRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new MainViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static MainViewModel newInstance(FinanceRepository repository,
      SavedStateHandle savedStateHandle) {
    return new MainViewModel(repository, savedStateHandle);
  }
}
