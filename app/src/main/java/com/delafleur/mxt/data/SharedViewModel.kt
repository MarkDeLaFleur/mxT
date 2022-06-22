package com.delafleur.mxt.data

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.CameraUtil.PointsfromDomino
import com.delafleur.mxt.CameraUtil.fixMatRotation
import com.delafleur.mxt.util.writeCSV
import org.opencv.android.Utils
import org.opencv.core.Mat


class SharedViewModel : ViewModel() {


    private val _playerLiveDataName  = MutableLiveData<List<String>>()
    val playerLiveDataName :LiveData<List<String>> = _playerLiveDataName

    private val _playerLiveDataSumStr = MutableLiveData<List<String>>()
    val playerLiveDataSumStr: LiveData<List<String>> = _playerLiveDataSumStr

    private val _playerLiveDataSumTot = MutableLiveData<List<String>>()
    val playerLiveDataSumTot: LiveData<List<String>> = _playerLiveDataSumTot
    private val _playerIndex = MutableLiveData<String>()
    private val _totalPoints = MutableLiveData<String>()
    private val _displayPts = MutableLiveData<String>()
    private val _bitmapx = MutableLiveData<Bitmap>()
    private val _roundScored = MutableLiveData<Int>()
    private val _roundsScored = MutableLiveData<String>()
    private val _roundsNotScored = MutableLiveData<String>()

    val bitmapX: LiveData<Bitmap> = _bitmapx
    val playerIndex: LiveData<String> = _playerIndex
    val totalPoints: LiveData<String>   = _totalPoints
    val displayPts: LiveData<String> = _displayPts
    val roundScored: LiveData<Int>   = _roundScored
    val roundsScored: LiveData<String> = _roundsScored
    val roundSNotScored: LiveData<String> = _roundsNotScored
    var playerT: MutableList<Players> = mutableListOf()
    fun imageRect (image : ImageProxy,prev: PreviewView){
        val cvBitmap = CameraUtil.JPGtoRGB888(CameraUtil.imageProxyToBitmap(image))
        var matCVT = Mat()
        Utils.bitmapToMat(cvBitmap,matCVT)
        matCVT = fixMatRotation(matCVT,prev)
        Log.i("SharedViewModel","MAT size row cols = ${matCVT.rows()},${matCVT.cols()}")
        val rectanglesfromImage = CameraUtil.dominoArray(matCVT)
        val pts = PointsfromDomino(rectanglesfromImage,matCVT)
        _bitmapx.value = CameraUtil.PutptsRectsonImage(rectanglesfromImage,pts,matCVT)
        var showPoints = "Player " +  " Points "
        var totPts = 0
        pts.forEach { showPoints += it.toString() + " + "
            Log.i("pts","$it + $totPts")
            totPts+=it;}
        showPoints = showPoints.substring(0,showPoints.lastIndexOf("+"))+ " = "+ totPts.toString()
        _totalPoints.postValue( totPts.toString())
        _displayPts.value = showPoints
    }
    fun setPlayer(strg: String){// actually this is the players index. Will be used when totalling domino.
        _playerIndex.value =  strg
        Log.i("view","player ${strg}")
    }
    fun setRoundsScored()  {
        var rsFlags =  Array(13){" "}
        var rsF =  "Completed Rounds "
        var rsNf = "Not Completed    "
        // looping through all the players to see if 'any' of the players have a score for a round
        playerT.forEach{
            val tt = it.testScore  //change to it.score for real
            tt.forEachIndexed {i,j ->
                if(j != "0")    { rsFlags[i] = "X"   }
            }
        }
        rsFlags.forEachIndexed { i,j ->  //build the string for rounds completed/ not completed
            rsF += if (j == "X")  i.toString()+", "  else ""
            rsNf += if (j != "X") i.toString()+", " else ""
        }
        Log.i("rounds","rsF is $rsF")
        Log.i("rounds","rsNf is $rsNf")
        _roundsScored.value = if(rsF.lastIndexOf(", ") > 0 )
                                rsF.substring(0,rsF.lastIndexOf(", ")) else rsF+ " None"
        _roundsNotScored.value = if(rsNf.lastIndexOf(", ") >0 )
                                rsNf.substring(0,rsNf.lastIndexOf(", ")) else rsNf + " None"
    }
    fun setPlayerSummaries(){
        _playerLiveDataName.value =  mutableListOf(
            playerT[0].playerName,
            playerT[1].playerName,
            playerT[2].playerName,
            playerT[3].playerName,
            playerT[4].playerName,
            playerT[5].playerName,
            playerT[6].playerName,
            playerT[7].playerName)


        _playerLiveDataSumStr.value = mutableListOf(
            playerT[0].totalString(playerT[0].testScore),
            playerT[1].totalString(playerT[1].testScore),
            playerT[2].totalString(playerT[2].testScore),
            playerT[3].totalString(playerT[3].testScore),
            playerT[4].totalString(playerT[4].testScore),
            playerT[5].totalString(playerT[5].testScore),
            playerT[6].totalString(playerT[6].testScore),
            playerT[7].totalString(playerT[7].testScore)  )
        _playerLiveDataSumTot.value = mutableListOf(
            playerT[0].totalScore(playerT[0].testScore).toString(),
            playerT[1].totalScore(playerT[1].testScore).toString(),
            playerT[2].totalScore(playerT[2].testScore).toString(),
            playerT[3].totalScore(playerT[3].testScore).toString(),
            playerT[4].totalScore(playerT[4].testScore).toString(),
            playerT[5].totalScore(playerT[5].testScore).toString(),
            playerT[6].totalScore(playerT[6].testScore).toString(),
            playerT[7].totalScore(playerT[7].testScore).toString() )
    }

