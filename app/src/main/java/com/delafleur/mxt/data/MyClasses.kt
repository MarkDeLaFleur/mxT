package com.delafleur.mxt.data

import org.opencv.core.Mat
import org.opencv.core.Point
import kotlin.random.Random

data class Players (var playerName: String){
    val testScore =  List(13){ Random.nextInt(0,155)}.map{it.toString()}.toTypedArray()
    var score = Array(13){"0"}
    fun csvRecord():Array<String>{ return (mutableListOf(playerName)+
                                score.toMutableList()).toTypedArray()}
    private var str = ""

    fun scoreTotalStr(): String {
        str = ""
        score.forEach{ str += if(it == "0") "ns_" else it.padStart(2,'0')+"_"
        }
        return str
    }
    fun scoreTotalStrTest(): String {
        str = ""
        testScore.forEach{ str += if(it == "0") "ns_" else it.padStart(2,'0')+"_"
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
data class MySubmatDomino (var pts: List<Point>, var cropImg :List<Mat>)


