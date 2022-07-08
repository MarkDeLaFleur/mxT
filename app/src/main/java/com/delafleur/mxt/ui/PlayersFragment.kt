package com.delafleur.mxt.ui

import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.delafleur.mxt.MainActivity
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentPlayersBinding
import com.delafleur.mxt.util.writeCSV
import com.google.android.material.snackbar.Snackbar


class PlayersFragment : Fragment() {
    private var _binding: FragmentPlayersBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val binding get() = _binding!!
    private var plScoreInd = 0   //map result from roundScored
    private var checkCameraP: Boolean = false
    private var camPts: String = ""
    private var plyrInd: String = ""


    private val button2PlayerScore = mapOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5,
        5 to 6, 6 to 7, 7 to 8, 8 to 9, 9 to 10, 10 to 11, 11 to 12, 12 to 13
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentPlayersBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playersFragment = this

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            sharedVM = sharedViewModel
            playersFragment = this@PlayersFragment
        }
        Log.i("frag","player fragment - view created doing setplayer summaries")
        sharedViewModel.setPlayerSummaries()
        val checkCameraPts = Observer<String> { pts -> camPts = pts }
        sharedViewModel.totalPoints.observe(viewLifecycleOwner, checkCameraPts)
        val checkCameraPlayerindex = Observer<String> { pInd -> plyrInd = pInd }
        sharedViewModel.playerIndex.observe(viewLifecycleOwner, checkCameraPlayerindex)

    // val checkRoundScored = Observer<String> { pRndo ->  pRnd =
        //     pRndo}
        // sharedViewModel.roundScored .observe(viewLifecycleOwner,checkRoundScored)



    }

    override fun onResume() {
        super.onResume()
    }

    fun onDominoButtonClicked(dominoButtonRound: Int) {
        val scoresIn = arrayOf(
            binding.editTextNumber1.text.toString(),
            binding.editTextNumber2.text.toString(),
            binding.editTextNumber3.text.toString(),
            binding.editTextNumber4.text.toString(),
            binding.editTextNumber5.text.toString(),
            binding.editTextNumber6.text.toString(),
            binding.editTextNumber7.text.toString(),
            binding.editTextNumber8.text.toString()
        )
        Log.i("frag","playersFrag domino button click")
        sharedViewModel.addScoresToPlayerT(dominoButtonRound, scoresIn)


    }

    fun onScoreSummaryButtonClicked() {
        //update the csvfile when navigating back to summary
        writeCSV(sharedViewModel.buildCsvRecordsFromPlayerT())
        findNavController().navigate(R.id.action_playersFragment_to_scoresummaryFragment)

    }

    fun onCameraButtonClicked(playerIndex: Int) {
        Log.i(
            "player", "player ${sharedViewModel.playerT[playerIndex].playerName} called the camera capture" +
                    " ${sharedViewModel.currentRound.value}"
        )
        if (sharedViewModel.currentRound.value == null) {
            val sn = Snackbar.make(binding.TextPersonName1,
                "Please select which round you are capturing", Snackbar.LENGTH_LONG)
            sn.show()
        } else {
            sharedViewModel.setPlayer(playerIndex.toString())
            findNavController().navigate((R.id.action_playersFragment_to_cameracaptureFragment))
        }
    }
}