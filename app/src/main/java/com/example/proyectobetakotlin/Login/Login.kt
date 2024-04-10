package com.example.proyectobetakotlin.Login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.EditRegist.SharedEditRegister
import com.example.proyectobetakotlin.databinding.LoginLayoutBinding

class Login : AppCompatActivity() {
    private lateinit var binding: LoginLayoutBinding

    private val REQUEST_REGISTER = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(R.layout.login_layout)

        binding.btnRegisterLayout.setOnClickListener(View.OnClickListener {
            val intentRegister = Intent(this, SharedEditRegister::class.java)
            intentRegister.putExtra("isRegistering", true)
            cleanFields()
            startActivityForResult(intentRegister, REQUEST_REGISTER)
        })

    }

    private fun cleanFields() {
        binding.txtUserLog.editText?.setText("")
        binding.txtPassLog.editText?.setText("")
    }


}