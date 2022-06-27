package com.delafleur.mxt.data

import kotlin.random.Random


data class Players (var playerName: String){
    val testScore =  List(13){ Random.nextInt(0,155)}.map{it.toString()}.toTypedArray()
    val score = Array(13){"0"}
    val record = arrayOf(playerName,score.toList().joinToString())
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

