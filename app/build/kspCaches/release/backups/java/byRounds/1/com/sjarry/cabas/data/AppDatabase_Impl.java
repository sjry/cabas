package com.sjarry.cabas.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.sjarry.cabas.data.dao.CheckedItemDao;
import com.sjarry.cabas.data.dao.CheckedItemDao_Impl;
import com.sjarry.cabas.data.dao.MenuDao;
import com.sjarry.cabas.data.dao.MenuDao_Impl;
import com.sjarry.cabas.data.dao.RecipeDao;
import com.sjarry.cabas.data.dao.RecipeDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RecipeDao _recipeDao;

  private volatile MenuDao _menuDao;

  private volatile CheckedItemDao _checkedItemDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `recipes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `sourceUri` TEXT, `sourceFileName` TEXT, `rawMarkdown` TEXT NOT NULL, `importedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_recipes_sourceUri` ON `recipes` (`sourceUri`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ingredients` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER NOT NULL, `name` TEXT NOT NULL, `quantity` REAL NOT NULL, `unit` TEXT NOT NULL, `freeUnitLabel` TEXT, `unspecified` INTEGER NOT NULL, `rawLine` TEXT NOT NULL, `position` INTEGER NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingredients_recipeId` ON `ingredients` (`recipeId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `steps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER NOT NULL, `text` TEXT NOT NULL, `position` INTEGER NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_steps_recipeId` ON `steps` (`recipeId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `menu_entries` (`recipeId` INTEGER NOT NULL, `servings` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, `done` INTEGER NOT NULL, PRIMARY KEY(`recipeId`), FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `checked_items` (`itemKey` TEXT NOT NULL, PRIMARY KEY(`itemKey`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '55cee5517a97dd52e62c2c89eef9e7c9')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `recipes`");
        db.execSQL("DROP TABLE IF EXISTS `ingredients`");
        db.execSQL("DROP TABLE IF EXISTS `steps`");
        db.execSQL("DROP TABLE IF EXISTS `menu_entries`");
        db.execSQL("DROP TABLE IF EXISTS `checked_items`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRecipes = new HashMap<String, TableInfo.Column>(6);
        _columnsRecipes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecipes.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecipes.put("sourceUri", new TableInfo.Column("sourceUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecipes.put("sourceFileName", new TableInfo.Column("sourceFileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecipes.put("rawMarkdown", new TableInfo.Column("rawMarkdown", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecipes.put("importedAt", new TableInfo.Column("importedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecipes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecipes = new HashSet<TableInfo.Index>(1);
        _indicesRecipes.add(new TableInfo.Index("index_recipes_sourceUri", true, Arrays.asList("sourceUri"), Arrays.asList("ASC")));
        final TableInfo _infoRecipes = new TableInfo("recipes", _columnsRecipes, _foreignKeysRecipes, _indicesRecipes);
        final TableInfo _existingRecipes = TableInfo.read(db, "recipes");
        if (!_infoRecipes.equals(_existingRecipes)) {
          return new RoomOpenHelper.ValidationResult(false, "recipes(com.sjarry.cabas.data.entities.RecipeEntity).\n"
                  + " Expected:\n" + _infoRecipes + "\n"
                  + " Found:\n" + _existingRecipes);
        }
        final HashMap<String, TableInfo.Column> _columnsIngredients = new HashMap<String, TableInfo.Column>(9);
        _columnsIngredients.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("recipeId", new TableInfo.Column("recipeId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("quantity", new TableInfo.Column("quantity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("freeUnitLabel", new TableInfo.Column("freeUnitLabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("unspecified", new TableInfo.Column("unspecified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("rawLine", new TableInfo.Column("rawLine", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("position", new TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIngredients = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysIngredients.add(new TableInfo.ForeignKey("recipes", "CASCADE", "NO ACTION", Arrays.asList("recipeId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesIngredients = new HashSet<TableInfo.Index>(1);
        _indicesIngredients.add(new TableInfo.Index("index_ingredients_recipeId", false, Arrays.asList("recipeId"), Arrays.asList("ASC")));
        final TableInfo _infoIngredients = new TableInfo("ingredients", _columnsIngredients, _foreignKeysIngredients, _indicesIngredients);
        final TableInfo _existingIngredients = TableInfo.read(db, "ingredients");
        if (!_infoIngredients.equals(_existingIngredients)) {
          return new RoomOpenHelper.ValidationResult(false, "ingredients(com.sjarry.cabas.data.entities.IngredientEntity).\n"
                  + " Expected:\n" + _infoIngredients + "\n"
                  + " Found:\n" + _existingIngredients);
        }
        final HashMap<String, TableInfo.Column> _columnsSteps = new HashMap<String, TableInfo.Column>(4);
        _columnsSteps.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSteps.put("recipeId", new TableInfo.Column("recipeId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSteps.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSteps.put("position", new TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSteps = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSteps.add(new TableInfo.ForeignKey("recipes", "CASCADE", "NO ACTION", Arrays.asList("recipeId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSteps = new HashSet<TableInfo.Index>(1);
        _indicesSteps.add(new TableInfo.Index("index_steps_recipeId", false, Arrays.asList("recipeId"), Arrays.asList("ASC")));
        final TableInfo _infoSteps = new TableInfo("steps", _columnsSteps, _foreignKeysSteps, _indicesSteps);
        final TableInfo _existingSteps = TableInfo.read(db, "steps");
        if (!_infoSteps.equals(_existingSteps)) {
          return new RoomOpenHelper.ValidationResult(false, "steps(com.sjarry.cabas.data.entities.StepEntity).\n"
                  + " Expected:\n" + _infoSteps + "\n"
                  + " Found:\n" + _existingSteps);
        }
        final HashMap<String, TableInfo.Column> _columnsMenuEntries = new HashMap<String, TableInfo.Column>(4);
        _columnsMenuEntries.put("recipeId", new TableInfo.Column("recipeId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMenuEntries.put("servings", new TableInfo.Column("servings", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMenuEntries.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMenuEntries.put("done", new TableInfo.Column("done", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMenuEntries = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMenuEntries.add(new TableInfo.ForeignKey("recipes", "CASCADE", "NO ACTION", Arrays.asList("recipeId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMenuEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMenuEntries = new TableInfo("menu_entries", _columnsMenuEntries, _foreignKeysMenuEntries, _indicesMenuEntries);
        final TableInfo _existingMenuEntries = TableInfo.read(db, "menu_entries");
        if (!_infoMenuEntries.equals(_existingMenuEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "menu_entries(com.sjarry.cabas.data.entities.MenuEntryEntity).\n"
                  + " Expected:\n" + _infoMenuEntries + "\n"
                  + " Found:\n" + _existingMenuEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsCheckedItems = new HashMap<String, TableInfo.Column>(1);
        _columnsCheckedItems.put("itemKey", new TableInfo.Column("itemKey", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCheckedItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCheckedItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCheckedItems = new TableInfo("checked_items", _columnsCheckedItems, _foreignKeysCheckedItems, _indicesCheckedItems);
        final TableInfo _existingCheckedItems = TableInfo.read(db, "checked_items");
        if (!_infoCheckedItems.equals(_existingCheckedItems)) {
          return new RoomOpenHelper.ValidationResult(false, "checked_items(com.sjarry.cabas.data.entities.CheckedItemEntity).\n"
                  + " Expected:\n" + _infoCheckedItems + "\n"
                  + " Found:\n" + _existingCheckedItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "55cee5517a97dd52e62c2c89eef9e7c9", "7166c85df7dfb3140a4564604a4b86c8");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "recipes","ingredients","steps","menu_entries","checked_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `recipes`");
      _db.execSQL("DELETE FROM `ingredients`");
      _db.execSQL("DELETE FROM `steps`");
      _db.execSQL("DELETE FROM `menu_entries`");
      _db.execSQL("DELETE FROM `checked_items`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RecipeDao.class, RecipeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MenuDao.class, MenuDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CheckedItemDao.class, CheckedItemDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RecipeDao recipeDao() {
    if (_recipeDao != null) {
      return _recipeDao;
    } else {
      synchronized(this) {
        if(_recipeDao == null) {
          _recipeDao = new RecipeDao_Impl(this);
        }
        return _recipeDao;
      }
    }
  }

  @Override
  public MenuDao menuDao() {
    if (_menuDao != null) {
      return _menuDao;
    } else {
      synchronized(this) {
        if(_menuDao == null) {
          _menuDao = new MenuDao_Impl(this);
        }
        return _menuDao;
      }
    }
  }

  @Override
  public CheckedItemDao checkedItemDao() {
    if (_checkedItemDao != null) {
      return _checkedItemDao;
    } else {
      synchronized(this) {
        if(_checkedItemDao == null) {
          _checkedItemDao = new CheckedItemDao_Impl(this);
        }
        return _checkedItemDao;
      }
    }
  }
}
