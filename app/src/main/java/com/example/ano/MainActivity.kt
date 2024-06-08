package com.example.ano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ano.dataSource.DataSource
import com.example.compose.AnoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataSource.loadJSONFromRaw(this@MainActivity, R.raw.donnees6)
                    AnoApp()


                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Sauvegarde de l'UIState lors de la fermeture de l'application
        SharedPreferencesManager.saveFavorites(this)
        SharedPreferencesManager.saveHistoryListAndFavoriteList(this)
        SharedPreferencesManager.savePackages(this)
    }
    override fun onStop(){
        super.onStop()
        SharedPreferencesManager.saveFavorites(this)
        SharedPreferencesManager.saveHistoryListAndFavoriteList(this)
        SharedPreferencesManager.savePackages(this)

    }
}
