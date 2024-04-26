package com.example.animalgallery.model

class ReadWriteUserDetail (var dob: String = "", var gender: String = "") {
    // No-argument constructor required by Firebase
    constructor() : this("", "")
}