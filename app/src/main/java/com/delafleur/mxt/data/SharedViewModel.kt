package com.delafleur.mxt.data

import android.graphics.Bitmap
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TableRow
import android.widget.TextView
import androidx.camera.core.ImageProxy
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.util.readCSV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.apache.commons.lang3.mutable.Mutable
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.text.NumberFormat



class SharedViewModel : ViewModel() {

    private val _playerName = MutableLiveData<String>()
    private val  _matImage = MutableLiveData <Mat>()
    private val _bitmapx = MutableLiveData<Bitmap>()
    val bitmapX: LiveData<Bitmap> = _bitmapx
    fun select(item: Bitmap){
        _bitmapx.value = item
    }

    fun imageRect (image : ImageProxy){
        val cvBitmap = CameraUtil.JPGtoRGB888(CameraUtil.imageProxyToBitmap(image))
        val matCVT = Mat()
       Utils.bitmapToMat(cvBitmap,matCVT)
        //Imgproc.resize(matCVT,matCVT, Size(700.0,900.0))
        val rectanglesfromImage = CameraUtil.dominoArray(matCVT)
        // convert to mat, draw rectangles then convert to bitmap
        _bitmapx.value = CameraUtil.PutptsRectsonImage(rectanglesfromImage,matCVT)
    }
    fun setPlayer(strg: String){
        _playerName.value = strg
        Log.i("view","player ${_playerName}")
    }


}







