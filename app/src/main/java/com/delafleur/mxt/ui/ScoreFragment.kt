package com.delafleur.mxt.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import android.graphics.Color as agC
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.R
import com.delafleur.mxt.databinding.FragmentScoreBinding
import com.delafleur.mxt.databinding.FragmentStartBinding
import com.delafleur.mxt.util.*
import java.io.FileOutputStream
import java.io.FileWriter


class ScoreFragment : Fragment() {
private var _binding: FragmentScoreBinding? = null
private val binding get() = _binding!!
    private var _pView: Array<TextView> = arrayOf() /* Player */
    private val pView get() = _pView
    private var _sView: Array<EditText> = arrayOf() /* Score */
    private val sView get() = _sView
    private var _iView: Array<ImageButton> = arrayOf() /*Domino Round */
    private val iView get() =_iView
    private var _tView: Array<TableRow> = arrayOf()  /* Table Row */
    private val tView get() = _tView
    private var _cView: Array<ImageButton> = arrayOf()  /* Camera Button */
    private val cView get() = _cView

    /* read the list of players from the the file*/
    private val listOfPlayers: MutableList<Array<String>> = readCSV()
    /* got the listOfPlayers from the file. */
    private var oldRound = 99 /* used for keeping track of scores*/
    val but2Score = mapOf(1 to 11,2 to 10, 3 to 9, 4 to 8, 5 to 7, 6 to 6, 7 to 5, 8 to 4,
        9 to 3, 10 to 2, 11 to 1, 12 to 0, 0 to 12)




    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScoreBinding.inflate(inflater, container, false)
    /* populate the score fields by creating the textviews for the players and then
    * create the editText views for the scores. we need to add listeners
    * for the domino icons and show which domino the scores are for
    * we can also use the icons 'change status to update the
    * scores and write to the csv file. we'll work adding the camera icon as well
    * or we might test using a long click on the players name to navigate to the camera
    * app but for beginning we'll just enter the scores then update the csv file. */
    /* Set the domino image button background depending on the list of Players [0][1.12]
    * values  - white if field is '3rd char blank' black if '3rd char C complete
    * green if third char is A (active) */
        _pView = arrayOf(
            binding.player1textView,binding.player2textView,binding.player3textView,
            binding.player4textView,binding.player5textView,binding.player6textView,
            binding.player7textView,binding.player8textView)

        _sView = arrayOf(
            binding.player1editTextNumber,binding.player2editTextNumber,binding.player3editTextNumber,
            binding.player4editTextNumber,binding.player5editTextNumber,binding.player6editTextNumber,
            binding.player7editTextNumber,binding.player8editTextNumber)

        _iView = arrayOf(binding.imageButton12,binding.imageButton11,binding.imageButton10,
            binding.imageButton09,binding.imageButton08,binding.imageButton07,binding.imageButton06,
            binding.imageButton05,binding.imageButton04,binding.imageButton03,binding.imageButton02,
            binding.imageButton01,binding.imageButton00)

        _tView = arrayOf(binding.TR1,binding.TR2,binding.TR3,binding.TR4,binding.TR5,binding.TR6,
                         binding.TR7,binding.TR8)
        _cView = arrayOf(binding.player1imageButton, binding.player2imageButton,binding.player3imageButton, binding.player4imageButton,
                         binding.player5imageButton, binding.player6imageButton,binding.player7imageButton, binding.player8imageButton)

        for(i in 0 until (listOfPlayers.size)-1) {
            pView[i].text = listOfPlayers[i+1][0]
            tView[i].visibility = View.VISIBLE /* set visibility by table row */

        }
        iView.forEachIndexed { ind, vi ->
            vi.setOnClickListener {
                myFunc( ind)
            }
        }
        cView.forEachIndexed { ind, vi ->
            vi.setOnClickListener {
            writeCSV(listOfPlayers)
                view?.findNavController()?.navigate(R.id.action_scoreFragment_to_cameracaptureFragment)

            }
        }

        binding.summaryPage.setOnClickListener { view: View ->
            writeCSV(listOfPlayers)
            view.findNavController().navigate(R.id.action_scoreFragment_to_summaryPageFragment)
        }
        setHasOptionsMenu(true)
        return binding.root

    }
    override fun onStart(){
        super.onStart()
        setImageColor()


    }
    private fun myFunc(scoreRound :Int) {
        /*  scoreround is the button so 0 is 12 is 1 is 11
        * but in player record, button 0 is player1 record position 13, button 1 is
        * player record position 12, etc to button 12 = player position 1
        * */
        var showScore = ""
        val scoreIn = but2Score.getValue(scoreRound)
        val newText = "Scoring for Round Double --> " + scoreIn
        binding.textView5.text = newText
        val scoreTitle = listOfPlayers[0][scoreIn+1]
        Log.i("Score","ScoreTitle $scoreTitle")
        listOfPlayers[0][scoreIn+1] = scoreTitle.substring(0,3)+"C"
        Log.i("Scores","show change to C for $scoreIn ${listOfPlayers[0][scoreIn+1]}")
        setImageColor()
        listOfPlayers[0][scoreIn+1] = scoreTitle.substring(0,3)+"F"
        for (i in 1 until listOfPlayers.size) {
           listOfPlayers[i][scoreIn+1]= sView[i-1].text.toString()
           showScore = listOfPlayers[i][scoreRound+1]
            Log.i("Score","Player ${listOfPlayers[i][0]} round " +
                    "${listOfPlayers[i][scoreIn+1]} for round ${scoreIn+1}")

       }
    }
    fun setImageColor(){
        for (x in 1 until 14){

            if (listOfPlayers[0][x].contains("C")){
                Log.i("Color","$x contains a C")
                iView[but2Score.getValue(x-1)].setBackgroundColor((android.graphics.Color.GREEN))
            }
            if (listOfPlayers[0][x].contains("F")){
                iView[but2Score.getValue(x-1)].setBackgroundColor((android.graphics.Color.YELLOW))
            }
        }

    }

}