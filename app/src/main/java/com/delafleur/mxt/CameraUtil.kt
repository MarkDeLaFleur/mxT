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
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.Features2d
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.features2d.SimpleBlobDetector_Params
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Moments
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

    fun dominoArrayofMat(imgIn: Mat) :ArrayList<Mat>{
        val grey = Mat()
        val thresh = Mat()
        val dominoRect = ArrayList<Mat>()
        val contours: List<MatOfPoint> = ArrayList()
        val contour2f = MatOfPoint2f()
        var peri: Double
        val poly = MatOfPoint2f()
        var rectWrk :Rect
        var wrkMat :Mat
        Imgproc.cvtColor(imgIn, grey,Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(grey, thresh, 155.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.findContours(thresh,contours,Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE)
        contours.forEach { it ->
            it.convertTo(contour2f, CvType.CV_32FC2)
            peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, poly, 0.1 * peri, true)
            rectWrk = Imgproc.boundingRect(poly)
            if((rectWrk.width < rectWrk.height && rectWrk.width > 90) ||
                (rectWrk.height < rectWrk.width && rectWrk.height > 90)) {
                wrkMat = Mat(imgIn, rectWrk)
                if (wrkMat.height() < wrkMat.width()) Core.rotate(wrkMat,wrkMat,Core.ROTATE_90_CLOCKWISE)

                Imgproc.resize(wrkMat, wrkMat, Size(75.0, 150.0))
                dominoRect.add(wrkMat)
                Log.i(
                    "Mat", "Mat size is ${dominoRect[dominoRect.size - 1].width()}" +
                            "by ${dominoRect[dominoRect.size - 1].height()}"
                )
            }
        }
            dominoRect.sortedWith(compareByDescending { it.rows()})  //first on row, then on col
            dominoRect.sortedWith(compareByDescending { it.cols() })

        return dominoRect

    }

    fun dominoArray(imgIn: Mat) :  ArrayList<Rect> {
        val grey = Mat()
        val thresh = Mat()
        val dominoRect = ArrayList<Rect>()
        val contours: List<MatOfPoint> = ArrayList()
        val contour2f = MatOfPoint2f()
        var peri: Double
        val poly = MatOfPoint2f()
        var rectWrk = Rect()
        Imgproc.cvtColor(imgIn, grey,Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(grey, thresh, 155.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.findContours(thresh,contours,Mat(), Imgproc.RETR_EXTERNAL,
                             Imgproc.CHAIN_APPROX_NONE)
        Log.i("rectangles","Image has rows,cols ${thresh.rows()},${thresh.cols()}")
        contours.forEach { it ->
            it.convertTo(contour2f, CvType.CV_32FC2)
            peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, poly, 0.1 * peri, true)
            rectWrk = Imgproc.boundingRect(poly)
            if((rectWrk.width < rectWrk.height && rectWrk.width > 90) ||
                (rectWrk.height < rectWrk.width && rectWrk.height > 90)) {
                  /*only adding rectangles with widths > 1 inch or height > 1 inch
                   * based on 96 pixels in an inch
                   * each domino has two halves, an 'a' and 'b' side, so we will modify
                   * width < height then domino is taller than wider
                   *  y,x ----- y,x+w
                   *  y+h/2,x ----- y+h/2,x+w  so change rectA's H to h/2
                   *  and change rectB's y to y+h/2
                   */

                //Log.i("rectangles","wrkA(x,y,w,h) ${rectWrk.x},${rectWrk.y},${rectWrk.width}"+
                //        ",${rectWrk.height} before"
                    // would be cool to get  array of mats using rect
                if (rectWrk.width < rectWrk.height) {
                    rectWrk.height = rectWrk.height / 2
                    //still need to test this so right now I'm dropping these rectangles
                    //Log.i("rectangles","width less than height")
                }
                else {
                    if (rectWrk.width > rectWrk.height) {
                        val splitW = rectWrk.width/2
                        rectWrk.width = splitW

                        dominoRect.add(rectWrk) // 'a' side
                        //Log.i(
                          //  "rectangles",
                          //  "wrk_(x,y,w,h) ${rectWrk.x},${rectWrk.y},${rectWrk.width}" +
                          //          ",${rectWrk.height} after")
                        rectWrk = Imgproc.boundingRect(poly)
                        rectWrk.x = (rectWrk.x + splitW)
                        rectWrk.width = splitW
                        dominoRect.add(rectWrk) //'b' side
                        //Log.i(
                        //    "rectangles",
                        //    "wrk(x,y,w,h) ${rectWrk.x},${rectWrk.y},${rectWrk.width}" +
                        //            ",${rectWrk.height} after"
                        // )
                    }
                } //endof else

            }  //end of if
        }
        dominoRect.sortedWith(compareByDescending{ it.y})  //first on row, then on col
        dominoRect.sortedWith(compareByDescending { it.x })

    return dominoRect

    }
    fun PointsfromDomino(dominoRects: ArrayList<Rect>, imgIn: Mat) :ArrayList<Int>{
        val ptsOut = ArrayList<Int>()
        dominoRects.forEach{
            ptsOut += keypointDetector(imgIn.submat(it)).toList().size
            Log.i("pts","${it.tl()},  ${ptsOut[ptsOut.size-1]}")
        }
        return ptsOut
    }
    fun PointsfromCroppedImage(cropList: List<Mat>): ArrayList<Int> {
        val ptsOut = ArrayList<Int>()
        cropList.forEach {
            ptsOut += keypointDetector(it.submat(0, 75, 0, 75)).toList().size
            ptsOut += keypointDetector(it.submat(75, 150, 0, 75)).toList().size
        }
        return ptsOut
    }
    fun Fragment.runOnUiThread(action: () -> Unit) {
        if (!isAdded) return
        activity?.runOnUiThread(action)
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

    fun PutptsRectsonImage(rectanglesfromImage: ArrayList<Rect>,
                           pts: ArrayList<Int>,image: Mat) :Bitmap{
        val bitmapOut: Bitmap
        var totPoints = 0
        var kep: MatOfKeyPoint
        kep = keypointDetector(image.clone())
        Features2d.drawKeypoints(image,kep,image,Scalar(0.0, 135.0,195.0)
            , Features2d.DrawMatchesFlags_DRAW_OVER_OUTIMG)

        rectanglesfromImage.forEachIndexed { ind,it ->
            Imgproc.rectangle(image,it, Scalar(100.0, 255.0, 100.0,255.0), 2, 0)
            var nl = Point(it.tl().x+40,it.tl().y+ 45)
            Imgproc.putText(image,
                pts[ind].toString(),nl, Imgproc.FONT_HERSHEY_SIMPLEX,1.0,Scalar(0.0,255.0,0.0,155.0),3)
          }
        bitmapOut = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, bitmapOut)
        return bitmapOut
    }
    fun putNumbersOnCrops(matList: List<Mat>,ptsIn :List<Int>): List<Mat>{
        var matListOut = Array<Mat>(matList.size){Mat()}
        var top = Point(150.0,30.0)
        var bot = Point(30.0,150.0)
        val grey = Mat()
        if (ptsIn.size == matListOut.size * 2) {Log.i("numbers","images is twice numbers")
            var ptsI = 0
            matList.forEachIndexed {i,j -> matListOut[i] = j
                var center = Point(0.0,0.0)
                Imgproc.cvtColor(j, grey,Imgproc.COLOR_RGB2GRAY)
                val mu  = Imgproc.moments(grey,true)
                center.x = (mu.m10 / mu.m00) -5
                center.y = (mu.m01 /mu.m00)
                center.y.toInt()
                Imgproc.putText(matListOut[i],
                    ptsIn[ptsI].toString(),center, Imgproc.FONT_HERSHEY_SIMPLEX,1.0,Scalar(0.0,255.0,155.0,255.0),2)
                Imgproc.putText(matListOut[i],
                    ptsIn[ptsI+1].toString(),bot, Imgproc.FONT_HERSHEY_SIMPLEX,1.0,Scalar(0.0,255.0,155.0,255.0),2)
               ptsI = ptsI+2
            }
        }
        return matListOut.toList()
    }

}
