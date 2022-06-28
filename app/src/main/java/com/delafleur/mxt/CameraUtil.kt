package com.delafleur.mxt

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.delafleur.mxt.CameraUtil.blobParamsInit
import com.delafleur.mxt.data.MySubmatDomino
import org.opencv.core.*
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.features2d.SimpleBlobDetector_Params
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer


private val REQUIRED_PERMISSIONS = arrayOf(
    "android.permission.CAMERA",
    "android.permission.WRITE_EXTERNAL_STORAGE"
)
private val blobparms = blobParamsInit()
private val detector: SimpleBlobDetector = SimpleBlobDetector.create(blobparms)
object CameraUtil {
    class CameraApplication: Application(),CameraXConfig.Provider {
        override fun getCameraXConfig(): CameraXConfig {
            return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .setMinimumLoggingLevel(Log.INFO).build()
        }
    }
    //fun getMatFromImage(image: ImageProxy): Mat {
    //    val yBuffer: ByteBuffer = image.planes[0].buffer
    //    val uBuffer: ByteBuffer = image.planes[1].buffer
    //    val vBuffer: ByteBuffer = image.planes[2].buffer
    //    val ySize: Int = yBuffer.remaining()
    //    val uSize: Int = uBuffer.remaining()
    //    val vSize: Int = vBuffer.remaining()
    //    val nv21 = ByteArray(ySize + uSize + vSize)
    //    yBuffer.get(nv21, 0, ySize)
    //    vBuffer.get(nv21, ySize, vSize)
    //    uBuffer.get(nv21, ySize + vSize, uSize)
    //    val yuv = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC3)
    //    yuv.put(0, 0, nv21)
    //    val mat = Mat()
    //    Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGB_NV21, 3)
    //    return mat
    //}

