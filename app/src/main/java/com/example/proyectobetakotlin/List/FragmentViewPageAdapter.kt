package com.example.proyectobetakotlin.List

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyectobetakotlin.User

class FragmentViewPageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val userLog: User?): FragmentStateAdapter(fragmentManager,lifecycle) {

    private lateinit var listFragment1: ListFragment
    private lateinit var listFragment2: ListFragmentBaja

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            listFragment1 = ListFragment(userLog)
            listFragment1
        } else {
            listFragment2 = ListFragmentBaja(userLog)
            listFragment2
        }
    }

    fun updateUserList(userList1: List<User>, userList2: List<User>) {
        listFragment1.updateUserList(userList1)
        if (::listFragment2.isInitialized) {
            listFragment2.updateUserList(userList2)
        } else {
            // Manejar el caso en el que listFragment2 no ha sido inicializado
        }
    }
}