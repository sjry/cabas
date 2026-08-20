package com.sjarry.cabas.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
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
import com.sjarry.cabas.data.entities.MenuEntryEntity;
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
public final class MenuDao_Impl implements MenuDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MenuEntryEntity> __insertionAdapterOfMenuEntryEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateServings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDone;

  private final SharedSQLiteStatement __preparedStmtOfRemoveEntry;

  private final SharedSQLiteStatement __preparedStmtOfClearMenu;

  private final Converters __converters = new Converters();

  public MenuDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMenuEntryEntity = new EntityInsertionAdapter<MenuEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `menu_entries` (`recipeId`,`servings`,`addedAt`,`done`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MenuEntryEntity entity) {
        statement.bindLong(1, entity.getRecipeId());
        statement.bindLong(2, entity.getServings());
        statement.bindLong(3, entity.getAddedAt());
        final int _tmp = entity.getDone() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__preparedStmtOfUpdateServings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE menu_entries SET servings = ? WHERE recipeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateDone = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE menu_entries SET done = ? WHERE recipeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveEntry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM menu_entries WHERE recipeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearMenu = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM menu_entries";
        return _query;
      }
    };
  }

  @Override
  public Object addEntry(final MenuEntryEntity entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMenuEntryEntity.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateServings(final long recipeId, final int servings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateServings.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, servings);
        _argIndex = 2;
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
          __preparedStmtOfUpdateServings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDone(final long recipeId, final boolean done,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDone.acquire();
        int _argIndex = 1;
        final int _tmp = done ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
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
          __preparedStmtOfUpdateDone.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removeEntry(final long recipeId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveEntry.acquire();
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
          __preparedStmtOfRemoveEntry.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearMenu(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearMenu.acquire();
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
          __preparedStmtOfClearMenu.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MenuEntryWithRecipe>> observeMenu() {
    final String _sql = "SELECT * FROM menu_entries ORDER BY addedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"ingredients", "steps", "recipes",
        "menu_entries"}, new Callable<List<MenuEntryWithRecipe>>() {
      @Override
      @NonNull
      public List<MenuEntryWithRecipe> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfRecipeId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipeId");
            final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
            final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
            final int _cursorIndexOfDone = CursorUtil.getColumnIndexOrThrow(_cursor, "done");
            final LongSparseArray<RecipeWithDetails> _collectionRecipe = new LongSparseArray<RecipeWithDetails>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfRecipeId);
              _collectionRecipe.put(_tmpKey, null);
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiprecipesAscomSjarryCabasDataDaoRecipeWithDetails(_collectionRecipe);
            final List<MenuEntryWithRecipe> _result = new ArrayList<MenuEntryWithRecipe>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final MenuEntryWithRecipe _item;
              final MenuEntryEntity _tmpEntry;
              final long _tmpRecipeId;
              _tmpRecipeId = _cursor.getLong(_cursorIndexOfRecipeId);
              final int _tmpServings;
              _tmpServings = _cursor.getInt(_cursorIndexOfServings);
              final long _tmpAddedAt;
              _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
              final boolean _tmpDone;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfDone);
              _tmpDone = _tmp != 0;
              _tmpEntry = new MenuEntryEntity(_tmpRecipeId,_tmpServings,_tmpAddedAt,_tmpDone);
              final RecipeWithDetails _tmpRecipe;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfRecipeId);
              _tmpRecipe = _collectionRecipe.get(_tmpKey_1);
              _item = new MenuEntryWithRecipe(_tmpEntry,_tmpRecipe);
              _result.add(_item);
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
  public Flow<List<Long>> observeMenuRecipeIds() {
    final String _sql = "SELECT recipeId FROM menu_entries";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"menu_entries"}, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Long> _result = new ArrayList<Long>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Long _item;
            _item = _cursor.getLong(0);
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

  private void __fetchRelationshiprecipesAscomSjarryCabasDataDaoRecipeWithDetails(
      @NonNull final LongSparseArray<RecipeWithDetails> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, false, (map) -> {
        __fetchRelationshiprecipesAscomSjarryCabasDataDaoRecipeWithDetails(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`title`,`sourceUri`,`sourceFileName`,`rawMarkdown`,`importedAt` FROM `recipes` WHERE `id` IN (");
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
    final Cursor _cursor = DBUtil.query(__db, _stmt, true, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "id");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfTitle = 1;
      final int _cursorIndexOfSourceUri = 2;
      final int _cursorIndexOfSourceFileName = 3;
      final int _cursorIndexOfRawMarkdown = 4;
      final int _cursorIndexOfImportedAt = 5;
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
      while (_cursor.moveToNext()) {
        final long _tmpKey_2;
        _tmpKey_2 = _cursor.getLong(_itemKeyIndex);
        if (_map.containsKey(_tmpKey_2)) {
          final RecipeWithDetails _item_1;
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
          final long _tmpKey_3;
          _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
          _tmpIngredientsCollection = _collectionIngredients.get(_tmpKey_3);
          final ArrayList<StepEntity> _tmpStepsCollection;
          final long _tmpKey_4;
          _tmpKey_4 = _cursor.getLong(_cursorIndexOfId);
          _tmpStepsCollection = _collectionSteps.get(_tmpKey_4);
          _item_1 = new RecipeWithDetails(_tmpRecipe,_tmpIngredientsCollection,_tmpStepsCollection);
          _map.put(_tmpKey_2, _item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
