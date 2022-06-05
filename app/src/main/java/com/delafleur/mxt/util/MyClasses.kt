package com.delafleur.mxt.util

import android.graphics.Rect
import android.os.Environment
import android.util.Log
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val fileName = "dominoS.csv"
val fileI = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
    fileName).absolutePath  // note this is actually returning a full pathname as a string
val fileX = File(fileI)
val inFile: MutableList<Array<String>> = mutableListOf()



fun backupCSV(){
    val fileNameOut = "dominoS" + DateTimeFormatter.ofPattern("YYYYLLLLddhhmmss").format(LocalDateTime.now()) + ".csv"
    val fileO = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileNameOut).absolutePath
    try{
        File(fileI).copyTo(File(fileO),overwrite = true)
        Log.i("backup","file backed up to ${fileO}")
        File(fileI).delete()

    }catch (e: IOException){
        if (checkFile() == "new"){Log.i("backup","no file to backup, created new one")}
    }


}
fun writeCSV(scoreRecords: MutableList<Array<String>>){
    /*
    * this will write the updated players to the dominoscoresfile
    * using xx.add("player","0","1","2").. etc.
    * */
    // create it if it doesn't exist
    Log.i("File", fileI)
    val fileWriter = FileWriter(fileX)
    val csvWriter = CSVWriter(fileWriter)
    csvWriter.writeAll(scoreRecords)
    csvWriter.close()
    Log.i("myIO", "Wrote to file via csvWriter" + fileX.absolutePath)
}
fun checkFile() :String {
    var fileck = ""
    try {
        if (File(fileI).createNewFile()) {
            Log.i("File", "File Created")
            fileck = "new"
            inFile.add(
                arrayOf(
                    "Player", "R00", "R01", "R02", "R03", "R04",
                    "R05", "R06", "R07", "R08", "R09", "R10", "R11", "R12"
                )
            )
            writeCSV(inFile)

        } else {
            Log.i("File", "$fileI already exists")
            fileck = "old"
        }
    } catch (e: IOException) {
        Log.i("File", "beats me!!")

    }
    return fileck
}

fun readCSV() : MutableList<Array<String>> {
        var csvReaderData = mutableListOf<Array<String>>()
        try{
            csvReaderData = CSVReader(FileReader(fileX)).readAll()
        } catch (e: IOException){
            if (checkFile() == "new" ){
                csvReaderData = inFile
                Log.i("myIO", "Read csv file size is " + csvReaderData.size)
            }
            return csvReaderData

        }
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

