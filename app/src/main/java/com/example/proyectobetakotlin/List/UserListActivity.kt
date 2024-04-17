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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.Carousel.CarouselActivity
import com.example.proyectobetakotlin.Login.Login
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.User
import com.example.proyectobetakotlin.ViewModel.UserViewModel
import com.example.proyectobetakotlin.databinding.ListLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class UserListActivity : AppCompatActivity(), UserListListener {

    private lateinit var binding: ListLayoutBinding

    private lateinit var userLog: User
    private lateinit var rvList: RecyclerView
    private lateinit var markedUsers: ArrayList<User>
    private lateinit var listUsers: ArrayList<User>
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var searchView: SearchView
    private lateinit var userListFragment: ListFragment
//    lateinit var userViewModel: UserViewModel



    companion object{
        private const val REQUEST_EDIT_USER = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userLog = intent.getSerializableExtra("userLog") as User

//        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        userListFragment = ListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, userListFragment)
            .commit()

        userListFragment.setUser(userLog)

        if (userLog.userAcc == 0) {
            toolbar = binding.tbToolbar
            setSupportActionBar(toolbar)
        }

        userListFragment.setUserListListener(this)


        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
//                    userViewModel.filterUsers(text)
//                    userListFragment.filterUsers(text)
                    filterUsers(text)


                }
                return true
            }
        })


//        userViewModel.getUserListLiveData().observe(this, Observer { userList ->
//            userListFragment.updateUserList(userList)
//        })
    }



    // Método para recibir la lista original del fragmento mediante la interfaz
    override fun onUserListReceived(userList: List<User>) {
        listUsers = userList as ArrayList<User>
    }

    private fun filterUsers(query: String) {
        val filteredList = listUsers.filter { user ->
            user.userName!!.contains(query, ignoreCase = true) ||
                    user.userEmail!!.contains(query, ignoreCase = true) ||
                    user.userBirth!!.contains(query, ignoreCase = true)
        }
        userListFragment.updateUserList(filteredList)
    }

    private fun obtenerUsuariosMarcados(): ArrayList<User> {
        return userListFragment.getMarkedUsers()
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
            markedUsers= obtenerUsuariosMarcados()
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
            val imageSelection = UserProvider.Images.COL_USER_ID + "=?"
            val imageSelectionArgs = arrayOf(usuario.id.toString())
            contentResolver.delete(UserProvider.CONTENT_URI_IMAGES, imageSelection, imageSelectionArgs)

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
        userListFragment.listar()
    }

}

