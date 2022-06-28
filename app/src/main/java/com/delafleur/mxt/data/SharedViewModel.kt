package com.delafleur.mxt.data

import android.graphics.Bitmap
import android.text.BoringLayout
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.CameraUtil.JPGtoRGB888
import com.delafleur.mxt.CameraUtil.PointsfromCroppedImage
import com.delafleur.mxt.CameraUtil.PointsfromDomino
import com.delafleur.mxt.CameraUtil.fixMatRotation
import com.delafleur.mxt.CameraUtil.putNumbersOnCrops
import com.delafleur.mxt.util.writeCSV
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat


class SharedViewModel : ViewModel() {


    private val _playerLiveDataName  = MutableLiveData<List<String>>()
    private val _playerLiveDataSumStr = MutableLiveData<List<String>>()
    private val _playerLiveDataSumTot = MutableLiveData<List<String>>()
    private val _scoreFieldLiveData = MutableLiveData<List<String>>()
    private val _playerIndex = MutableLiveData<String>()
    private val _playerVisibility = MutableLiveData<List<Boolean>>()
    private val _totalPoints = MutableLiveData<String>()
    private val _displayPts = MutableLiveData<String>()
    private val _bitmapx = MutableLiveData<Bitmap>()
    private val _roundScored = MutableLiveData<String>()
    private val _currentRound = MutableLiveData<Int>()
    private val _roundsScored = MutableLiveData<String>()
    private val _roundsNotScored = MutableLiveData<String>()
    private val _scoredRounds = MutableLiveData<List<Boolean>>()
    val scoreFieldLiveData  :LiveData<List<String>> = _scoreFieldLiveData
    val playerLiveDataName :LiveData<List<String>> = _playerLiveDataName
    val playerLiveDataSumStr: LiveData<List<String>> = _playerLiveDataSumStr
    val playerLiveDataSumTot: LiveData<List<String>> = _playerLiveDataSumTot
    val playerVisibility: LiveData<List<Boolean>> = _playerVisibility
    val playerIndex: LiveData<String> = _playerIndex
    val totalPoints: LiveData<String>   = _totalPoints
    val displayPts: LiveData<String> = _displayPts
    val bitmapX: LiveData<Bitmap> = _bitmapx
    val roundScored: LiveData<String>   = _roundScored
    val currentRound: LiveData<Int> = _currentRound
    val scoredRounds: LiveData<List<Boolean>> = _scoredRounds
    val roundsScored: LiveData<String> = _roundsScored
    val roundSNotScored: LiveData<String> = _roundsNotScored
    var playerT: MutableList<Players> = mutableListOf()


