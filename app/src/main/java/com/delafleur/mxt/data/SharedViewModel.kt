package com.delafleur.mxt.data

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.CameraUtil.JPGtoRGB888
import com.delafleur.mxt.CameraUtil.fixMatRotation
import com.delafleur.mxt.CameraUtil.putNumbersOnCrops
import com.delafleur.mxt.util.writeCSV
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point


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
        Utils.bitmapToMat(cvBitmap, matCVT)  //matCVT is converted to BGRA 4 channel color
        //Imgproc.cvtColor(matCVT,matCVT,Imgproc.COLOR_BGRA2RGB) will convert it to RGB 3 channel
        matCVT = fixMatRotation(matCVT, prev)
        Log.i("SharedViewModel", "MAT size row cols = ${matCVT.rows()},${matCVT.cols()}")
        val wrkMySubmatDomino = CameraUtil.dominoArrayofMat(matCVT)
        if (wrkMySubmatDomino.cropImg.size > 0) {
            var newMat = Mat()
            var smallList = listOf(
                wrkMySubmatDomino.cropImg[0], wrkMySubmatDomino.cropImg[1],wrkMySubmatDomino.cropImg[2], wrkMySubmatDomino.cropImg[3],
                wrkMySubmatDomino.cropImg[4], wrkMySubmatDomino.cropImg[5]
            )
            Core.hconcat(smallList, newMat)
            var bitmapO = Bitmap.createBitmap(newMat.cols(), newMat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(newMat, bitmapO)
            _bitmapx.value = bitmapO
        }
        var showPoints = "Player " +  " Points "
        var totPts = 0
        if (wrkMySubmatDomino.pts.size > 0) {
            wrkMySubmatDomino.pts.forEach {
                showPoints += "("+ it.x.toInt().toString() + "/" + it.y.toInt().toString() +
                        ") + "
                Log.i("pts", "$it + $totPts")
                totPts += (it.x + it.y).toInt()
            }
            showPoints = showPoints.substring(0, showPoints.lastIndexOf("+")) +
                        " = " + totPts.toString()
        }
        _totalPoints.postValue(totPts.toString())
        _displayPts.value = showPoints
        Log.i("ptsC","points from camera ${displayPts.value}")
        Log.i("ptsC","current domino button is ${currentRound.value}")
        Log.i("ptsC","Player index is ${playerIndex.value}")
    }

    fun setPlayer(strg: String){// actually this is the players index. Will be used when totalling domino.
        _playerIndex.value =  strg
        Log.i("view","player ${strg}")
    }
    fun setVisibiltyNewGame () {_playerVisibility.value = mutableListOf<Boolean>(true,true,true,true,true,
    true,true,true)}

    fun setRoundsScored()  {
        // we want to find out which rounds have been scored so we look at each player's score or
        // test score and if the player's score is not "0" then set the flag to true
        val rsAnyScoreInRound =  Array<Boolean>(13){false}
        var rsF =  "Completed Rounds "
        var rsNf = "Not Completed    "
        // looping through all the players to see if 'any' of the players have a score for a round
        playerT.forEach{
            Log.i("anyscores","checking scores for each player ${it.playerName}" +
                    "'s score is ${it.csvRecord().toList()}")

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
            Log.i("visi","player i ${playerT[i].playerName.length}")
            if (playerT[i].playerName.length < 2) {
                visibilityList[i] = false
            }
        }
        Log.i("visi","${visibilityList.joinToString()}")
        _playerVisibility.value = mutableListOf(
            visibilityList[0],visibilityList[1],visibilityList[2],visibilityList[3],
            visibilityList[4],visibilityList[5],visibilityList[6],visibilityList[7])
        _playerLiveDataName.value = mutableListOf(
            playerT[0].playerName,playerT[1].playerName,playerT[2].playerName,
            playerT[3].playerName,playerT[4].playerName,playerT[5].playerName,
            playerT[6].playerName,playerT[7].playerName)

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
        playerT = mutableListOf()
        Log.i("BuildplayerTable","player table inPut size is ${inPut.size}")
        inPut.forEachIndexed { i, j ->
            if (i > 0) {  //skip the header
                val tP = Players(j[0])
                playerT.add(tP)
                playerT[playerT.size-1].score = j.copyOfRange(1,j.size) //getting the score from the csv record
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
            _roundScored.value = "Round " + dominoButton.toString() + " VALUES --> "
            _currentRound.value = dominoButton
        }
        else{
            _roundScored.value = "Round " + dominoButton.toString() + " UPDATED -->"
            _currentRound!!.value = null
            scoresToAdd.forEachIndexed { i, j ->
               if (j.length > 0 && playerT[i].playerName.length > 1) {
                    playerT[i].score[dominoButton] = j}
                }

            //writeCSV(buildCsvRecordsFromPlayerT()) hold off updating here until they go to summaries
            setRoundsScored()
            setPlayerSummaries()


        }
    }
    fun updatePtablefromPlayerNames(playerNamesArr: Array<String>) {
       playerNamesArr.forEachIndexed{  i, rec ->
            if (rec != " ") {
                    playerT[i].playerName = rec
                }
        }
        playerT.forEach { Log.i("out","PlayerT record ${it.csvRecord().toList().joinToString()}")
            Log.i("updatePtable","PlayerT playerName ${it.playerName}")
        }

        writeCSV(buildCsvRecordsFromPlayerT())
        setRoundsScored()
        setPlayerSummaries()
    }
    fun buildCsvRecordsFromPlayerT() :MutableList<Array<String>>{
        // returns a csvRecord Table used by csvWrite
        val header = arrayOf("Header","12 ","11 ","10 ","09 ","08 ","07 ","06 "
            ,"05 ","04 ","03 ","02 ","01 ","00 ")
        val outRec = mutableListOf<Array<String>>()
        outRec.add(header)
        playerT.forEach{
            Log.i("outrec","outrec CSV fun ${it.csvRecord().toList()}")
            outRec.add(it.csvRecord())
        }
        Log.i("outRec","outRec size is ${outRec.size}")
        return outRec
    }




}







