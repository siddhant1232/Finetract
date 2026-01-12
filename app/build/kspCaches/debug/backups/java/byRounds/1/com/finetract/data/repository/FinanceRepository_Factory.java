package com.finetract.data.repository;

import com.finetract.data.local.dao.FinanceDao;
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
public final class FinanceRepository_Factory implements Factory<FinanceRepository> {
  private final Provider<FinanceDao> financeDaoProvider;

  public FinanceRepository_Factory(Provider<FinanceDao> financeDaoProvider) {
    this.financeDaoProvider = financeDaoProvider;
  }

  @Override
  public FinanceRepository get() {
    return newInstance(financeDaoProvider.get());
  }

  public static FinanceRepository_Factory create(Provider<FinanceDao> financeDaoProvider) {
    return new FinanceRepository_Factory(financeDaoProvider);
  }

  public static FinanceRepository newInstance(FinanceDao financeDao) {
    return new FinanceRepository(financeDao);
  }
}