    fun imageRect (image : ImageProxy,prev: PreviewView) {
        val cvBitmap = JPGtoRGB888(CameraUtil.imageProxyToBitmap(image))
        var matCVT = Mat()
        Utils.bitmapToMat(cvBitmap, matCVT)
        matCVT = fixMatRotation(matCVT, prev)
        Log.i("SharedViewModel", "MAT size row cols = ${matCVT.rows()},${matCVT.cols()}")
        val rectanglesfromImage = CameraUtil.dominoArray(matCVT)
        var pts = PointsfromDomino(rectanglesfromImage, matCVT)
        Log.i(
            "camera",
            "domino points ${pts.toList().toString()} rects ${rectanglesfromImage.size} "
        )
        //just a test
        var matCrops = CameraUtil.dominoArrayofMat(matCVT).toList()
        if (matCrops.size > 0) {
            // before concatenation put the points on the matCrops
            pts = PointsfromCroppedImage(matCrops)
            var newMat = Mat()
            matCrops = putNumbersOnCrops(matCrops,pts) // don't need to put a box around the image
            var smallList = listOf(matCrops[0],matCrops[1],matCrops[2],matCrops[3],
                                    matCrops[5],matCrops[6])
            Core.hconcat(smallList, newMat)
            var bitmapO = Bitmap.createBitmap(newMat.cols(), newMat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(newMat, bitmapO)
            _bitmapx.value = bitmapO
        // through here to work with pulling just the dominos from the camptured image rotating it
            // then handing it off to the point counting routine.
        } else{
            if (rectanglesfromImage.size > 0)
                _bitmapx.value =
                    CameraUtil.PutptsRectsonImage(rectanglesfromImage, pts, matCVT) else
                _bitmapx.value = cvBitmap!!
        }
        var showPoints = "Player " +  " Points "
        var totPts = 0
        if (pts.size > 0) {
            pts.forEach {
                showPoints += it.toString() + " + "
                Log.i("pts", "$it + $totPts")
                totPts += it
            }
            showPoints =
                showPoints.substring(0, showPoints.lastIndexOf("+")) + " = " + totPts.toString()
        }
        _totalPoints.postValue( totPts.toString())
        _displayPts.value = showPoints
        Log.i("ptsC","points from camera ${displayPts.value}")
        Log.i("ptsC","current domino button is ${currentRound.value}")
        Log.i("ptsC","Player index is ${playerIndex.value}")
    }

    fun setPlayer(strg: String){// actually this is the players index. Will be used when totalling domino.
        _playerIndex.value =  strg
        Log.i("view","player ${strg}")
    }

    fun setRoundsScored()  {
        // we want to find out which rounds have been scored so we look at each player's score or
        // test score and if the player's score is not "0" then set the flag to true
        val rsAnyScoreInRound =  Array<Boolean>(13){false}
        var rsF =  "Completed Rounds "
        var rsNf = "Not Completed    "
        // looping through all the players to see if 'any' of the players have a score for a round
        playerT.forEach{
             it.score.forEachIndexed{i,j ->
                   if(j != "0")  rsAnyScoreInRound[i] = true
            }
        }
        rsAnyScoreInRound.forEachIndexed { i,j ->  //build the string for rounds completed/ not completed
            rsF += if (j)  i.toString()+", "  else ""
            rsNf += if (!j) i.toString()+", " else ""
        }
        _scoredRounds.value = mutableListOf(rsAnyScoreInRound[0],rsAnyScoreInRound[1],
            rsAnyScoreInRound[2],rsAnyScoreInRound[3],
            rsAnyScoreInRound[4],rsAnyScoreInRound[5],
            rsAnyScoreInRound[6],rsAnyScoreInRound[7],
            rsAnyScoreInRound[8],rsAnyScoreInRound[9],
            rsAnyScoreInRound[10],rsAnyScoreInRound[11],
            rsAnyScoreInRound[12])
        _roundsScored.value = if(rsF.lastIndexOf(", ") > 0 )
                                rsF.substring(0,rsF.lastIndexOf(", ")) else rsF+ " None"
        _roundsNotScored.value = if(rsNf.lastIndexOf(", ") >0 )
                                rsNf.substring(0,rsNf.lastIndexOf(", ")) else rsNf + " None"
    }
    fun refreshScoreLiveData(domButton: Int){
        _scoreFieldLiveData.value = mutableListOf(
            playerT[0].score[domButton],
            playerT[1].score[domButton],
            playerT[2].score[domButton],
            playerT[3].score[domButton],
            playerT[4].score[domButton],
            playerT[5].score[domButton],
            playerT[6].score[domButton],
            playerT[7].score[domButton]

        )

    }
    fun setPlayerSummaries(){
        val visibilityList = MutableList<Boolean>(8){true}

        visibilityList.forEachIndexed { i, it ->
            Log.i("visi","player i ${playerT[i].record[0].length}")
            if (playerT[i].record[0].length < 2) {
                visibilityList[i] = false
            }
        }
        Log.i("visi","${visibilityList.joinToString()}")
        _playerVisibility.value = mutableListOf(
            visibilityList[0],visibilityList[1],visibilityList[2],visibilityList[3],
            visibilityList[4],visibilityList[5],visibilityList[6],visibilityList[7])
        _playerLiveDataName.value = mutableListOf(
            playerT[0].record[0],playerT[1].record[0],playerT[2].record[0],
            playerT[3].record[0],playerT[4].record[0],playerT[5].record[0],
            playerT[6].record[0],playerT[7].record[0])

        //change scoreTotalStrTest to scoreTotalStr and scoreTotIntTest to scoreTotInt
        _playerLiveDataSumStr.value = mutableListOf(
            playerT[0].scoreTotalStr(),playerT[1].scoreTotalStr(),playerT[2].scoreTotalStr(),
            playerT[3].scoreTotalStr(),playerT[4].scoreTotalStr(),playerT[5].scoreTotalStr(),
            playerT[6].scoreTotalStr(),playerT[7].scoreTotalStr() )
        _playerLiveDataSumTot.value = mutableListOf(
            playerT[0].scoreTotInt().toString(),playerT[1].scoreTotInt().toString(),
            playerT[2].scoreTotInt().toString(),playerT[3].scoreTotInt().toString(),
            playerT[4].scoreTotInt().toString(),playerT[5].scoreTotInt().toString(),
            playerT[6].scoreTotInt().toString(),playerT[7].scoreTotInt().toString() )
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
        //playerT = mutableListOf()


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
                Log.i("playerT","${playerT[playerT.size-1].playerName} is ${playerT[i-1].score.toList()}")
            }
        }
    }
    fun processCameraPoints(){
        Log.i("ptsP","points from camera ${totalPoints.value}")
        Log.i("ptsP","current domino button is ${currentRound.value}")
        Log.i("ptsP","Player index is ${playerIndex.value}")
        playerT[playerIndex.value!!.toInt() ].
        score[currentRound.value!!] = totalPoints!!.value.toString()
        refreshScoreLiveData(currentRound.value!!)
       // _currentRound!!.value = null

    }
    fun addScoresToPlayerT(dominoButton :Int,scoresToAdd: Array<String>){
        Log.i("scoresUpdate","current round value ${currentRound.value}")
        if(currentRound.value != dominoButton ){
             refreshScoreLiveData(dominoButton)
            _roundScored.value = "Round " + dominoButton.toString() + " Values are --> "
            _currentRound.value = dominoButton


        }
        else{
            _roundScored.value = "Round " + dominoButton.toString() + "have been UPDATED"
            _currentRound!!.value = null
            scoresToAdd.forEachIndexed { i, j ->
                if (j == "") playerT[i].score[dominoButton] = "0" else j
            }
        }
    }
    fun updatePtablefromPlayerNames(playerNamesArr: Array<String>) {
       playerNamesArr.forEachIndexed{  i, rec ->
            if (rec != playerT[i].record[0] ) {
                    playerT[i].record[0] = rec
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
    fun checkPts(playerNo: Int) :Int{
        return(playerT[playerNo].record[0].toInt())
    }




}







