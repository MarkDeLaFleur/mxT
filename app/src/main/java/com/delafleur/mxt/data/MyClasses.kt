package com.delafleur.mxt.data

import kotlin.random.Random


data class Players (var playerName: String){
    val testScore =  List(13){ Random.nextInt(0,155)}.map{it.toString()}.toTypedArray()
    var score = Array(13){"0"}
    val record = arrayOf(playerName,score.toList().joinToString())
    fun totalString(inArr: Array<String>) :String{
        var str = ""
        inArr.forEach{ str += it.padStart(2,'0')+"_"}
        return str
    }
    fun totalScore(inArr: Array<String>) :Int{
        return inArr.map{it.toInt()}.toTypedArray().sum()
    }
}