    fun clearProcess(){

        _playerIndex.value = ""
        _totalPoints.value = ""
    }
    fun buildPlayersFromCSVrecords(inPut: MutableList<Array<String>>) {
        /* Using CSV records from file. First record is a header record but we don't use it.
        when we create the output, since it could be used in a spread sheet, we insert the header.
        *  */
        // initialize it first
        playerT = mutableListOf()

        Log.i("playerTable","player table inPut size is ${inPut.size}")
        inPut.forEachIndexed{i,j ->
            if (i>0){
                /*  building the player table from the csv Record. playerT is an array of class
                Players that is initialized from csv Record - name if field 1 of the csv record 2
                scores are fields 2 - 14 ( ROUNDS 0 THROUGH  12 ) for each player
                * */
                val tP = Players(j[0])  //input from csv record field 0 is player Name
                val jless1 = j.drop(1).toTypedArray()
                playerT.add(tP)
                //jless1.forEachIndexed {indices,it ->
                //    playerT[playerT.size-1].testScore[indices] = it  //change to score for real
                //}
                Log.i("playerT","${playerT[playerT.size-1].playerName} is ${playerT[i-1].testScore.toList()}")
            }
        }
    }
    fun updatePtablefromPlayerNames(playerNamesArr: Array<String>) {
        Log.i("onReturn","playerNames index ${playerNamesArr.size}")
        Log.i("onReturn","playerT size ${playerT.size}")

        playerT.forEachIndexed { indices, it ->
            Log.i("check","indices is $indices and record is ${it.record.toList().toString()}  ")
            if (it.record[0] != playerNamesArr[indices]) {
                Log.i("onReturn", "${it.playerName} should be ${playerNamesArr[indices]}")
                it.record[0] = playerNamesArr[indices]
                //sharedViewModel.playerT[indices].playerName = playerNamesArr[indices]
            }
        }
        playerT.forEach { Log.i("out","PlayerT record ${it.record.toList().joinToString()}")
            Log.i("out","PlayerT playerName ${it.playerName}")}
        writeCSV(buildCsvRecordsFromPlayerT())

    }
    fun buildCsvRecordsFromPlayerT() :MutableList<Array<String>>{
        // returns a csvRecord Table used by csvWrite
        val header = arrayOf("Header","12 ","11 ","10 ","09 ","08 ","07 ","06 "
            ,"05 ","04 ","03 ","02 ","01 ","00 ")
        val outRec = mutableListOf<Array<String>>()
        outRec.add(header)
        playerT.forEach{
            Log.i("outrec","outRec = ${it.record.toList().toString()}")
            outRec.add(it.record)
        }
        Log.i("outRec","outRec size is ${outRec.size}")
        return outRec
    }

}







