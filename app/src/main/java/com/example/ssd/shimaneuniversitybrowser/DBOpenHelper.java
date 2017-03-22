package com.example.ssd.shimaneuniversitybrowser;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "shimane_university";
    public static final int DB_VERSION = 1;

    // コンストラクタ
    public DBOpenHelper( Context context ){
        // 任意のデータベースファイル名と、バージョンを指定する
        super( context, DB_NAME, null, DB_VERSION );
    }

    /**
     * このデータベースを初めて使用する時に実行される処理
     * テーブルの作成や初期データの投入を行う
     */
    @Override
    public void onCreate( SQLiteDatabase db ) {
        // テーブルを作成。SQLの文法は通常のSQLiteと同様
        db.execSQL(
                "create table PlacesList ("
                        + "id integer primary key autoincrement not null, "
                        + "name text not null, "
                        + "age integer )" );
        // 必要なら、ここで他のテーブルを作成したり、初期データを挿入したりする
    }

    /**
     * アプリケーションの更新などによって、データベースのバージョンが上がった場合に実行される処理
     * 今回は割愛
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 取りあえず、空実装でよい
    }

}
