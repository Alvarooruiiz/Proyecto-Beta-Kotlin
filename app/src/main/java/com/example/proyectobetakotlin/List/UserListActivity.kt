package com.example.proyectobetakotlin.List

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.Carousel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.Carousel.CarouselActivity
import com.example.proyectobetakotlin.Login.Login
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.User
import com.example.proyectobetakotlin.databinding.ListLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class UserListActivity : AppCompatActivity(), OnAvatarClickListener {

    private lateinit var binding: ListLayoutBinding

    private lateinit var userLog: User
    private lateinit var rvList: RecyclerView
    private lateinit var adapter: RecyclerAdapterList
    private lateinit var markedUsers: ArrayList<User>
    private lateinit var listUsers: ArrayList<User>
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var searchView: SearchView

    companion object{
        private const val REQUEST_EDIT_USER = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userLog = intent.getSerializableExtra("userLog") as User

        markedUsers = ArrayList()

        adapter = RecyclerAdapterList(this, ArrayList(),userLog,markedUsers)
        binding.rvList.layoutManager = LinearLayoutManager(this)

        binding.rvList.adapter = adapter

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filtrar(newText)
                return true
            }
        })
        rvList = binding.rvList

        if(userLog.userAcc==0){
            toolbar = binding.tbToolbar
            setSupportActionBar(toolbar)
        }

    }

    override fun onResume() {
        super.onResume()
        listar()
    }


    private fun listar() {
        val columnas = arrayOf(
            UserProvider.Users._ID,
            UserProvider.Users.COL_USER,
            UserProvider.Users.COL_EMAIL,
            UserProvider.Users.COL_PASSWORD,
            UserProvider.Users.COL_DATE,
            UserProvider.Users.COL_ACCTYPE,
            UserProvider.Users.COL_ICON,
            UserProvider.Users.COL_STATUS
        )
        val versionesUri: Uri = UserProvider.CONTENT_URI_USERS
        val cr: ContentResolver = contentResolver
        val cur: Cursor? = cr.query(versionesUri, columnas, null, null, null)
        var objetoDato: User
        listUsers = ArrayList()
        if (cur != null) {
            if (cur.moveToFirst()) {
                val colId: Int = cur.getColumnIndex(UserProvider.Users._ID)
                val colUse: Int = cur.getColumnIndex(UserProvider.Users.COL_USER)
                val colEma: Int = cur.getColumnIndex(UserProvider.Users.COL_EMAIL)
                val colPas: Int = cur.getColumnIndex(UserProvider.Users.COL_PASSWORD)
                val colDat: Int = cur.getColumnIndex(UserProvider.Users.COL_DATE)
                val colAcc: Int = cur.getColumnIndex(UserProvider.Users.COL_ACCTYPE)
                val colIcon: Int = cur.getColumnIndex(UserProvider.Users.COL_ICON)
                val colStatus: Int = cur.getColumnIndex(UserProvider.Users.COL_STATUS)
                do {
                    val id: Int = cur.getInt(colId)
                    val user: String = cur.getString(colUse)
                    val mail: String = cur.getString(colEma)
                    val pass: String = cur.getString(colPas)
                    val date: String = cur.getString(colDat)
                    val accType: Int = cur.getInt(colAcc)
                    val icon: ByteArray = cur.getBlob(colIcon)
                    val status: Int = cur.getInt(colStatus)
                    Log.d("Estado Usuario", "Estado: $status")
                    objetoDato = User(id, user, mail, pass, date, accType, icon, status)
                    listUsers.add(objetoDato)
                } while (cur.moveToNext())
            }
            cur.close()
        }
        adapter = RecyclerAdapterList(this, listUsers, userLog, markedUsers)
        adapter.setOnAvatarClickListener(this)
        rvList.adapter = adapter
        rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }



    private fun filtrar(texto: String) {
        val usuariosFiltrados = ArrayList<User>()
        if (texto.isEmpty()) {
            usuariosFiltrados.addAll(listUsers)
        } else {
            val text = texto.lowercase(Locale.getDefault())
            for (usuario in listUsers) {
                if (usuario.userName!!.lowercase(Locale.getDefault()).contains(text) ||
                    usuario.userEmail!!.lowercase(Locale.getDefault()).contains(text) ||
                    usuario.userBirth!!.lowercase(Locale.getDefault()).contains(text)
                ) {
                    usuariosFiltrados.add(usuario)
                }
            }
        }
        adapter = RecyclerAdapterList(this, usuariosFiltrados, userLog, markedUsers)
        rvList.adapter = adapter
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val userLogUpdated = data?.getSerializableExtra("userLogUpdated") as? User
            if (userLogUpdated != null) {
                userLog.userAcc = userLogUpdated.userAcc
                listar()
            } else {
                Toast.makeText(this, "es nulo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAvatarClick(user: User) {
        val imageBitmaps = obtenerListaDeBitmapsDeImagenes(user.id)
        if (imageBitmaps.isEmpty()) {
            Toast.makeText(this, "No hay imágenes disponibles", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, CarouselActivity::class.java)
            intent.putExtra("userId", user.id)
            startActivity(intent)
        }
    }

    @SuppressLint("Range")
    private fun obtenerListaDeBitmapsDeImagenes(userId: Int): ArrayList<Bitmap> {
        val bitmaps = ArrayList<Bitmap>()
        val cursor: Cursor? = contentResolver.query(
            UserProvider.CONTENT_URI_IMAGES,
            arrayOf(UserProvider.Images.COL_IMAGE_URL),
            UserProvider.Images.COL_USER_ID + " = ?",
            arrayOf(userId.toString()),
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val imageData: ByteArray? = cursor.getBlob(cursor.getColumnIndex(UserProvider.Images.COL_IMAGE_URL))
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData?.size ?: 0)
                bitmaps.add(bitmap)
            } while (cursor.moveToNext())
            cursor.close()
        }
        return bitmaps
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (userLog.userAcc == 0) {
            menuInflater.inflate(R.menu.top_app_bar, menu)

            return super.onCreateOptionsMenu(menu)
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            if (markedUsers.isNotEmpty()) {
                mostrarBottomSheet()
                return true
            } else {
                Toast.makeText(this, "No ha seleccionado ningun usuario", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun mostrarBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView: View = layoutInflater.inflate(R.layout.bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val deleteButton: Button = bottomSheetView.findViewById(R.id.btnDelete)
        deleteButton.setOnClickListener {
            eliminarUsuariosMarcados()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun eliminarUsuariosMarcados() {
        var salir = false
        for (usuario in markedUsers) {
            val selection = UserProvider.Users._ID + "=?"
            val selectionArgs = arrayOf(usuario.id.toString())
            if (userLog.id == usuario.id) {
                salir = true
            }
            // Eliminar imágenes relacionadas con el usuario
            val imageSelection = UserProvider.Images.COL_USER_ID + "=?"
            val imageSelectionArgs = arrayOf(usuario.id.toString())
            contentResolver.delete(UserProvider.CONTENT_URI_IMAGES, imageSelection, imageSelectionArgs)

            // Eliminar el usuario
            val rowsDeleted = contentResolver.delete(UserProvider.CONTENT_URI_USERS, selection, selectionArgs)
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Usuarios eliminados exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
        if (salir) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
        listUsers.removeAll(markedUsers)
        adapter.notifyDataSetChanged()
        adapter.clearSelection()
    }

}

