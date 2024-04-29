package com.example.animalgallery.model

class AnimalModelAPI(var id: String?,
                     var regular: String?,
                     var imageTitle: String?,
                     var imageDescription: String?,
                     var imageDownloadLink: String,
                     var tag: String){
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "", "", "")
}