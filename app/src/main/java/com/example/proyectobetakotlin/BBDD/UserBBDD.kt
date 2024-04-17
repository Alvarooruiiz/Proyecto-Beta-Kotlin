package com.example.proyectobetakotlin.BBDD

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserBBDD(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int ) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){


    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UsersDB"
    }

    private val sqlCreateUsers = "CREATE TABLE UsersTable (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_name TEXT, " +
            "user_pass TEXT, " +
            "user_mail TEXT, " +
            "user_birth TEXT, " +
            "user_acc INTEGER, " +
            "user_image BLOB, " +
            "user_status INTEGER DEFAULT 0)"

    private val sqlCreateImages = "CREATE TABLE ImagesTable (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER, " +
            "image_url BLOB, " +
            "FOREIGN KEY(user_id) REFERENCES UsersTable(_id))"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlCreateUsers)
        db.execSQL(sqlCreateImages)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UsersTable")
        db.execSQL("DROP TABLE IF EXISTS ImagesTable")
        onCreate(db)
    }
}