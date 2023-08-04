package com.sportsintercative.contentapp.constants

import com.sportsintercative.contentapp.R
import com.sportsintercative.contentapp.models.ContentData
import com.sportsintercative.contentapp.models.ImageItem

object AppConstants {

    fun getContext(){

    }
    var b1 = "https://r4.wallpaperflare.com/wallpaper/387/50/394/anime-demon-slayer-kimetsu-no-yaiba-akaza-demon-slayer-kimetsu-no-yaiba-hd-wallpaper-54a218098cdc609320d2415f56dcf5d8.jpg"
    val listOfOnePiece = listOf("")
    val listOfDemonSlayer = listOf("https://r4.wallpaperflare.com/wallpaper/387/50/394/anime-demon-slayer-kimetsu-no-yaiba-akaza-demon-slayer-kimetsu-no-yaiba-hd-wallpaper-54a218098cdc609320d2415f56dcf5d8.jpg",
    )
    private var demonImageList = listOf(
        ImageItem(R.drawable.b1),
        ImageItem(R.drawable.b2),
        ImageItem(R.drawable.b3),
        ImageItem(R.drawable.b4),
        ImageItem(R.drawable.b5)
    )
    private var jujutsuList = listOf(
        ImageItem(R.drawable.b1),
        ImageItem(R.drawable.b2),
        ImageItem(R.drawable.b3),
        ImageItem(R.drawable.b4),
        ImageItem(R.drawable.b5)
    )

    val demonSlayer = ContentData("Demon Slayer","description", demonImageList,"video_id")
    val jujutsuPiece = ContentData("Demon Slayer","", jujutsuList,"video_id")
    val onePiece = ContentData("Demon Slayer","description", jujutsuList,"video_id")
}