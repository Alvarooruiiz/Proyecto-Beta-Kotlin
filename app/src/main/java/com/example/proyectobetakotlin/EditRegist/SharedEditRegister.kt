package com.example.proyectobetakotlin.EditRegist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.databinding.EditRegisLayoutBinding


class SharedEditRegister : AppCompatActivity() {

    private lateinit var binding: EditRegisLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditRegisLayoutBinding.inflate(layoutInflater)
        setContentView(R.layout.edit_regis_layout)


    }
}