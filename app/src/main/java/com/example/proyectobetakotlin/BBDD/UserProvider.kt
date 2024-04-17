package com.example.proyectobetakotlin.BBDD

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.BaseColumns
import java.sql.SQLException

class UserProvider : ContentProvider() {

    var userBBDD: UserBBDD? = null

    override fun onCreate(): Boolean {
        context?.let {
            userBBDD = UserBBDD(context, BD_NAME, null, BD_VERSION)
        }
        return false
    }



    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = userBBDD!!.writableDatabase
        val match = URI_MATCHER!!.match(uri)
        return when (match) {
            USERS -> db.query(USERS_TABLE, projection, selection, selectionArgs, null, null, sortOrder)
            USERS_ID -> {
                val userId = uri.lastPathSegment
                val userSelection = "_id=?"
                val userSelectionArgs = arrayOf(userId)
                db.query(USERS_TABLE, projection, userSelection, userSelectionArgs, null, null, sortOrder)
            }
            IMAGES -> db.query(IMAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder)
            IMAGES_ID -> {
                val imageId = uri.lastPathSegment
                val imageSelection = "_id=?"
                val imageSelectionArgs = arrayOf(imageId)
                db.query(IMAGES_TABLE, projection, imageSelection, imageSelectionArgs, null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("URI desconocido: $uri")
        }
    }

    override fun getType(uri: Uri): String {
        val match = URI_MATCHER?.match(uri)
        return when (match) {
            USERS -> "vnd.android.cursor.dir/vnd.com.example.proyectobetakotlin.user"
            USERS_ID -> "vnd.android.cursor.item/vnd.example.proyectobetakotlin.user"
            IMAGES -> "vnd.android.cursor.dir/vnd.com.example.proyectobetakotlin.image"
            IMAGES_ID -> "vnd.android.cursor.item/vnd.com.example.proyectobetakotlin.image"
            else -> throw IllegalArgumentException("URI desconocido: $uri")
        }

    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val db: SQLiteDatabase = userBBDD!!.getWritableDatabase()
        var regId: Long = -1
        regId =
            when (URI_MATCHER!!.match(uri)) {
                USERS -> db.insert(USERS_TABLE, null, values)
                IMAGES -> db.insert(IMAGES_TABLE, null, values)
                else -> throw IllegalArgumentException("URI desconocido: $uri")
            }
        val contentUri = when (URI_MATCHER!!.match(uri)) {
            USERS -> CONTENT_URI_USERS
            IMAGES -> CONTENT_URI_IMAGES
            else -> throw IllegalArgumentException("URI desconocido: $uri")
        }
        return if (regId != -1L) {
            ContentUris.withAppendedId(contentUri, regId)
        } else {
            throw android.database.SQLException("Falla al insertar fila en: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db: SQLiteDatabase = userBBDD!!.getWritableDatabase()
        val cont: Int
        cont =
            when (URI_MATCHER!!.match(uri)) {
                USERS -> db.delete(USERS_TABLE, selection, selectionArgs)
                USERS_ID -> {
                    val userId = uri.lastPathSegment
                    val whereImages = "user_id=?"
                    val whereImagesArgs = arrayOf(userId)
                    db.delete(IMAGES_TABLE, whereImages, whereImagesArgs)
                    val whereUser = "_id=?"
                    val whereUserArgs = arrayOf(userId)
                    db.delete(USERS_TABLE, whereUser, whereUserArgs)
                }

                IMAGES -> db.delete(IMAGES_TABLE, selection, selectionArgs)
                IMAGES_ID -> {
                    val imageId = uri.lastPathSegment
                    val whereImagenes = BaseColumns._ID + "=?"
                    val whereImagenesArgs = arrayOf(imageId)
                    db.delete(IMAGES_TABLE, whereImagenes, whereImagenesArgs)
                }

                else -> throw IllegalArgumentException("URI desconocido delete: $uri")
            }
        return cont
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db: SQLiteDatabase = userBBDD!!.getWritableDatabase()
        val cont: Int
        cont = when (URI_MATCHER!!.match(uri)) {
            USERS_ID -> {
                val userId = uri.lastPathSegment
                val whereUser = BaseColumns._ID + "=?"
                val whereUserArgs = arrayOf(userId)
                db.update(USERS_TABLE, values, whereUser, whereUserArgs)
            }

            IMAGES_ID -> {
                val imageId = uri.lastPathSegment
                val whereImagenes = BaseColumns._ID + "=?"
                val whereImagenesArgs = arrayOf(imageId)
                db.update(IMAGES_TABLE, values, whereImagenes, whereImagenesArgs)
            }

            else -> throw IllegalArgumentException("URI desconocido en el update: $uri")
        }
        return cont
    }


    object Users : BaseColumns {
        const val _ID = "_id"
        const val COL_USER = "user_name"
        const val COL_PASSWORD = "user_pass"
        const val COL_EMAIL = "user_mail"
        const val COL_DATE = "user_birth"
        const val COL_ACCTYPE = "user_acc"
        const val COL_ICON = "user_image"
        const val COL_STATUS = "user_status"
    }

    object Images : BaseColumns {
        const val _ID = "_id"
        const val COL_USER_ID = "user_id"
        const val COL_IMAGE_URL = "image_url"
    }

    companion object {
        private const val AUTHORITY = "com.example.proyectobetakotlin.bbdd"
        private const val USERS_PATH = "users"
        private const val IMAGES_PATH = "images"
        private const val URI_USERS = "content://" + AUTHORITY + "/" + USERS_PATH
        private const val URI_IMAGES = "content://" + AUTHORITY + "/" + IMAGES_PATH
        val CONTENT_URI_USERS = Uri.parse(URI_USERS)
        val CONTENT_URI_IMAGES = Uri.parse(URI_IMAGES)

        // Definimos el objeto UriMatcher
        private const val USERS = 1
        private const val USERS_ID = 2
        var URI_MATCHER: UriMatcher? = null
        const val BD_NAME = "DBUSERS_V1"
        const val BD_VERSION = 1
        const val USERS_TABLE = "UsersTable"
        const val IMAGES_TABLE = "ImagesTable"
        private const val IMAGES = 3
        private const val IMAGES_ID = 4

        init {
            URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)
            URI_MATCHER!!.addURI(AUTHORITY, "users", USERS)
            URI_MATCHER!!.addURI(AUTHORITY, "users/#", USERS_ID)
            URI_MATCHER!!.addURI(AUTHORITY, "images", IMAGES)
            URI_MATCHER!!.addURI(AUTHORITY, "images/#", IMAGES_ID)
        }
    }
}