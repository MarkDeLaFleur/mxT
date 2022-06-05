package com.delafleur.mxt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import org.opencv.core.Mat

typealias DominoListener = (dominoImg: Mat) -> Unit
 class MainActivity : AppCompatActivity(R.layout.activity_main) {
     private lateinit var navController: NavController
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
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