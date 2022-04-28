package com.delafleur.mxt.ui
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.R
import com.delafleur.mxt.databinding.FragmentStartBinding

import com.delafleur.mxt.util.*

class StartFragment : Fragment() {
    /*
* Start of Scores -- before onCreate we need to see if there is an active game by
* checking the dominoScores file
* if it exists and if the file's first record has any blank score header fields then show
* the continue game button and set up a listener. if not then game was complete, so initialize
* the dominoScores file and then navigate to selectPlayers fragment.
* */
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!
    private var listOfPlayers: MutableList<Array<String>> = mutableListOf()
    private var _buttonList: Array<Button> = arrayOf()
    private val buttonList get() = _buttonList
    override fun onResume() {
        super.onResume()
        Log.i("Resume", "${binding.names.childCount} is childCount")


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        _buttonList = arrayOf(
            binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6,
            binding.button7
        )
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            /*check the child count and reset any previous edit views
            *
            *
            *  */
            if (binding.names.childCount > 0) {
                binding.names.removeAllViews()
            }


            /* Based on which button, add Player editText
                    * button 0 is 2, 1 is 3, 2 is 4, 3 is 5, 4 is 6 ... 8*/

            buttonList.forEachIndexed { ind, it ->
                if (checkedId == it.id) {
                    Log.i("StartFrag","Button ${ind+1} was pushed")
                    for (i in -1 until ind+1) {
                        val z = EditText(this.context)
                        z.id = i + 100
                        z.hint = "Player " + (i + 2).toString()
                        binding.names.addView(z)
                        Log.i("StartFrag", "binding -> ${z.hint}")
                        Log.i("StartFrag","childcount ${binding.names.childCount}")
                    }
                }

            }
        }
        binding.newGame.setOnClickListener { view: View ->
            /* when clicking on the start button I want to see if I get the edit text fields
                            NOTE --> i had a heck of a time
                             until I finally figured out that you have to
                            cast the view object as EditText in order to be able to get the data.
                 */
            listOfPlayers.add(
                arrayOf(
                    "Player", "R00", "R01", "R02", "R03", "R04",
                    "R05", "R06", "R07", "R08", "R09", "R10", "R11", "R12"
                )
            )
            for (i in 0..binding.names.childCount - 1) {
                val zx = binding.names.getChildAt(i) as EditText
                val zzx = zx.text.toString()
                Log.i("childAt", "id ${zx.id} ${zzx}")
                listOfPlayers.add(
                    arrayOf(
                        zzx, "0", "0", "0", "0", "0", "0", "0",
                        "0", "0", "0", "0", "0", "0"
                    )
                )
            }
            writeCSV(listOfPlayers)
            Log.i("StartFrag", "wrote  ${listOfPlayers}")
            view.findNavController()
                .navigate(R.id.action_startFragment_to_scoreFragment)
        }

        if (!CameraUtil.checkPermissions(requireContext())) {
            CameraUtil.userRequestPermissions(requireActivity())
        }
        setHasOptionsMenu(true)
        return binding.root
        }
}  /* end of Fragment class */