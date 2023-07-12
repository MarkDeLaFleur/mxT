package com.delafleur.mxt.data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import org.opencv.core.KeyPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import kotlin.random.Random


class MxT : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        var context: Context? = null
        val appContext: Context?
            get() = context
    }
}
data class Players (var playerName: String){
    val testScore =  List(13){ Random.nextInt(0,155)}.map{it.toString()}.toTypedArray()
    var score = Array(13){"0"}
    fun csvRecord():Array<String>{ return (mutableListOf(playerName)+
                                score.toMutableList()).toTypedArray()}
    private var str = ""

    fun scoreTotalStr(): String {
        str = ""
        score.forEach{ str += if(it == "0") "~_" else it.padStart(2,'0')+"_"
        }
        return str
    }
    fun scoreTotalStrTest(): String {
        str = ""
        testScore.forEach{ str += if(it == "0") "~_" else it.padStart(2,'0')+"_"
        }
        return str
    }
    fun scoreTotInt(): Int {
        return score.map{it.toInt()}.toTypedArray().sum()
    }
    fun scoreTotIntTest(): Int {
        return testScore.map{it.toInt()}.toTypedArray().sum()
    }

}
data class MySubmatDomino(
    var pts: List<Point>,
    val bitmapImgs: ArrayList<Bitmap>
)
data class RectswithPoints(
    var dominoRect: Rect,
    var dominoPoint: List<KeyPoint>
)

