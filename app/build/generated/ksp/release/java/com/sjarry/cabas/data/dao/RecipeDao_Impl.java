package com.sjarry.cabas.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.sjarry.cabas.data.Converters;
import com.sjarry.cabas.data.entities.IngredientEntity;
import com.sjarry.cabas.data.entities.RecipeEntity;
import com.sjarry.cabas.data.entities.StepEntity;
import com.sjarry.cabas.parser.IngredientUnit;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class RecipeDao_Impl implements RecipeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecipeEntity> __insertionAdapterOfRecipeEntity;

  private final EntityInsertionAdapter<IngredientEntity> __insertionAdapterOfIngredientEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<StepEntity> __insertionAdapterOfStepEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRecipe;

  private final SharedSQLiteStatement __preparedStmtOfDeleteIngredientsOf;

  private final SharedSQLiteStatement __preparedStmtOfDeleteStepsOf;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecipe;

  public RecipeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecipeEntity = new EntityInsertionAdapter<RecipeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `recipes` (`id`,`title`,`sourceUri`,`sourceFileName`,`rawMarkdown`,`importedAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecipeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getSourceUri() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSourceUri());
        }
        if (entity.getSourceFileName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSourceFileName());
        }
        statement.bindString(5, entity.getRawMarkdown());
        statement.bindLong(6, entity.getImportedAt());
      }
    };
    this.__insertionAdapterOfIngredientEntity = new EntityInsertionAdapter<IngredientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ingredients` (`id`,`recipeId`,`name`,`quantity`,`unit`,`freeUnitLabel`,`unspecified`,`rawLine`,`position`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IngredientEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecipeId());
        statement.bindString(3, entity.getName());
        statement.bindDouble(4, entity.getQuantity());
        final String _tmp = __converters.unitToString(entity.getUnit());
        statement.bindString(5, _tmp);
        if (entity.getFreeUnitLabel() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getFreeUnitLabel());
        }
        final int _tmp_1 = entity.getUnspecified() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindString(8, entity.getRawLine());
        statement.bindLong(9, entity.getPosition());
      }
    };
    this.__insertionAdapterOfStepEntity = new EntityInsertionAdapter<StepEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `steps` (`id`,`recipeId`,`text`,`position`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StepEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecipeId());
        statement.bindString(3, entity.getText());
        statement.bindLong(4, entity.getPosition());
      }
    };
    this.__preparedStmtOfUpdateRecipe = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recipes SET title = ?, rawMarkdown = ?, sourceFileName = ?, importedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteIngredientsOf = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ingredients WHERE recipeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteStepsOf = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM steps WHERE recipeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteRecipe = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recipes WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecipe(final RecipeEntity recipe,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRecipeEntity.insertAndReturnId(recipe);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertIngredients(final List<IngredientEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIngredientEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSteps(final List<StepEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStepEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecipe(final long id, final String title, final String markdown,
      final String fileName, final long importedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRecipe.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, title);
        _argIndex = 2;
        _stmt.bindString(_argIndex, markdown);
        _argIndex = 3;
        if (fileName == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, fileName);
        }
        _argIndex = 4;
        _stmt.bindLong(_argIndex, importedAt);
        _argIndex = 5;
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
          __preparedStmtOfUpdateRecipe.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteIngredientsOf(final long recipeId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteIngredientsOf.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, recipeId);
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
          __preparedStmtOfDeleteIngredientsOf.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStepsOf(final long recipeId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteStepsOf.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, recipeId);
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
          __preparedStmtOfDeleteStepsOf.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecipe(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecipe.acquire();
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
          __preparedStmtOfDeleteRecipe.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RecipeEntity>> observeAll() {
    final String _sql = "SELECT * FROM recipes ORDER BY title COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recipes"}, new Callable<List<RecipeEntity>>() {
      @Override
      @NonNull
      public List<RecipeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSourceUri = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUri");
          final int _cursorIndexOfSourceFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceFileName");
          final int _cursorIndexOfRawMarkdown = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMarkdown");
          final int _cursorIndexOfImportedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "importedAt");
          final List<RecipeEntity> _result = new ArrayList<RecipeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecipeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSourceUri;
            if (_cursor.isNull(_cursorIndexOfSourceUri)) {
              _tmpSourceUri = null;
            } else {
              _tmpSourceUri = _cursor.getString(_cursorIndexOfSourceUri);
            }
            final String _tmpSourceFileName;
            if (_cursor.isNull(_cursorIndexOfSourceFileName)) {
              _tmpSourceFileName = null;
            } else {
              _tmpSourceFileName = _cursor.getString(_cursorIndexOfSourceFileName);
            }
            final String _tmpRawMarkdown;
            _tmpRawMarkdown = _cursor.getString(_cursorIndexOfRawMarkdown);
            final long _tmpImportedAt;
            _tmpImportedAt = _cursor.getLong(_cursorIndexOfImportedAt);
            _item = new RecipeEntity(_tmpId,_tmpTitle,_tmpSourceUri,_tmpSourceFileName,_tmpRawMarkdown,_tmpImportedAt);
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
  public Flow<RecipeWithDetails> observeWithDetails(final long id) {
    final String _sql = "SELECT * FROM recipes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"ingredients", "steps",
        "recipes"}, new Callable<RecipeWithDetails>() {
      @Override
      @Nullable
      public RecipeWithDetails call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfSourceUri = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUri");
            final int _cursorIndexOfSourceFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceFileName");
            final int _cursorIndexOfRawMarkdown = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMarkdown");
            final int _cursorIndexOfImportedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "importedAt");
            final LongSparseArray<ArrayList<IngredientEntity>> _collectionIngredients = new LongSparseArray<ArrayList<IngredientEntity>>();
            final LongSparseArray<ArrayList<StepEntity>> _collectionSteps = new LongSparseArray<ArrayList<StepEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionIngredients.containsKey(_tmpKey)) {
                _collectionIngredients.put(_tmpKey, new ArrayList<IngredientEntity>());
              }
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionSteps.containsKey(_tmpKey_1)) {
                _collectionSteps.put(_tmpKey_1, new ArrayList<StepEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipingredientsAscomSjarryCabasDataEntitiesIngredientEntity(_collectionIngredients);
            __fetchRelationshipstepsAscomSjarryCabasDataEntitiesStepEntity(_collectionSteps);
            final RecipeWithDetails _result;
            if (_cursor.moveToFirst()) {
              final RecipeEntity _tmpRecipe;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpSourceUri;
              if (_cursor.isNull(_cursorIndexOfSourceUri)) {
                _tmpSourceUri = null;
              } else {
                _tmpSourceUri = _cursor.getString(_cursorIndexOfSourceUri);
              }
              final String _tmpSourceFileName;
              if (_cursor.isNull(_cursorIndexOfSourceFileName)) {
                _tmpSourceFileName = null;
              } else {
                _tmpSourceFileName = _cursor.getString(_cursorIndexOfSourceFileName);
              }
              final String _tmpRawMarkdown;
              _tmpRawMarkdown = _cursor.getString(_cursorIndexOfRawMarkdown);
              final long _tmpImportedAt;
              _tmpImportedAt = _cursor.getLong(_cursorIndexOfImportedAt);
              _tmpRecipe = new RecipeEntity(_tmpId,_tmpTitle,_tmpSourceUri,_tmpSourceFileName,_tmpRawMarkdown,_tmpImportedAt);
              final ArrayList<IngredientEntity> _tmpIngredientsCollection;
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              _tmpIngredientsCollection = _collectionIngredients.get(_tmpKey_2);
              final ArrayList<StepEntity> _tmpStepsCollection;
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              _tmpStepsCollection = _collectionSteps.get(_tmpKey_3);
              _result = new RecipeWithDetails(_tmpRecipe,_tmpIngredientsCollection,_tmpStepsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object findBySourceUri(final String uri,
      final Continuation<? super RecipeEntity> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE sourceUri = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uri);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecipeEntity>() {
      @Override
      @Nullable
      public RecipeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSourceUri = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUri");
          final int _cursorIndexOfSourceFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceFileName");
          final int _cursorIndexOfRawMarkdown = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMarkdown");
          final int _cursorIndexOfImportedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "importedAt");
          final RecipeEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSourceUri;
            if (_cursor.isNull(_cursorIndexOfSourceUri)) {
              _tmpSourceUri = null;
            } else {
              _tmpSourceUri = _cursor.getString(_cursorIndexOfSourceUri);
            }
            final String _tmpSourceFileName;
            if (_cursor.isNull(_cursorIndexOfSourceFileName)) {
              _tmpSourceFileName = null;
            } else {
              _tmpSourceFileName = _cursor.getString(_cursorIndexOfSourceFileName);
            }
            final String _tmpRawMarkdown;
            _tmpRawMarkdown = _cursor.getString(_cursorIndexOfRawMarkdown);
            final long _tmpImportedAt;
            _tmpImportedAt = _cursor.getLong(_cursorIndexOfImportedAt);
            _result = new RecipeEntity(_tmpId,_tmpTitle,_tmpSourceUri,_tmpSourceFileName,_tmpRawMarkdown,_tmpImportedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findPastedByTitle(final String title,
      final Continuation<? super RecipeEntity> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE title = ? AND sourceUri IS NULL LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, title);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecipeEntity>() {
      @Override
      @Nullable
      public RecipeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSourceUri = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUri");
          final int _cursorIndexOfSourceFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceFileName");
          final int _cursorIndexOfRawMarkdown = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMarkdown");
          final int _cursorIndexOfImportedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "importedAt");
          final RecipeEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSourceUri;
            if (_cursor.isNull(_cursorIndexOfSourceUri)) {
              _tmpSourceUri = null;
            } else {
              _tmpSourceUri = _cursor.getString(_cursorIndexOfSourceUri);
            }
            final String _tmpSourceFileName;
            if (_cursor.isNull(_cursorIndexOfSourceFileName)) {
              _tmpSourceFileName = null;
            } else {
              _tmpSourceFileName = _cursor.getString(_cursorIndexOfSourceFileName);
            }
            final String _tmpRawMarkdown;
            _tmpRawMarkdown = _cursor.getString(_cursorIndexOfRawMarkdown);
            final long _tmpImportedAt;
            _tmpImportedAt = _cursor.getLong(_cursorIndexOfImportedAt);
            _result = new RecipeEntity(_tmpId,_tmpTitle,_tmpSourceUri,_tmpSourceFileName,_tmpRawMarkdown,_tmpImportedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object allSourceUris(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT sourceUri FROM recipes WHERE sourceUri IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            if (_cursor.isNull(0)) {
              _item = null;
            } else {
              _item = _cursor.getString(0);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBySourceUris(final List<String> uris,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM recipes WHERE sourceUri IN (");
        final int _inputSize = uris.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : uris) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipingredientsAscomSjarryCabasDataEntitiesIngredientEntity(
      @NonNull final LongSparseArray<ArrayList<IngredientEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipingredientsAscomSjarryCabasDataEntitiesIngredientEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`recipeId`,`name`,`quantity`,`unit`,`freeUnitLabel`,`unspecified`,`rawLine`,`position` FROM `ingredients` WHERE `recipeId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "recipeId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfRecipeId = 1;
      final int _cursorIndexOfName = 2;
      final int _cursorIndexOfQuantity = 3;
      final int _cursorIndexOfUnit = 4;
      final int _cursorIndexOfFreeUnitLabel = 5;
      final int _cursorIndexOfUnspecified = 6;
      final int _cursorIndexOfRawLine = 7;
      final int _cursorIndexOfPosition = 8;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<IngredientEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final IngredientEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpRecipeId;
          _tmpRecipeId = _cursor.getLong(_cursorIndexOfRecipeId);
          final String _tmpName;
          _tmpName = _cursor.getString(_cursorIndexOfName);
          final double _tmpQuantity;
          _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
          final IngredientUnit _tmpUnit;
          final String _tmp;
          _tmp = _cursor.getString(_cursorIndexOfUnit);
          _tmpUnit = __converters.stringToUnit(_tmp);
          final String _tmpFreeUnitLabel;
          if (_cursor.isNull(_cursorIndexOfFreeUnitLabel)) {
            _tmpFreeUnitLabel = null;
          } else {
            _tmpFreeUnitLabel = _cursor.getString(_cursorIndexOfFreeUnitLabel);
          }
          final boolean _tmpUnspecified;
          final int _tmp_1;
          _tmp_1 = _cursor.getInt(_cursorIndexOfUnspecified);
          _tmpUnspecified = _tmp_1 != 0;
          final String _tmpRawLine;
          _tmpRawLine = _cursor.getString(_cursorIndexOfRawLine);
          final int _tmpPosition;
          _tmpPosition = _cursor.getInt(_cursorIndexOfPosition);
          _item_1 = new IngredientEntity(_tmpId,_tmpRecipeId,_tmpName,_tmpQuantity,_tmpUnit,_tmpFreeUnitLabel,_tmpUnspecified,_tmpRawLine,_tmpPosition);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipstepsAscomSjarryCabasDataEntitiesStepEntity(
      @NonNull final LongSparseArray<ArrayList<StepEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipstepsAscomSjarryCabasDataEntitiesStepEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`recipeId`,`text`,`position` FROM `steps` WHERE `recipeId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "recipeId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfRecipeId = 1;
      final int _cursorIndexOfText = 2;
      final int _cursorIndexOfPosition = 3;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<StepEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final StepEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpRecipeId;
          _tmpRecipeId = _cursor.getLong(_cursorIndexOfRecipeId);
          final String _tmpText;
          _tmpText = _cursor.getString(_cursorIndexOfText);
          final int _tmpPosition;
          _tmpPosition = _cursor.getInt(_cursorIndexOfPosition);
          _item_1 = new StepEntity(_tmpId,_tmpRecipeId,_tmpText,_tmpPosition);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
