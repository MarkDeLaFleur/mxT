package com.delafleur.mxt.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentPlayersBinding
import com.delafleur.mxt.databinding.FragmentScoresummaryBinding
import com.delafleur.mxt.util.backupCSV
import com.delafleur.mxt.util.readCSV
import com.delafleur.mxt.util.writeCSV

/*
* This will be the opening screen
* it will read the domino csv file and see if there is an existing game.
* if there is then the screen will check the header and see how many rounds are scored
* and list Rounds scored, rounds left then depending on the rounds it will fill in the
* players names  total points and the points for each round.
* if there is only a header record then turn on the select players layout and fill in the player
* fields for the number of players.
* if there is rounds that have not been scored, then navigate to the score fragment
* the score fragment has a button that will take you to the score summary fragment where you
* can select the new game button if you don't want to continue the current game.
* */
class ScoresummaryFragment : Fragment() {
    private var _binding: FragmentScoresummaryBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var listofPlayers = readCSV()
    private var playerNames = mutableListOf<EditText>()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val fragmentBinding = FragmentScoresummaryBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scoresummaryFragment = this

        playerNames = arrayListOf(binding.TR1C1,binding.TR2C1,binding.TR3C1,
                                 binding.TR4C1,binding.TR5C1, binding.TR6C1,binding.TR7C1, binding.TR8C1)

        playerNames.forEachIndexed{ind,it ->
            it.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus){
                    if (listofPlayers[ind+1][0] != it.text.toString()){
                        listofPlayers[ind+1][0] = it.text.toString()
                        writeCSV(listofPlayers)
                    }
                }
            }
        }
        checkPlayerTable()
    }

    override fun onStart() {
        super.onStart()
        listofPlayers= readCSV()
        checkPlayerTable()
        setPlayervisibility()
        playerScoresummary(playerNames)

    }

    fun checkPlayerTable(){
        if (listofPlayers.size > 1){
            // we have a game already
            binding.radioGroup.isVisible = false
            binding.textView2.isVisible = false
        }
        else{
            // we need to start a new game
            Log.i("new Game","")
            binding.radioGroup.isVisible = true
            binding.textView2.isVisible = true
        }
    }

    fun setPlayervisibility() {
        val tr = listOf<TableRow>(binding.TR1, binding.TR2, binding.TR3,
            binding.TR4, binding.TR5, binding.TR6, binding.TR7, binding.TR8
        )
        listofPlayers.forEachIndexed {j,_ ->
            if( j > 1)
            { tr[j-1].isVisible = true}

        }
        tr.forEachIndexed {j,v ->
            if(j > listofPlayers.size-2){
                v.isVisible = false
            }
        }
    }
    fun onPlayersSelected(numSelected: Int) {
        Log.i("what?", "onPlayerSelected is called")
        /* turn visibility on or off  called from radio buttons */
        if (binding.radioGroup.isVisible) {
            Log.i("what?","${binding.radioGroup.isVisible}")
            playerNames = mutableListOf(binding.TR1C1, binding.TR2C1,binding.TR3C1, binding.TR4C1,
                                        binding.TR5C1, binding.TR6C1,binding.TR7C1, binding.TR8C1
            )
            var pnam = ""
            val x = listOf<TableRow>(binding.TR1, binding.TR2, binding.TR3,binding.TR4,
                                     binding.TR5, binding.TR6, binding.TR7, binding.TR8
            )
            Log.i("select", "${numSelected} is the num passed from the view")
            x.forEachIndexed { ind, it ->
                if (ind <= numSelected) {
                    it.isVisible = true
                    pnam = "P" + (ind + 1)
                    listofPlayers.add(
                        arrayOf(
                            pnam, "0", "0", "0", "0", "0", "0",
                            "0", "0", "0", "0", "0", "0", "0"
                        )
                    )
                    playerNames[ind].setText(pnam)
                } else {
                    it.isVisible = false
                }
            }
            writeCSV(listofPlayers)
        }
    }
    fun onNewGameSelected() {
        /* do something cool like copy the file and delete the old one.
        * */
        backupCSV()

    }

    fun playerScoresummary(playerNames: MutableList<EditText>) {

        val playerTots = mutableListOf<TextView>(binding.TR1C2, binding.TR2C2, binding.TR3C2,
            binding.TR4C2, binding.TR5C2, binding.TR6C2,
            binding.TR7C2, binding.TR8C2
        )
        val playerPts = mutableListOf(binding.TR1C3, binding.TR2C3,binding.TR3C3,
                                                binding.TR4C3, binding.TR5C3, binding.TR6C3,
                                                binding.TR7C3, binding.TR8C3)
        val summaryScores = mutableListOf(binding.roundsScored,binding.roundsNotScored)
        var roundsScored = ""
        var roundsNotScored = ""
        val indices = ArrayList<Int>()
        listofPlayers[0].forEachIndexed { index, s ->
            if (s.contains("G")) {
                roundsScored += s.substring(0, 3) + ","
                indices.add(index)
            } else {
                roundsNotScored += s.substring(0, 3) + ","
            }
        }
        val summaryTxt = "Rounds Scored - "+ roundsScored
        val summaryTxt1 = "Rounds Not Scored" + roundsNotScored.substring(3)
        summaryScores[0].text = summaryTxt
        summaryScores[1].text = summaryTxt1
        writeCSV(listofPlayers)  //best to just do a write so that we get the listofplayers in sync
        playerNames.forEach { Log.i("Names","${it.text},${it.textSize}") }

        for (j in 1 until listofPlayers.size) {
            playerNames[j - 1].setText(listofPlayers[j][0]) //using onfocuschange listener and updating listofplayers
            var playerPtstr=""
            var playerTotalPts=0
            indices.forEach {
                playerPtstr += listofPlayers[j][it] + "+"
                playerTotalPts += listofPlayers[j][it].toInt()
            }
            playerTots[j-1].text = playerTotalPts.toString()
            playerPts[j-1].text = playerPtstr.substring(0,playerPtstr.length-1)
        }
    }
    fun onReturnSelected(){
        /*navigation time.
        * */
        findNavController().navigate(R.id.action_scoresummaryFragment_to_playersFragment)
    }
    /**
     * This fragment lifecycle method is called when the view hierarchy associated with the fragment
     * is being removed. As a result, clear out the binding object.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


