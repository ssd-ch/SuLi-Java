package com.example.ssd.shimaneuniversitybrowser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Databaseに関連するクラス
 * DBAdapter
 */
public class DBAdapter {

    private final static String DB_NAME = "shimane_university.db";      // DBファイル名
    private final static int DB_VERSION = 1;                // DBのバージョン

    private SQLiteDatabase db = null;           // SQLiteDatabase
    private DBHelper dbHelper = null;           // DBHelper
    protected Context context;                  // Context

    // コンストラクタ
    public DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DBHelper(this.context);
    }

    /**
     * DBの読み書き
     * openDB()
     *
     * @return this 自身のオブジェクト
     */
    public DBAdapter openDB() {
        db = dbHelper.getWritableDatabase();        // DBの読み書き
        return this;
    }

    /**
     * DBの読み込み 今回は未使用
     * readDB()
     *
     * @return this 自身のオブジェクト
     */
    public DBAdapter readDB() {
        db = dbHelper.getReadableDatabase();        // DBの読み込み
        return this;
    }

    /**
     * DBを閉じる
     * closeDB()
     */
    public void closeDB() {
        db.close();     // DBを閉じる
        db = null;
    }

    /**
     * DBのレコードへ登録
     * saveDB()
     *
     * @param table テーブル名
     * @param column 列名
     * @param value 値
     */
    public void saveDB( String table, String[] column, String[][] value) {

        db.beginTransaction();          // トランザクション開始
        try {
            for (int i = 0; i < value.length; i++) {
                ContentValues values = new ContentValues();     // ContentValuesでデータを設定していく
                for (int j = 0; j < column.length; j++) {
                    values.put(column[j], value[i][j]);
                }
                // insertメソッド データ登録
                // 第1引数：DBのテーブル名
                // 第2引数：更新する条件式
                // 第3引数：ContentValues
                db.insert( table, null, values);      // レコードへ登録
            }
            db.setTransactionSuccessful();      // トランザクションへコミット
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();                // トランザクションの終了
        }
    }

    /**
     * DBのデータを取得
     * getDB()
     * _idの昇順にソートする
     *
     * @param table String テーブル名
     * @param columns String[] 取得するカラム名 nullの場合は全カラムを取得
     * @return DBのデータ
     */
    public Cursor getDB(String table, String[] columns) {

        // queryメソッド DBのデータを取得
        // 第1引数：DBのテーブル名
        // 第2引数：取得するカラム名
        // 第3引数：選択条件(WHERE句)
        // 第4引数：第3引数のWHERE句において?を使用した場合に使用
        // 第5引数：集計条件(GROUP BY句)
        // 第6引数：選択条件(HAVING句)
        // 第7引数：ソート条件(ORDER BY句)
        return db.query( table, columns, null, null, null, null, "_id ASC");
    }

    /**
     * DBの検索したデータを取得
     * searchDB()
     * _idの昇順にソートする
     *
     * @param table String 取得するテーブル名
     * @param columns String[] 取得するカラム名 nullの場合は全カラムを取得
     * @param column  String 選択条件に使うカラム名
     * @param name    String[]
     * @return DBの検索したデータ
     */
    public Cursor searchDB(String table, String[] columns, String column, String[] name) {
        return db.query( table, columns, column, name, null, null, "_id ASC");
    }

    /**
     * DBのレコードを全削除
     * allDelete()
     *
     * @param table 削除するテーブル名
     */
    public void allDelete(String table) {

        db.beginTransaction();                      // トランザクション開始
        try {
            // deleteメソッド DBのレコードを削除
            // 第1引数：テーブル名
            // 第2引数：削除する条件式 nullの場合は全レコードを削除
            // 第3引数：第2引数で?を使用した場合に使用
            db.delete( table, null, null);        // DBのレコードを全削除
            db.setTransactionSuccessful();          // トランザクションへコミット
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();                    // トランザクションの終了
        }
    }

    /**
     * DBのレコードの単一削除
     * selectDelete()
     *
     * @param table String 削除するテーブル名
     * @param position String
     */
    public void selectDelete( String table, String position) {

        db.beginTransaction();                      // トランザクション開始
        try {
            db.delete( table, "_id=?", new String[]{position});
            db.setTransactionSuccessful();          // トランザクションへコミット
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();                    // トランザクションの終了
        }
    }

    /**
     * データベースの生成やアップグレードを管理するSQLiteOpenHelperを継承したクラス
     * DBHelper
     */
    private static class DBHelper extends SQLiteOpenHelper {

        // コンストラクタ
        public DBHelper(Context context) {
            //第1引数：コンテキスト
            //第2引数：DBファイル名
            //第3引数：factory nullでよい
            //第4引数：DBのバージョン
            super( context, DB_NAME, null, DB_VERSION);
        }

        /**
         * DB生成時に呼ばれる
         * onCreate()
         *
         * @param db SQLiteDatabase
         */
        @Override
        public void onCreate(SQLiteDatabase db) {

            //テーブルを作成する ※スペースに気を付ける
            db.execSQL(
                    "CREATE TABLE ClassroomDivide ("
                            + "_id INTEGER PRIMARY KEY NOT NULL, "
                            + "building_id INTEGER NOT NULL, "
                            + "place TEXT NOT NULL, "
                            + "weekday INTEGER NOT NULL, "
                            + "time INTEGER NOT NULL, "
                            + "cell_text TEXT, "
                            + "cell_color TEXT, "
                            + "classname TEXT, "
                            + "person TEXT, "
                            + "department TEXT, "
                            + "class_code TEXT);" );
            db.execSQL(
                    "CREATE TABLE Building ("
                            + "_id INTEGER PRIMARY KEY NOT NULL, "
                            + "building_name TEXT);" );
        }

        /**
         * DBアップグレード(バージョンアップ)時に呼ばれる
         *
         * @param db         SQLiteDatabase
         * @param oldVersion int 古いバージョン
         * @param newVersion int 新しいバージョン
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // DBからテーブル削除
            db.execSQL("DROP TABLE IF EXISTS ClassroomDivide");
            db.execSQL("DROP TABLE IF EXISTS Building");
            // テーブル生成
            onCreate(db);
        }
    }
}