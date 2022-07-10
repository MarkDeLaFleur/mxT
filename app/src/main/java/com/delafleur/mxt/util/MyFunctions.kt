package com.delafleur.mxt.util

import android.os.Environment
import android.util.Log
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val fileName = "mXtrainTest.csv"
val fileI = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
    fileName).absolutePath  // note this is actually returning a full pathname as a string
val fileX = File(fileI)
val fileSamsung = File("sdcard/Download/"+ fileName)
fun backupCSV(){
    val fileNameOut = "dominoS" + DateTimeFormatter.ofPattern("yyyyLLLLddhhmmss").format(LocalDateTime.now()) + ".csv"
    val fileO = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileNameOut).absolutePath
    try{
        File(fileI).copyTo(File(fileO),overwrite = true)
        Log.i("backup","file backed up to ${fileO}")
        File(fileI).delete()

    }catch (e: IOException){
        if (checkFile())Log.i("backup","no file to backup, created new one")
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
fun checkFile() :Boolean {
    var fileck = false
    try {
        if (File(fileI).createNewFile()) {
            Log.i("File", "File Created")
            fileck = true
        } else {
            Log.i("File", "$fileI already exists")
            fileck = false
        }
    } catch (e: IOException) {
        Log.e("File", "Looks like ${e.message} caused the exception")

    }
    return fileck
}

fun readCSV() : MutableList<Array<String>> {
        var csvReaderData = mutableListOf<Array<String>>()
        try{
            Log.i("myIO","reading csvFile ${fileSamsung}")
            csvReaderData = CSVReader(FileReader(fileSamsung)).readAll()
            if (csvReaderData.size == 0){
                Log.i("myIO","File was created loading playerT from initfile")
                return initCsvFile()
            }
        } catch (e: IOException){

            Log.i("myIO","Hit the exception area on ${e.message} checkFIle is ${checkFile()}")
            Log.i("myIO", "csvreader data from initPlayers Table")
            Log.e("IOException", "${e.message} Exception ")
                csvReaderData = initCsvFile()
                writeCSV(csvReaderData)
                return csvReaderData

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



