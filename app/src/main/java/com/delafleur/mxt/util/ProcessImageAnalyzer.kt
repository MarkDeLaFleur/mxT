package com.delafleur.mxt.util

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.camera2.internal.compat.ApiCompat.Api21Impl.close
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.ImageProxyBundle
import androidx.camera.core.internal.compat.ImageWriterCompat.close
import androidx.camera.view.PreviewView
import androidx.compose.runtime.key
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.CameraUtil.blobParamsInit
import com.delafleur.mxt.CameraUtil.dominoArray
import com.delafleur.mxt.CameraUtil.fixMatRotation
import com.delafleur.mxt.CameraUtil.getMatFromImage
import com.delafleur.mxt.CameraUtil.keypointDetector
import com.delafleur.mxt.DominoListener
import com.delafleur.mxt.data.Params
import kotlinx.coroutines.flow.StateFlow
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.Features2d
import org.opencv.imgproc.Imgproc
import java.security.Key


class ProcessImageAnalyzer (

   // val runOnUiThread: (Bitmap) -> Unit,
    val previewView: PreviewView?,
   // val params: StateFlow<Params>,
    private var listener: DominoListener

) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val matOrg: Mat = getMatFromImage(image)
        val mat = fixMatRotation(matOrg, previewView)

        /*val matOutput = Mat(mat.rows(), mat.cols(), mat.type()) */
        //var matOutput = Mat(matOrg.rows(), matOrg.cols(), matOrg.type())
        //when (val params = params.value) {
        //   is Params.BlobParam -> {

        //      val dominoList = dominoArray(mat.clone())


        // var kePointsList =  keyPoints.toList().sortedBy { it -> it.pt.x }


        //    }
        //    else -> {}
        //}

/*        val bitmap: Bitmap =
            Bitmap.createBitmap(matOutput.cols(), matOutput.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matOutput, bitmap)
        runOnUiThread(bitmap)

 */

        val body = dominoArray(mat.clone()).toList().size
        if(body < 15 && body > 0) {
            Log.i("analyzer","body count is ${body}")
            listener(mat)
            Log.i("after","${body}")
        }
        image.close()

    }
    }
