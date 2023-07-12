package com.delafleur.mxt


import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import org.opencv.android.OpenCVLoader

 class MainActivity : AppCompatActivity(R.layout.activity_main) {
     private lateinit var navController: NavController
     private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA",
         "android.permission.WRITE_EXTERNAL_STORAGE")


     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         if (!OpenCVLoader.initDebug()) Log.e("OpenCV", "Unable to load OpenCV!")
         else   Log.d("OpenCV", "OpenCV loaded Successfully!")
         if(!checkPermissions(this)) userRequestPermissions(this)
         navController = (supportFragmentManager.findFragmentById(R.id.myNavHostFragment)
                 as NavHostFragment).navController
         setupActionBarWithNavController(navController)


     }

     override fun onSupportNavigateUp(): Boolean {
         navController =(supportFragmentManager.findFragmentById(R.id.myNavHostFragment)
                         as NavHostFragment).navController
         return navController.navigateUp() || super.onSupportNavigateUp()
     }
     private fun checkPermissions(context: Context): Boolean {
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

     private fun userRequestPermissions(activity: Activity) {
         ActivityCompat.requestPermissions(
             activity,REQUIRED_PERMISSIONS,2000)
         }


}