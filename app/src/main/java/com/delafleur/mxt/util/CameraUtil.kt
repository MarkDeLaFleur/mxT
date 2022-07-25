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
import org.opencv.features2d.Features2d
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.features2d.SimpleBlobDetector_Params
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Moments
import java.nio.ByteBuffer
import kotlin.system.exitProcess


private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA",
                             "android.permission.WRITE_EXTERNAL_STORAGE")
val colorRed = Scalar(255.0,0.0,0.0,255.0)
val colorBlue = Scalar(0.0,0.0,255.0,255.0)
val colorBlue2 = Scalar(155.0,0.0,0.0,255.0)
val colorGreen = Scalar(0.0,255.0,0.0,255.0)
val colorBlack = Scalar(50.0,50.0,0.0,255.0)
val colorYellow = Scalar(255.0,180.0,0.0,255.0)
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
        blobParms._minArea = 25F
        blobParms._minConvexity = 0.800000011920929F
        blobParms._minDistBetweenBlobs = 10.0F
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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),2000)
    }
    fun keypointDetector(mat: Mat): MatOfKeyPoint {
        val keypts = MatOfKeyPoint()
        detector.detect(mat,keypts)
        return keypts
    }
    fun blobs(image: Mat):MySubmatDomino {
        val arrayOfKeyPts = keypointDetector(image)
        val listOfKeyPts  = arrayOfKeyPts.toList()
        listOfKeyPts.sortBy { it.pt.y }
        listOfKeyPts.sortBy { it.pt.x }
        Features2d.drawKeypoints(
            image, arrayOfKeyPts, image,colorYellow,
            Features2d.DrawMatchesFlags_DEFAULT)
        val arrayOfRects = findRectangles(image)
        val ptsByRec: ArrayList<Point> = ArrayList()
        arrayOfRects.forEach {
          ptsByRec.add(Point(0.0,0.0))  //x is a side y is b side
          listOfKeyPts.forEach { kPt ->
              val pts = findKptsInRect(kPt,it)
              ptsByRec[ptsByRec.size-1].x += pts.x
              ptsByRec[ptsByRec.size-1].y += pts.y
          }

        }
        // got all the points now stored in ptsByRec
        var finalPts  : ArrayList<Point> = ArrayList()

        ptsByRec.forEachIndexed { ins,it ->
            if (it.x + it.y > 0.0) {
                Imgproc.rectangle(image, arrayOfRects[ins].tl(),
                    arrayOfRects[ins].br(), colorYellow, 2, Imgproc.LINE_8)
                val ff = Point(arrayOfRects[ins].tl().x + arrayOfRects[ins].width/2,
                    arrayOfRects[ins].tl().y + arrayOfRects[ins].height/2)
                finalPts.add(Point(it.x,it.y))

                Imgproc.putText(image,finalPts.size.toString(),ff,Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.2, colorRed,2)
            }

        }
        val wrkBitmap = Bitmap.createBitmap(image.cols(),image.rows(),Bitmap.Config.ARGB_8888)
        val dominoBitmaps = ArrayList<Bitmap>()
        Utils.matToBitmap(image,wrkBitmap)
        dominoBitmaps.add(wrkBitmap)
        return  MySubmatDomino(pts = finalPts,bitmapImgs = dominoBitmaps)


    }
   fun findKptsInRect(kPt: KeyPoint,rctPt: Rect) :Point{
       //kPt is the keypoint we're looking for in the rectangle
       // note: check out the method keypoint.pt.inside . I couldn't find an doco on it but
       //       it simplifies finding a point in a rectangle.
       var rctPtB : Rect = Rect()
       var rctPtA : Rect = Rect()
       var outVal : Point = Point(0.0,0.0)
       if(rctPt.width > rctPt.height) {
            rctPtB = Rect(rctPt.x+rctPt.width/2,rctPt.y,rctPt.width/2,rctPt.height)
           Log.i("showRctB","rctPtB is ${rctPtB} vs rctPt ${rctPt}")
            rctPtA = Rect(rctPt.x, rctPt.y, rctPt.width / 2, rctPt.height)
       }else
       {
           rctPtB = Rect(rctPt.x,rctPt.y+rctPt.height/2,rctPt.width,rctPt.height/2)
           rctPtA = Rect(rctPt.x, rctPt.y, rctPt.width , rctPt.height/2)
       }
       if(kPt.pt.inside(rctPtB))  outVal = Point(0.0,1.0)
       if (kPt.pt.inside(rctPtA)) outVal = Point(1.0,0.0)
       return outVal
    }
    fun findRectangles(imgin: Mat) :List<Rect> {
        val wrkGrey = Mat()
        val contours: List<MatOfPoint> = ArrayList()
        val wrkRects: ArrayList<Rect> = ArrayList()
        val contour2f = MatOfPoint2f()
        var peri: Double
        val poly = MatOfPoint2f()
        Imgproc.cvtColor(imgin, wrkGrey, Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(wrkGrey, wrkGrey, 155.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.findContours(
            wrkGrey,
            contours,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE  )
        contours.forEach {
            it.convertTo(contour2f, CvType.CV_32FC2)
            peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, poly, 0.1 * peri, true)
            wrkRects.add(Imgproc.boundingRect(poly))
        }
        return wrkRects.sortedBy { it.tl().x }
    }
    fun Fragment.runOnUiThread(action: () -> Unit) {
        if (!isAdded) return
        activity?.runOnUiThread(action)
    }
}
