package com.example.proyectobetakotlin.Login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.EditRegist.SharedEditRegister
import com.example.proyectobetakotlin.List.UserListActivity
import com.example.proyectobetakotlin.User
import com.example.proyectobetakotlin.databinding.LoginLayoutBinding

class Login : AppCompatActivity() {
    private lateinit var binding: LoginLayoutBinding
    private var userLog: User? = null

    companion object{
        private val REQUEST_REGISTER = 1
        private val REQUEST_LOGIN = 0
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnRegisterLayout.setOnClickListener {
            val intentRegister = Intent(this, SharedEditRegister::class.java)
            intentRegister.putExtra("isRegistering", true)
            startActivityForResult(intentRegister, REQUEST_REGISTER)
        }

        binding.btnLogin.setOnClickListener {
            userLog = getAuthenticatedUser()
            if (userLog != null) {
                val intentList = Intent(this@Login, UserListActivity::class.java)
                intentList.putExtra("userLog", userLog)
                cleanFields()
                startActivity(intentList)
            } else {
                Toast.makeText(this@Login,"La cuenta no existe o la contraseña es incorrecta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cleanFields() {
        binding.txtUserLog.editText?.setText("")
        binding.txtPassLog.editText?.setText("")
    }

    private fun getAuthenticatedUser(): User? {
        val userName = binding.txtUserLog.editText?.getText().toString().trim { it <= ' ' }
        val userPass = binding.txtPassLog.editText?.getText().toString().trim { it <= ' ' }
        val projection = arrayOf<String>(
            UserProvider.Users._ID,
            UserProvider.Users.COL_USER,
            UserProvider.Users.COL_PASSWORD,
            UserProvider.Users.COL_ACCTYPE,
            UserProvider.Users.COL_EMAIL,
            UserProvider.Users.COL_DATE,
            UserProvider.Users.COL_ICON
        )
        val selection: String = UserProvider.Users.COL_USER + " = ?"
        val selectionArgs = arrayOf(userName)
        val cursor = contentResolver.query(UserProvider.CONTENT_URI_USERS, projection, selection, selectionArgs,null)
        if (cursor != null && cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(UserProvider.Users._ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(UserProvider.Users.COL_USER))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(UserProvider.Users.COL_PASSWORD))
            val accType = cursor.getInt(cursor.getColumnIndexOrThrow(UserProvider.Users.COL_ACCTYPE))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(UserProvider.Users.COL_EMAIL))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(UserProvider.Users.COL_DATE))
            val icon = cursor.getBlob(cursor.getColumnIndexOrThrow(UserProvider.Users.COL_ICON))
            val user = User (userId, name, email, password, date, accType, icon, 0)
            cursor.close()
            if (userPass == password) {
                return user
            }
        }
        return null
    }
}