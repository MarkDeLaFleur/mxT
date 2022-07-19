package com.delafleur.mxt.util

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
import com.delafleur.mxt.util.CameraUtil.blobParamsInit
import com.delafleur.mxt.data.MySubmatDomino

import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Core.bitwise_not
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.features2d.SimpleBlobDetector_Params
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Moments
import java.nio.ByteBuffer


private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA",
                             "android.permission.WRITE_EXTERNAL_STORAGE")
val colorRed = Scalar(255.0,0.0,0.0,255.0)
val colorBlue = Scalar(0.0,0.0,255.0,255.0)
val colorBlue2 = Scalar(155.0,0.0,0.0,255.0)
val colorGreen = Scalar(0.0,255.0,0.0,255.0)
val colorBlack = Scalar(50.0,50.0,0.0,255.0)
val colorWhite = Scalar(0.0,0.0,0.0,255.0)
private val blobparms = blobParamsInit()
private val detector: SimpleBlobDetector = SimpleBlobDetector.create(blobparms)
object CameraUtil {
    class CameraApplication: Application(),CameraXConfig.Provider {
        override fun getCameraXConfig(): CameraXConfig {
            return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .setMinimumLoggingLevel(Log.WARN)
                .build()
        }
    }
    /*commented out because this is for imageproxy from analysis and I'm using capture use case
    which is formmated as jpeg
    fun getMatFromImage(image: ImageProxy): Mat {
        val yBuffer: ByteBuffer = image.planes[0].buffer
        val uBuffer: ByteBuffer = image.planes[1].buffer
        val vBuffer: ByteBuffer = image.planes[2].buffer
        val ySize: Int = yBuffer.remaining()
        val uSize: Int = uBuffer.remaining()
        val vSize: Int = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuv = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC3)
        yuv.put(0, 0, nv21)
        val mat = Mat()
        Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGB_NV21, 3)
        return mat
    }
    */
    fun fixMatRotation(matOrg: Mat, previewView: PreviewView?): Mat {
        val mat : Mat

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
        blobParms._filterByInertia =   true
        blobParms._maxArea =  5000.0F
        blobParms._maxConvexity = 3.4028234663852886e+38F
        blobParms._minCircularity = 0.800000011920929F
        blobParms._maxInertiaRatio = 3.4028234663852886e+38F
        blobParms._minArea = 20F
        blobParms._minConvexity = 0.800000011920929F
        blobParms._minDistBetweenBlobs = 05.0F
        blobParms._minInertiaRatio = 0.10000000149011612F
        blobParms._minRepeatability = 2
        blobParms._maxThreshold = 255.0F
        blobParms._minThreshold = 155.0F
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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),2000)
    }
    fun keypointDetector(mat: Mat): MatOfKeyPoint {
        val keypts = MatOfKeyPoint()
        detector.detect(mat,keypts)
        return keypts
    }

    fun dominoArrayofMat(imgIn: Mat) :MySubmatDomino {
        val grey = Mat()
        val thresh = Mat()
        val dominoBitmaps = ArrayList<Bitmap>()
        val contours: List<MatOfPoint> = ArrayList()
        val contour2f = MatOfPoint2f()
        var peri: Double
        val poly = MatOfPoint2f()
        var rectWrk :Rect
        var wrkMat :Mat
        val ptsOutList = ArrayList<Point>()
        val dominoMats = ArrayList<Mat>()

        Imgproc.cvtColor(imgIn, grey,Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(grey, thresh, 155.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.findContours(thresh,contours,Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE)

        contours.forEach {
            it.convertTo(contour2f, CvType.CV_32FC2)
            peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, poly, 0.1 * peri, true)
            rectWrk = Imgproc.boundingRect(poly)
            wrkMat = Mat(imgIn, rectWrk)
            val chkPts = PointsfromCroppedImage(wrkMat)
            if (chkPts.x + chkPts.y > 0) {
                ptsOutList.add(chkPts)
                // take the rectWrk.tl and add a few rows
                val ff = Point(rectWrk.tl().x+30,rectWrk.tl().y+10)
                Imgproc.rectangle(imgIn, rectWrk.tl(),rectWrk.br(), colorRed, 2,Imgproc.LINE_4 )
                Imgproc.putText(imgIn,ptsOutList.size.toString(),ff,Imgproc.FONT_HERSHEY_SIMPLEX,
                0.8, colorBlack,2)
                //org.opencv.features2d.Features2d.drawKeypoints(imgIn,chkPts,imgIn,
                //    colorBlue,4)
                dominoMats.add(wrkMat)
            }
        }
            /*dominoMats.add(imgIn)
            dominoMats.sortBy { it.rows() }
                dominoMats.sortByDescending { it.cols() }

                dominoMats.forEach {

                 //   Imgproc.resize(it,it,Size(50.0,100.0))
                    wrkPts = PointsfromCroppedImage(it)
                    val z = wrkPts.x + wrkPts.y
                    if (z > 2) {
                        Log.i("Points"," rows cols ${wrkPts} ${it.rows()} ${it.cols()}")
                        ptsOutList.add(wrkPts)
                        val tempIt = putNumbersOnCrops(it, wrkPts)
                        val wrkBitmap =
                            Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
                        Utils.matToBitmap(tempIt, wrkBitmap)
                        dominoBitmaps.add(wrkBitmap)
                    }
                }
*/
        val wrkBitmap = Bitmap.createBitmap(imgIn.cols(),imgIn.rows(),Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imgIn,wrkBitmap)
        dominoBitmaps.add(wrkBitmap)



        Log.i("bitmaps","passing ${dominoBitmaps.size} back from dominoArrayofMat method")
        return  MySubmatDomino(pts = ptsOutList,bitmapImgs = dominoBitmaps)
    }

    fun PointsfromCroppedImage(cropImage: Mat) :Point{
        val ptsOut = Point(0.0,0.0)
        if (cropImage.rows() < 20) {
            return ptsOut
        }
        Log.i("ptsD", "cropImage rows >  20")

        if (cropImage.rows() < cropImage.cols()) Core.rotate(
            cropImage,cropImage,Core.ROTATE_90_CLOCKWISE)

        val sRow = cropImage.rows()
        val sCol = cropImage.cols()

        ptsOut.x = (keypointDetector(cropImage.submat(0,
            sRow/2,
            0, sCol)).toList().size).toDouble()
        ptsOut.y = (keypointDetector(cropImage.submat(sRow/2,
            sRow,0,sCol)).toList().size).toDouble()
        return ptsOut
    }
     fun putNumbersOnCrops(wrkM: Mat, ptsIn :Point): Mat {
        val grey = Mat()
        Log.i("numbersonDomino","wrkMat size is ${wrkM.width()}" +
                "/${wrkM.height()}")
         Log.i("numbersonDomino","image rows / cols /width/height ${wrkM.rows()}" +
                 " ${wrkM.cols()} ${wrkM.width()} ${wrkM.height()}")
        //middle is cols / 2 rows /2
        //Imgproc.cvtColor(wrkM, grey,Imgproc.COLOR_BGR2GRAY)
         //  val center = Point(0.0,0.0)
         //  center.x = (mu.m10 / mu.m00)
         //  center.y = (mu.m01 /mu.m00)
         //  val centerH = Point((center.x - center.x/2) , (center.y - center.y/2))
       // val centerL = Point((center.x - center.x/2), (center.y + center.y))
     Imgproc.putText(wrkM,ptsIn.x.toInt().toString(),
       Point(10.0,40.0),
         Imgproc.FONT_HERSHEY_SIMPLEX,
         1.0, colorRed,2,1,false)

         Imgproc.putText(wrkM,ptsIn.y.toInt().toString(),
             Point(10.0,80.0),
             Imgproc.FONT_HERSHEY_SIMPLEX,
             1.0, colorBlack,2,1,false)

         /*   Imgproc.putText(wrkM,ptsIn.x.toInt().toString(),
                       centerH, Imgproc.FONT_HERSHEY_SIMPLEX,1.0, colorGreen,
                2,1,false)
            Imgproc.putText(wrkM,ptsIn.y.toInt().toString(),
                       centerL, Imgproc.FONT_HERSHEY_SIMPLEX,1.0, colorRed,
                2,1,false)
         */
        return wrkM
    }
    fun Fragment.runOnUiThread(action: () -> Unit) {
        if (!isAdded) return
        activity?.runOnUiThread(action)
    }
    }
