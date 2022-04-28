package com.delafleur.mxt.util

import android.os.Environment
import android.util.Log
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
val fileName = "dominoScore.csv"
val fileI = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
    fileName).absolutePath

fun writeCSV(scoreRecords: MutableList<Array<String>>){
    /*
    * this will write the updated players to the dominoscoresfile
    * using xx.add("player","0","1","2").. etc.
    * */
    Log.i("File", fileI)
    val fileX = File(fileI)
    try {
        if ( File(fileI).createNewFile() ){
            Log.i("File", "File Created")
        }
        else {

            Log.i("File", "$fileI already exists" )
        }
    }catch (e: IOException){
        Log.i("File","beats me!!")

    }
    val fileWriter = FileWriter(fileX)
    val csvWriter = CSVWriter(fileWriter)
    csvWriter.writeAll(scoreRecords)
    csvWriter.close()
    Log.i("myIO", "Wrote to file via csvWriter" + fileX.absolutePath)
}


fun readCSV() : MutableList<Array<String>> {

        val filex = File(fileI)
        val csvReaderData = CSVReader(FileReader(filex)).readAll()
        Log.i("myIO", "Read csv file size is " + csvReaderData.size)
        return csvReaderData
}

class Playerclass(xname: String) {
        val name = xname
        val scores: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        fun updateScores(inD: Int, pTs: Int) {
             scores[inD] = pTs
        }

        fun csvRecord(): Array<String> {
            var outPutRecord: Array<String> = arrayOf(name)
            scores.forEach { item ->
                outPutRecord += item.toString()
            }
            return outPutRecord
        }
}
