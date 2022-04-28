package com.delafleur.mxt.data

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameracaptureViewModel : ViewModel() {
   /* private val _params = MutableStateFlow(Params.ThresholdParams(128.0, 255.0))
   */
    private val _params = MutableStateFlow(Params.BlobParam("dude"))
    val params: StateFlow<Params.BlobParam> = _params

    fun onnutsChange (data: String){_params.value.copy(nuts = data)

     Log.i("nuts","yep now ${params.value.nuts}")
    }


}
