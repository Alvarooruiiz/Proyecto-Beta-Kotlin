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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.Carousel.CarouselActivity
import com.example.proyectobetakotlin.Login.Login
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.User
import com.example.proyectobetakotlin.ViewModel.UserViewModel
import com.example.proyectobetakotlin.databinding.ListLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
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


    private var listUsersAlta: ArrayList<User> = ArrayList()
    private var listUsersBaja: ArrayList<User> = ArrayList()

    private var userList1: MutableList<User> = mutableListOf()
    private var userList2: MutableList<User> = mutableListOf()

    private lateinit var adapterFrag: FragmentViewPageAdapter



    companion object{
        private const val REQUEST_EDIT_USER = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userLog = intent.getSerializableExtra("userLog") as User


        adapterFrag = FragmentViewPageAdapter(supportFragmentManager,lifecycle,userLog)
        binding.tabTabOptions.addTab(binding.tabTabOptions.newTab().setText("Usuarios de alta"))
        binding.tabTabOptions.addTab(binding.tabTabOptions.newTab().setText("Usuarios de baja"))
        binding.vpViewPager.adapter = adapterFrag


        binding.tabTabOptions.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab!=null){
                    binding.vpViewPager.currentItem = tab.position
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.vpViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabTabOptions.selectTab(binding.tabTabOptions.getTabAt(position))

            }
        })

        if (userLog.userAcc == 0) {
            toolbar = binding.tbToolbar
            setSupportActionBar(toolbar)
        }



        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
                    filterUsers(text)
                }
                return true
            }
        })



    }

    private fun filterUsers(query: String) {
        val filteredUserList1 = userList1.filter { user ->
            user.userName!!.contains(query, ignoreCase = true) ||
                    user.userEmail!!.contains(query, ignoreCase = true) ||
                    user.userBirth!!.contains(query, ignoreCase = true)
        }

        val filteredUserList2 = userList2.filter { user ->
            user.userName!!.contains(query, ignoreCase = true) ||
                    user.userEmail!!.contains(query, ignoreCase = true) ||
                    user.userBirth!!.contains(query, ignoreCase = true)
        }

        // Actualizar los fragmentos con las listas filtradas
        adapterFrag.updateUserList(filteredUserList1, filteredUserList2)
    }



    override fun onUserListReceived(userList: List<User>) {
        // Recibir la lista original de cada fragmento
        if (userList1.isEmpty()) {
            userList1.addAll(userList)
        } else {
            userList2.addAll(userList)
        }
    }

    fun clearSearchView() {
        searchView.setQuery("", false)
        searchView.clearFocus()
    }


    private fun obtenerUsuariosMarcados(): ArrayList<User> {
        val altaFragment = getCurrentFragment(0) as ListFragment
        val bajaFragment = getCurrentFragment(1) as ListFragmentBaja
        val markedUsers = ArrayList<User>()
        markedUsers.addAll(altaFragment.getMarkedUsers())
        markedUsers.addAll(bajaFragment.getMarkedUsers())
        return markedUsers
    }

    private fun getCurrentFragment(position: Int): Fragment {
        return (binding.vpViewPager.adapter as FragmentStateAdapter).createFragment(position)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (userLog.userAcc == 0) {
            this.menuInflater.inflate(R.menu.top_app_bar, menu)
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val markedUsers = obtenerUsuariosMarcados()
            if (markedUsers.isNotEmpty()) {
                mostrarBottomSheet()
                return true
            } else {
                Toast.makeText(this, "No ha seleccionado ningún usuario", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun mostrarBottomSheet() {
        val altaFragment = getCurrentFragment(0) as ListFragment
        val bajaFragment = getCurrentFragment(1) as ListFragmentBaja
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView: View = layoutInflater.inflate(R.layout.bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val deleteButton: Button = bottomSheetView.findViewById(R.id.btnDelete)
        deleteButton.setOnClickListener {
            eliminarUsuariosMarcados(altaFragment, bajaFragment)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun eliminarUsuariosMarcados(altaFragment: ListFragment, bajaFragment: ListFragmentBaja) {
        var salir = false
        val markedUsers = obtenerUsuariosMarcados()
        for (usuario in markedUsers) {
            val selection = UserProvider.Users._ID + "=?"
            val selectionArgs = arrayOf(usuario.id.toString())
            if (userLog.id == usuario.id) {
                salir = true
            }
            val imageSelection = UserProvider.Images.COL_USER_ID + "=?"
            val imageSelectionArgs = arrayOf(usuario.id.toString())
            this.contentResolver.delete(UserProvider.CONTENT_URI_IMAGES, imageSelection, imageSelectionArgs)

            val rowsDeleted = this.contentResolver.delete(UserProvider.CONTENT_URI_USERS, selection, selectionArgs)
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Usuarios eliminados exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
        if (salir) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            this.finish()
        }
        altaFragment.listar()
        bajaFragment.listar()
    }

}