    fun fixMatRotation(matOrg: Mat, previewView: PreviewView?): Mat {
        val mat: Mat
        when (previewView?.display?.rotation) {
            Surface.ROTATION_0 -> {
                mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, mat)
                Core.flip(mat, mat, 1)
            }
            Surface.ROTATION_90 -> mat = matOrg
            Surface.ROTATION_270 -> {
                mat = matOrg
                Core.flip(mat, mat, -1)
            }
            else -> {
                mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, mat)
                Core.flip(mat, mat, 1)
            }
        }
        return mat
    }
    fun JPGtoRGB888(img: Bitmap): Bitmap? {
        var result: Bitmap? = null
        val numPixels = img.width * img.height
        val pixels = IntArray(numPixels)
        // get jpeg pixels, each int is the color value of one pixel
        img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)
        // create bitmap in appropriate format either ARGB_8888 or RGB_565
        result = Bitmap.createBitmap(img.width, img.height,  Bitmap.Config.ARGB_8888)
        // Set RGB pixels
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    fun blobParamsInit ():SimpleBlobDetector_Params{
        val blobParms = SimpleBlobDetector_Params()
        blobParms._filterByArea = true
        blobParms._filterByCircularity = false
        blobParms._filterByColor = true
        blobParms._filterByConvexity = true
        blobParms._filterByInertia = true
        blobParms._maxArea = 5000.0F
        blobParms._maxConvexity = 3.4028234663852886e+38F
        blobParms._minCircularity = 0.800000011920929F
        blobParms._maxInertiaRatio = 3.4028234663852886e+38F
        blobParms._minArea = 25F
        blobParms._minConvexity = 0.800000011920929F
        blobParms._minDistBetweenBlobs =  10.0F
        blobParms._minInertiaRatio = 0.10000000149011612F
        blobParms._minRepeatability = 2
        blobParms._maxThreshold = 200.0F
        blobParms._minThreshold = 100.0F
        blobParms._thresholdStep = 10.0F
        return blobParms
    }
    fun checkPermissions(context: Context): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (context.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        permission
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun userRequestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
            2000
        )
    }
    fun keypointDetector(mat: Mat): MatOfKeyPoint {
        val keypts = MatOfKeyPoint()
        detector.detect(mat,keypts)
        return keypts
    }

    fun dominoArrayofMat(imgIn: Mat) :MySubmatDomino {//:ArrayList<Mat>{
        val grey = Mat()
        val thresh = Mat()
        val dominoRect = ArrayList<Mat>()
        val contours: List<MatOfPoint> = ArrayList()
        val contour2f = MatOfPoint2f()
        var peri: Double
        val poly = MatOfPoint2f()
        var rectWrk :Rect
        var wrkMat :Mat
        var ptsOutList = ArrayList<Point>()
        Imgproc.cvtColor(imgIn, grey,Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(grey, thresh, 155.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.findContours(thresh,contours,Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE)
        contours.forEach { it ->
            it.convertTo(contour2f, CvType.CV_32FC2)
            peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, poly, 0.1 * peri, true)
            rectWrk = Imgproc.boundingRect(poly)
            if((rectWrk.width < rectWrk.height && rectWrk.width > 90) ||
               (rectWrk.height < rectWrk.width && rectWrk.height > 90)) {
                wrkMat = Mat(imgIn, rectWrk)   //cropping image
                if (wrkMat.height() < wrkMat.width()) {
                    Core.rotate(wrkMat,wrkMat,Core.ROTATE_90_CLOCKWISE)}
                Imgproc.resize(wrkMat, wrkMat, Size(75.0, 150.0))
                //count the dominos
                ptsOutList.add( PointsfromCroppedImage(wrkMat))
                dominoRect.add(wrkMat)  //cut it down to fixed
                Log.i(
                    "Mat", "Mat size is ${dominoRect[dominoRect.size - 1].width()}" +
                            "by ${dominoRect[dominoRect.size - 1].height()}"
                )
            }
        }
            dominoRect.sortedWith(compareByDescending { it.rows()})  //first on row, then on col
            dominoRect.sortedWith(compareByDescending { it.cols() })

        return  MySubmatDomino(pts = ptsOutList, cropImg = dominoRect)//dominoRect

    }

    fun PointsfromCroppedImage(cropImage: Mat) :Point{
         val ptsOut = Point(0.0,0.0)
         ptsOut.x = (keypointDetector(cropImage.submat(0, 75, 0, 75)).toList().size).toDouble()
        ptsOut.y  = (keypointDetector(cropImage.submat(75, 150, 0, 75)).toList().size).toDouble()
        return ptsOut
    }
    fun Fragment.runOnUiThread(action: () -> Unit) {
        if (!isAdded) return
        activity?.runOnUiThread(action)
    }


    fun putNumbersOnCrops(matList: List<Mat>, ptsIn :List<Point>): List<Mat>{
        var matListOut = Array<Mat>(matList.size){Mat()}
        var top = Point(150.0,30.0)
        var bot = Point(30.0,150.0)
        val grey = Mat()
        Imgproc.cvtColor(matList[0], grey,Imgproc.COLOR_RGB2GRAY)
        val mu  = Imgproc.moments(grey,true)
        var center = Point(0.0,0.0)
        center.x = (mu.m10 / mu.m00) - 15.0
        center.y = (mu.m01 /mu.m00)
        val centerH = Point((center.x - 15.0), center.y)
        val centerL = Point((center.x + 15.0), center.y)
        matList.forEachIndexed {i,j -> matListOut[i] = j
               Imgproc.putText(matListOut[i],ptsIn[i].x.toInt().toString(),
                   centerH, Imgproc.FONT_HERSHEY_SIMPLEX,1.0,Scalar(0.0,255.0,155.0,255.0),2)
                Imgproc.putText(matListOut[i],ptsIn[i].y.toInt().toString(),
                    centerL, Imgproc.FONT_HERSHEY_SIMPLEX,1.0,Scalar(0.0,255.0,165.0,255.0),2)
        }
        return matListOut.toList()
    }

}
