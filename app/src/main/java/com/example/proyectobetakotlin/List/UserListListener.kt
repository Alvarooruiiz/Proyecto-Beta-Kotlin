package com.example.proyectobetakotlin.List

import com.example.proyectobetakotlin.User

interface UserListListener {
    fun onUserListReceived(userList: List<User>)

}