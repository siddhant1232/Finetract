package com.finetract.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.finetract.data.local.converters.Converters;
import com.finetract.data.local.entities.Budget;
import com.finetract.data.local.entities.Category;
import com.finetract.data.local.entities.Transaction;
import com.finetract.data.local.entities.TransactionType;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FinanceDao_Impl implements FinanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Transaction> __insertionAdapterOfTransaction;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<Category> __insertionAdapterOfCategory;

  private final EntityInsertionAdapter<Budget> __insertionAdapterOfBudget;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCategory;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllTransactions;

  public FinanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransaction = new EntityInsertionAdapter<Transaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `transactions` (`id`,`amount`,`timestamp`,`categoryId`,`note`,`type`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Transaction entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindLong(3, entity.getTimestamp());
        statement.bindLong(4, entity.getCategoryId());
        statement.bindString(5, entity.getNote());
        final String _tmp = __converters.fromTransactionType(entity.getType());
        statement.bindString(6, _tmp);
      }
    };
    this.__insertionAdapterOfCategory = new EntityInsertionAdapter<Category>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `categories` (`id`,`name`,`iconMetadata`,`budgetLimit`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Category entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIconMetadata());
        statement.bindDouble(4, entity.getBudgetLimit());
      }
    };
    this.__insertionAdapterOfBudget = new EntityInsertionAdapter<Budget>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `budgets` (`id`,`categoryId`,`monthlyLimit`,`monthYear`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Budget entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCategoryId());
        statement.bindDouble(3, entity.getMonthlyLimit());
        statement.bindString(4, entity.getMonthYear());
      }
    };
    this.__preparedStmtOfDeleteCategory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM categories WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllTransactions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transactions";
        return _query;
      }
    };
  }

  @Override
  public Object insertTransaction(final Transaction transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTransaction.insert(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCategory(final Category category,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCategory.insert(category);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertBudget(final Budget budget, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBudget.insert(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCategory(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCategory.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCategory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllTransactions(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllTransactions.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllTransactions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Transaction>> getAllTransactions() {
    final String _sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<Transaction>>() {
      @Override
      @NonNull
      public List<Transaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final List<Transaction> _result = new ArrayList<Transaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Transaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toTransactionType(_tmp);
            _item = new Transaction(_tmpId,_tmpAmount,_tmpTimestamp,_tmpCategoryId,_tmpNote,_tmpType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Double> getMonthlyTotal(final TransactionType type, final String monthYear) {
    final String _sql = "\n"
            + "        SELECT SUM(amount) FROM transactions \n"
            + "        WHERE type = ? AND strftime('%m-%Y', timestamp / 1000, 'unixepoch') = ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    final String _tmp = __converters.fromTransactionType(type);
    _statement.bindString(_argIndex, _tmp);
    _argIndex = 2;
    _statement.bindString(_argIndex, monthYear);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp_1;
            if (_cursor.isNull(0)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getDouble(0);
            }
            _result = _tmp_1;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CategoryTotal>> getCategoryTotalsByMonth(final String monthYear) {
    final String _sql = "\n"
            + "        SELECT categoryId, name as categoryName, SUM(amount) as totalAmount \n"
            + "        FROM transactions \n"
            + "        JOIN categories ON transactions.categoryId = categories.id\n"
            + "        WHERE type = 'EXPENSE' AND strftime('%m-%Y', timestamp / 1000, 'unixepoch') = ?\n"
            + "        GROUP BY categoryId\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthYear);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions",
        "categories"}, new Callable<List<CategoryTotal>>() {
      @Override
      @NonNull
      public List<CategoryTotal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategoryId = 0;
          final int _cursorIndexOfCategoryName = 1;
          final int _cursorIndexOfTotalAmount = 2;
          final List<CategoryTotal> _result = new ArrayList<CategoryTotal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryTotal _item;
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            _item = new CategoryTotal(_tmpCategoryId,_tmpCategoryName,_tmpTotalAmount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DailyTotal>> getDailyTotalsByMonth(final String monthYear) {
    final String _sql = "\n"
            + "        SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') as date, SUM(amount) as totalAmount\n"
            + "        FROM transactions\n"
            + "        WHERE type = 'EXPENSE' AND strftime('%m-%Y', timestamp / 1000, 'unixepoch') = ?\n"
            + "        GROUP BY date\n"
            + "        ORDER BY date ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthYear);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<DailyTotal>>() {
      @Override
      @NonNull
      public List<DailyTotal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = 0;
          final int _cursorIndexOfTotalAmount = 1;
          final List<DailyTotal> _result = new ArrayList<DailyTotal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyTotal _item;
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            _item = new DailyTotal(_tmpDate,_tmpTotalAmount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Category>> getAllCategories() {
    final String _sql = "SELECT * FROM categories";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"categories"}, new Callable<List<Category>>() {
      @Override
      @NonNull
      public List<Category> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIconMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "iconMetadata");
          final int _cursorIndexOfBudgetLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "budgetLimit");
          final List<Category> _result = new ArrayList<Category>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Category _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIconMetadata;
            _tmpIconMetadata = _cursor.getString(_cursorIndexOfIconMetadata);
            final double _tmpBudgetLimit;
            _tmpBudgetLimit = _cursor.getDouble(_cursorIndexOfBudgetLimit);
            _item = new Category(_tmpId,_tmpName,_tmpIconMetadata,_tmpBudgetLimit);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Budget>> getBudgetsByMonth(final String monthYear) {
    final String _sql = "SELECT * FROM budgets WHERE monthYear = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthYear);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<List<Budget>>() {
      @Override
      @NonNull
      public List<Budget> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfMonthlyLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "monthlyLimit");
          final int _cursorIndexOfMonthYear = CursorUtil.getColumnIndexOrThrow(_cursor, "monthYear");
          final List<Budget> _result = new ArrayList<Budget>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Budget _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final double _tmpMonthlyLimit;
            _tmpMonthlyLimit = _cursor.getDouble(_cursorIndexOfMonthlyLimit);
            final String _tmpMonthYear;
            _tmpMonthYear = _cursor.getString(_cursorIndexOfMonthYear);
            _item = new Budget(_tmpId,_tmpCategoryId,_tmpMonthlyLimit,_tmpMonthYear);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
