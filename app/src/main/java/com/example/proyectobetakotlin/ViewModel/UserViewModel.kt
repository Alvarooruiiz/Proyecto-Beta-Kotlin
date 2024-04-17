package com.example.proyectobetakotlin.ViewModel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectobetakotlin.User

class UserViewModel : ViewModel() {

    private val userListLiveData: MutableLiveData<List<User>> = MutableLiveData()
    private var userList: List<User> = ArrayList()
    private var filteredList: List<User> = ArrayList()

    fun setUserList(users: List<User>) {
        userList = users
        filteredList = userList
        userListLiveData.postValue(userList)
        filterUsers("")
    }

    fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            userList
        } else {
            userList.filter { user ->
                user.userName?.contains(query, ignoreCase = true) ?: false ||
                        user.userEmail?.contains(query, ignoreCase = true) ?: false ||
                        user.userBirth?.contains(query, ignoreCase = true) ?: false
            }
        }
        userListLiveData.postValue(filteredList)
    }

    fun getUserListLiveData(): LiveData<List<User>> {
        return userListLiveData
    }
}