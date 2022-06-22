package com.delafleur.mxt.util


import android.os.Environment
import android.util.Log
import com.delafleur.mxt.data.Players
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val fileName = "mXtrain.csv"
val fileI = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
    fileName).absolutePath  // note this is actually returning a full pathname as a string
val fileX = File(fileI)

fun backupCSV(){
    val fileNameOut = "dominoS" + DateTimeFormatter.ofPattern("yyyyLLLLddhhmmss").format(LocalDateTime.now()) + ".csv"
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

    Log.i("File", fileI)
    val fileWriter = FileWriter(fileX)
    val csvWriter = CSVWriter(fileWriter)
    try {
        csvWriter.writeAll(scoreRecords)
        csvWriter.close()
        Log.i("csvWriter","scorerecords size ${scoreRecords.size}")
        scoreRecords.forEach{
            Log.i("csvWriter","records written ${it.toList().toString()}")
        }
    }catch (e: IOException){
        Log.e("IOException","${e.message} Exception on Write ")
    }
    Log.i("myIO", "Wrote to file via csvWriter" + fileX.absolutePath)
}
fun checkFile() :String {
    var fileck = ""
    try {
        if (File(fileI).createNewFile()) {
            Log.i("File", "File Created")
            fileck = "new"
           // writeCSV(initCsvFile())
        } else {
            Log.i("File", "$fileI already exists")
            fileck = "old"
        }
    } catch (e: IOException) {
        Log.e("File", "Looks like ${e.message} caused the exception")

    }
    return fileck
}

fun readCSV() : MutableList<Array<String>> {
        var csvReaderData = mutableListOf<Array<String>>()
        try{
            Log.i("myIO","reading csvFile fileX")
            csvReaderData = CSVReader(FileReader(fileX)).readAll()
            Log.i("myIO","after reading csvReaderData size is ${csvReaderData.size}")
        } catch (e: IOException){

            Log.i("myIO","Hit the exception area on ${e.message} checkFIle is ${checkFile()}")
            if (checkFile() == "new" ) {
                csvReaderData = initCsvFile()
                Log.i("myIO", "csvreader data from initPlayers Table")
                Log.e("IOException", "${e.message} Exception ")
                return csvReaderData
            }
        }
        return csvReaderData
}
fun initCsvFile(): MutableList<Array<String>>{

    //used to initialize a file for keeping player score
    val outTable: MutableList<Array<String>> = mutableListOf()
    val header = arrayOf("Header","12 ","11 ","10 ","09 ","08 ","07 ","06 "
        ,"05 ","04 ","03 ","02 ","01 ","00 ")
    outTable.add(header)
    repeat(8){outTable.add(arrayOf(" ","0","0","0","0","0",
                                             "0","0","0","0","0",
                                             "0","0","0"))}

    return outTable
}



