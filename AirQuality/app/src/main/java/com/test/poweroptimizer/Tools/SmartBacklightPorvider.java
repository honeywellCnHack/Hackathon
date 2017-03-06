/**
 * @file Copyright (C) 2015 Honeywell Inc. All rights reserved.
 */

package com.test.poweroptimizer.Tools;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class SmartBacklightPorvider extends ContentProvider {

    private static final String DATABASE_NAME = "SmartBacklight.db";

    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "SmartBacklight";

    private DatabaseHelper mOpenHelper;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_NAME, where, whereArgs);

        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        String field_0 = initialValues.get(BacklightConst.STRING_PROFILE_NAME).toString();

        String field_1 = null;
        if(initialValues.get(BacklightConst.STRING_PN) != null)
            field_1 = initialValues.get(BacklightConst.STRING_PN).toString();

        String field_2 = null;
        if(initialValues.get(BacklightConst.STRING_STATUS) != null)
            field_2 = initialValues.get(BacklightConst.STRING_STATUS).toString();

        String field_3 = initialValues.get(BacklightConst.STRING_BACKLIGHT_LEVEL).toString();
        String field_4 = initialValues.get(BacklightConst.STRING_BACKLIGHT_TIME).toString();

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String sql_1 = "insert into " + TABLE_NAME + " (" + BacklightConst.STRING_PROFILE_NAME + ", " + BacklightConst.STRING_PN + ", "
                + BacklightConst.STRING_STATUS + ", " + BacklightConst.STRING_BACKLIGHT_LEVEL + ", " + BacklightConst.STRING_BACKLIGHT_TIME
                + ") values('" + field_0 + "', '" + field_1 + "', '" + field_2 + "', '" + field_3 + "', '" + field_4
                + "');";

        try {
            db.execSQL(sql_1);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("ERROR", e.toString());
        }

        return uri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null,
                null);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.update(TABLE_NAME, values, where, whereArgs);

        return 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + BacklightConst.STRING_PROFILE_NAME + " TEXT," + BacklightConst.STRING_PN + " TEXT,"
                    + BacklightConst.STRING_STATUS + " TEXT," + BacklightConst.STRING_BACKLIGHT_LEVEL
                    + " TEXT," + BacklightConst.STRING_BACKLIGHT_TIME + " TEXT" + ");");

            return;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

            return;
        }
    }
}
