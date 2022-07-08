package com.delafleur.mxt.ui


import android.app.Notification
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.view.SupportActionModeWrapper
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
               inflater.inflate(R.menu.thenavdrawer,   menu)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            sharedVM = sharedViewModel
            scoresummaryFragment = this@ScoresummaryFragment
        }
        binding.scoresummaryFragment = this


        Log.i("readcsv","called from scoresummaryFragment create View")
        if (sharedViewModel.playerT.size == 0 ){
            Log.i("frag","playerT must be empty")
            sharedViewModel.buildPlayersFromCSVrecords(readCSV())
        }

        sharedViewModel.setRoundsScored()

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
    }

    override fun onResume() {
        super.onResume()
        Log.i("resuming Scoresummary","resume score summary")

    }

    fun onNewGameSelected() {
        backupCSV()
        sharedViewModel.buildPlayersFromCSVrecords(readCSV())
        sharedViewModel.setPlayerSummaries()
        sharedViewModel.clearProcess()
        sharedViewModel.setVisibiltyNewGame()

    }
    fun onReturnSelected(){
        /*called from Enter Scores button via onClick="@{() -> scoresummaryFragment.onReturnSelected()}"
        * pass the player text entry fields to sharedViewmodel update and let it take care*
        * of updating the player table
        * */
        val playerNamesArr = arrayOf(
            binding.TR1C1.text.toString(),
            binding.TR2C1.text.toString(),
            binding.TR3C1.text.toString(),
            binding.TR4C1.text.toString(),
            binding.TR5C1.text.toString(),
            binding.TR6C1.text.toString(),
            binding.TR7C1.text.toString(),
            binding.TR8C1.text.toString())
        Log.i("frag","scoreSumary enter scores calling updatePtablefromPlayernames")
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


