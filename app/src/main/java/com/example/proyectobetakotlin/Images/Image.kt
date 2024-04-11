package com.example.proyectobetakotlin.Images

class Image {
    var id = 0
    var userId = 0
    var imageUrl: String? = null


    constructor()

    constructor(userId: Int, imageUrl: String) {
        this.userId = userId
        this.imageUrl = imageUrl
    }
}