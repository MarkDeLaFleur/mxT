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
import com.delafleur.mxt.data.Players
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentScoresummaryBinding
import com.delafleur.mxt.util.*

/*
* This will be the opening screen
* */
class ScoresummaryFragment : Fragment() {
    private var _binding: FragmentScoresummaryBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val fragmentBinding = FragmentScoresummaryBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            sharedVM = sharedViewModel
            scoresummaryFragment = this@ScoresummaryFragment
        }
        binding.scoresummaryFragment = this
        sharedViewModel.buildPlayersFromCSVrecords(readCSV())
        sharedViewModel.setPlayerSummaries()
   /*     playerNames.forEachIndexed{ind,it ->
            it.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus){
                    if (listofPlayers[ind+1][0] != it.text.toString()){
                        listofPlayers[ind+1][0] = it.text.toString()
                        Log.i("myIO","focus change writing ${it.text.toString()}")

                    }
                }
            }
        }
*/
    }

    override fun onStart() {
        super.onStart()
        sharedViewModel.setRoundsScored()

    }



    fun onNewGameSelected() {
        /* do something cool like copy the file and delete the old one.
        * */
        backupCSV()

    }

    fun onReturnSelected(){

        /*navigation time.
        before we do, update the players records with names if the edit text fields have values
        * */
        val playerNamesArr = arrayOf(binding.TR1C1.text.toString(),binding.TR2C1.text.toString(),
            binding.TR3C1.text.toString(),binding.TR4C1.text.toString(),binding.TR5C1.text.toString(),
            binding.TR6C1.text.toString(),binding.TR7C1.text.toString(),binding.TR8C1.text.toString())
        sharedViewModel.updatePtablefromPlayerNames(playerNamesArr)
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


