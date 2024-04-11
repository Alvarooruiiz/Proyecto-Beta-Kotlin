package com.example.proyectobetakotlin

import java.io.Serializable

class User : Serializable {
    var id = 0
    var userName: String? = null
    var userPass: String? = null
    var userEmail: String? = null
    var userBirth: String? = null
    var userAcc = 0
    var userImage: ByteArray? = null
    var userStatus = 0
    var isChecked = false

    constructor()
    constructor(id: Int, userName: String?, userMail: String?, userPass: String?, userBirth: String?, userAcc: Int, userImage: ByteArray, userStatus: Int) {
        this.id = id
        this.userName = userName
        this.userEmail = userMail
        this.userPass = userPass
        this.userBirth = userBirth
        this.userAcc = userAcc
        this.userImage = userImage
        this.userStatus = userStatus
    }

}
