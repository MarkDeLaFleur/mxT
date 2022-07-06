package com.delafleur.mxt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.delafleur.mxt.CameraUtil.checkPermissions
import com.delafleur.mxt.CameraUtil.userRequestPermissions
import org.opencv.core.Mat


 class MainActivity : AppCompatActivity(R.layout.activity_main) {
     private lateinit var navController: NavController
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         if(!checkPermissions(this)) userRequestPermissions((this))

         val navController = (
                 supportFragmentManager.findFragmentById(R.id.myNavHostFragment)
                         as NavHostFragment
                 ).navController
            setupActionBarWithNavController(navController)

     }

     override fun onSupportNavigateUp(): Boolean {
         navController =(supportFragmentManager.findFragmentById(R.id.myNavHostFragment)
                         as NavHostFragment).navController
         return navController.navigateUp() || super.onSupportNavigateUp()
     }

     companion object {
        init { System.loadLibrary("opencv_java4") }
    }

}