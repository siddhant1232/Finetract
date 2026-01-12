package com.finetract.di;

import com.finetract.data.local.AppDatabase;
import com.finetract.data.local.dao.FinanceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideFinanceDaoFactory implements Factory<FinanceDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideFinanceDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public FinanceDao get() {
    return provideFinanceDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideFinanceDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFinanceDaoFactory(databaseProvider);
  }

  public static FinanceDao provideFinanceDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFinanceDao(database));
  }
}
