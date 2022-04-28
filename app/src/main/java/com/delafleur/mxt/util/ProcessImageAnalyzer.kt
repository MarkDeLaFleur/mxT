package com.delafleur.mxt.util

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import com.delafleur.mxt.CameraUtil.blobParamsInit
import com.delafleur.mxt.CameraUtil.fixMatRotation
import com.delafleur.mxt.CameraUtil.getMatFromImage
import com.delafleur.mxt.CameraUtil.keypointDetector
import com.delafleur.mxt.data.Params
import kotlinx.coroutines.flow.StateFlow
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.Features2d
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.features2d.SimpleBlobDetector_Params
import org.opencv.imgproc.Imgproc
import java.security.Key

class ProcessImageAnalyzer (
    val runOnUiThread: (Bitmap) -> Unit,
    val previewView: PreviewView?,
    val params: StateFlow<Params>

) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val matOrg: Mat = getMatFromImage(image)
        val mat: Mat = fixMatRotation(matOrg, previewView)
        val matMask = Mat(mat.rows(), mat.cols(), 0)
        val matTemp = Mat(mat.rows(), mat.cols(), mat.type())
        /*val matOutput = Mat(mat.rows(), mat.cols(), mat.type()) */
        var matOutput = Mat(matOrg.rows(),matOrg.cols(),matOrg.type())

        when (val params = params.value) {
             is Params.BlobParam -> {

                val keyPoints: MatOfKeyPoint =  keypointDetector(mat)

                val z: List<KeyPoint> = keyPoints.toList().sortedBy { it -> it.pt.x }
                 /*keyPoints.forEach{Log.i("kp","x,y = ${it.pt.x}"+
                          ",${it.pt.y}")} */
                if (z.size > 50) {
                    params.nuts = z.size.toString()
                    Log.i("kp", "keyPoints ${z.size}")

                    Features2d.drawKeypoints(
                        mat,
                        keyPoints,
                        matOutput,
                        Scalar(
                           100.toDouble(), 100.toDouble(),
                           100.toDouble()
                        ), Features2d.DrawMatchesFlags_DEFAULT
                    )
                    val pt1: Point = Point(10.toDouble(),10.toDouble())
                    val pt2: Point = Point(300.toDouble(),300.toDouble())
                    Imgproc.rectangle(matOutput,pt1,pt2,Scalar(255.toDouble(),255.toDouble(),
                    ),3,0)


                }
            }

        }

        val bitmap: Bitmap =
            Bitmap.createBitmap(matOutput.cols(), matOutput.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matOutput, bitmap)
        runOnUiThread(bitmap)
        image.close()
    }
}