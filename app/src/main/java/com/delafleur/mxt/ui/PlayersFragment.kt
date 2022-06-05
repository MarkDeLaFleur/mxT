package com.delafleur.mxt.ui

import android.os.Bundle
import android.text.Editable
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentPlayersBinding
import com.delafleur.mxt.util.readCSV
import com.delafleur.mxt.util.writeCSV

/*  Soon to replace the fragment start , score with this. Need to work out putting the date for
*   Player scores in the livedata shared view model and update the domino file from there
*   also need to hook up the camera image button using the function call from the camera xml
*   and then once were good there, we'll get to the summary thing.
*  */


class PlayersFragment : Fragment() {
    private var _binding: FragmentPlayersBinding? = null
    private var listOfPlayers = mutableListOf<Array<String>>()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val binding get() = _binding!!
    //players record 0 is Player,R00x,R01x,R02x,R03X,R04X,R05X...
    //players record 0    0     ,1   ,2   ,3   ,4   ,5   ,6.....
    //button index   0 --12, 1 --11, 2 --10, 3 --09, 4 --08, 5 --07
    private val button2PlayerScore = mapOf(0 to 1,1 to 2,2 to 3,3 to 4, 4 to 5,
                                    5 to 6, 6 to 7,7 to 8,8 to 9,9 to 10,10 to 11,11 to 12,12 to 13)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
           val fragmentBinding = FragmentPlayersBinding.inflate(inflater, container, false)
            _binding = fragmentBinding
            return fragmentBinding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playersFragment = this
        listOfPlayers = readCSV()
        checkPlayers()

    }
    fun checkPlayers(){
            // put in the players names, turn on visibility
        val pName = arrayOf(binding.TextPersonName1,binding.TextPersonName2,
            binding.TextPersonName3,binding.TextPersonName4,
            binding.TextPersonName5,binding.TextPersonName6,
            binding.TextPersonName7,binding.TextPersonName8)
        val tRow = arrayOf(binding.TR1,binding.TR2,binding.TR3,binding.TR4,
            binding.TR5,binding.TR6,binding.TR7,binding.TR8)



        pName.forEachIndexed { index, textV ->
                  if(index < listOfPlayers.size-1){
                    textV.text= listOfPlayers[index+1][0]
                    tRow[index].isVisible = true
                  }
                  else{
                      tRow[index].isVisible = false
                  }
            }
    }
    fun onDominoButtonClicked (roundScored: Int) {
        //clicking the domino button sets up which score from scoresIn is written to the listofplayers
        // but if there is already an entry eg. listofPlayers [0][roundIndex] has a 'G' then
        // put the listofplayers value in the scoresIn field turn off the 'G'
        // if there is no 'G' assume no value has been entered
        // when no 'G' and the button is pushed, then update list of players and set the 'G'
        val dominoButton = arrayOf(binding.imageButton0,binding.imageButton1,binding.imageButton2,
            binding.imageButton3,binding.imageButton4,binding.imageButton5,
            binding.imageButton6,binding.imageButton7,binding.imageButton8,
            binding.imageButton9,binding.imageButton10,binding.imageButton11,
            binding.imageButton12)

        val scoresIn = arrayOf(
            binding.editTextNumber1, binding.editTextNumber2,
            binding.editTextNumber3, binding.editTextNumber4, binding.editTextNumber5,
            binding.editTextNumber6, binding.editTextNumber7, binding.editTextNumber8
        )

        val plScoreInd = button2PlayerScore.getValue(roundScored) //eg.  domino 00 is score index 01
        val newText = "Scoring for Round Double --> " + (roundScored).toString()
        binding.scoreRound.text = newText
        val hasScore = listOfPlayers[0][plScoreInd].contains("G")
        //check players and update
        var totalPts = 0

        listOfPlayers.forEachIndexed { index, it ->

            if (index > 0 &&  hasScore){
                Log.i("round","$plScoreInd has a 'G'")
                listOfPlayers[0][plScoreInd] = listOfPlayers[0][plScoreInd].substring(0,3)
                totalPts = 0
                scoresIn[index - 1].setText(it[plScoreInd])
                writeCSV(listOfPlayers)
            }
            else {
                    if (index > 0 && scoresIn[index - 1].text.toString() != "") {
                        Log.i("round", "Round $roundScored is put into player field $plScoreInd")
                        it[plScoreInd] = scoresIn[index - 1].text.toString()
                        totalPts += it[plScoreInd].toInt()

                    }
                }

            }
            if (totalPts > 0) {
                dominoButton[roundScored].setBackgroundColor((android.graphics.Color.GREEN))
                listOfPlayers[0][plScoreInd] = listOfPlayers[0][plScoreInd].substring(0, 3) + "G"
                writeCSV(listOfPlayers)
                scoresIn.forEach { it.setText("")
                dominoButton[roundScored].requestFocus()
                }
            }
        else{
            dominoButton[roundScored].setBackgroundColor((android.graphics.Color.LTGRAY))
            listOfPlayers[0][plScoreInd] = listOfPlayers[0][plScoreInd].substring(0, 3)
            writeCSV(listOfPlayers)


        }

    }
    fun onScoreSummaryButtonClicked(){
        findNavController().navigate(R.id.action_playersFragment_to_scoresummaryFragment)

    }
    fun onCameraButtonClicked(player: Int){
        Log.i("player","player $player called the camera capture")
        sharedViewModel.setPlayer(player.toString())
        findNavController().navigate((R.id.action_playersFragment_to_cameracaptureFragment))
    }
 }