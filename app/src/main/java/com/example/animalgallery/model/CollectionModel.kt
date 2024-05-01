package com.example.animalgallery.model

class CollectionModel(var key: String?, var collectionName: String?) {
    // No-argument constructor required by Firebase
    constructor() : this("", "")
}