package com.example.patrolisatpam.`object`

class markerz {
    var lang: Double? = null
    var long: Double? = null
    var title: String? = null
    var snipset: String? = null
    var icon: Float? = null

    constructor(
        lang: Double,
        long: Double,
        title: String,
        snipset: String,
        icon: Float?
    ) {
        this.lang = lang
        this.long = long
        this.title = title
        this.snipset = snipset
        this.icon = icon
    }
}