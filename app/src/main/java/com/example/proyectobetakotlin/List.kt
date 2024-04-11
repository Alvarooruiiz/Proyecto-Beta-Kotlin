package com.example.proyectobetakotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectobetakotlin.databinding.EditRegisLayoutBinding
import com.example.proyectobetakotlin.databinding.ListLayoutBinding

class List : AppCompatActivity() {

    private lateinit var binding: ListLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}