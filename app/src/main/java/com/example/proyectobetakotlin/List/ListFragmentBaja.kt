package com.example.proyectobetakotlin.List

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.Carousel.CarouselActivity
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.User

class ListFragmentBaja(private val userLog: User?) : Fragment(), OnAvatarClickListener {

    private lateinit var adapter: RecyclerAdapterList
    private var markedUsers: ArrayList<User> = ArrayList()
    private var listUsers: ArrayList<User> = ArrayList()

    private var originalUserList: MutableList<User> = mutableListOf()
    private lateinit var userListListener: UserListListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Asegúrate de que el contexto que adjunta el fragmento implemente UserListListener
        if (context is UserListListener) {
            userListListener = context
        } else {
            throw RuntimeException("$context must implement UserListListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_baja, container, false)
        markedUsers = ArrayList()
        listUsers = ArrayList()
        val rvList = view.findViewById<RecyclerView>(R.id.rvRecyclerView2)
        rvList.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecyclerAdapterList(requireContext(), ArrayList(), userLog, ArrayList())
        adapter.setOnAvatarClickListener(this)
        rvList.adapter = adapter
        listar()

        originalUserList.addAll(listUsers)
        userListListener.onUserListReceived(originalUserList)
        return view
    }
    fun getAdapter(): RecyclerAdapterList{
        return adapter
    }


    fun setUserListListener(listener: UserListListener) {
        userListListener = listener
    }

    fun updateUserList(userList: List<User>) {
        if (::adapter.isInitialized) { // Verifica si el adaptador está inicializado
            adapter.updateData(userList)
        } else {
            Log.e("ListFragment", "El adaptador no ha sido inicializado")
            // Maneja la situación donde el adaptador no está inicializado
        }
    }


    fun getMarkedUsers(): ArrayList<User> {
        for (user in listUsers) {
            if (user.isChecked) {
                markedUsers.add(user)
            }
        }
        return markedUsers
    }

    fun listar() {
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
        val cr: ContentResolver = requireActivity().contentResolver
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
                    if (objetoDato.userStatus == 1) {
                        listUsers.add(objetoDato)
                    }

                } while (cur.moveToNext())
            }
            cur.close()
        }
        for (user in listUsers) {
            user.isChecked = false
        }

        adapter.updateData(listUsers)

    }

    override fun onAvatarClick(user: User) {
        val imageBitmaps = obtenerListaDeBitmapsDeImagenes(user.id)
        if (imageBitmaps.isEmpty()) {
            Toast.makeText(requireContext(), "No hay imágenes disponibles", Toast.LENGTH_SHORT)
                .show()
        } else {
            val intent = Intent(requireContext(), CarouselActivity::class.java)
            intent.putExtra("userId", user.id)
            startActivity(intent)
        }
    }

    @SuppressLint("Range")
    private fun obtenerListaDeBitmapsDeImagenes(userId: Int): ArrayList<Bitmap> {
        val bitmaps = ArrayList<Bitmap>()
        val cursor: Cursor? = requireActivity().contentResolver.query(
            UserProvider.CONTENT_URI_IMAGES,
            arrayOf(UserProvider.Images.COL_IMAGE_URL),
            UserProvider.Images.COL_USER_ID + " = ?",
            arrayOf(userId.toString()),
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val imageData: ByteArray? =
                    cursor.getBlob(cursor.getColumnIndex(UserProvider.Images.COL_IMAGE_URL))
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData?.size ?: 0)
                bitmaps.add(bitmap)
            } while (cursor.moveToNext())
            cursor.close()
        }
        return bitmaps
    }

    override fun onResume() {
        super.onResume()
        listar()
    }
}