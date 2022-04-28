package com.delafleur.mxt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.delafleur.mxt.databinding.ActivityMainBinding
/*   Modeled after the android lesson - 'Sleep Tracker'
* all that main activity holds is the nav controller setup.
* Read online that with androidx you can pass the layout through AppcompatAtivity and not
* have to run call override fun onCreate
*/
/*class MainActivity : AppCompatActivity(R.layout.activity_main) {*/

 /*   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

  */
 class MainActivity : AppCompatActivity() {
     private lateinit var drawerLayout: DrawerLayout
     private lateinit var binding: ActivityMainBinding
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)
         drawerLayout = binding.drawerLayout
         val navController = (
                 supportFragmentManager.findFragmentById(R.id.myNavHostFragment)
                         as NavHostFragment
                 ).navController
         NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
         NavigationUI.setupWithNavController(binding.navView, navController)
     }

     override fun onSupportNavigateUp(): Boolean {
         val navController = this.findNavController(R.id.myNavHostFragment)
         return NavigationUI.navigateUp(navController, drawerLayout)
     }

    companion object {
        init { System.loadLibrary("opencv_java4") }
    }

}