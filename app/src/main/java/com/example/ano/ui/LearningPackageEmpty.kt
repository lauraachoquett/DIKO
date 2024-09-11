package com.example.ano.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPackageEmpty(delay : Long){
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()

    ) {
        var delayInSec = delay/1000L
        var dayDelay = Math.round((delayInSec/(3600*24L)).toDouble())
        var hourDelay = Math.round(((delayInSec - dayDelay*3600*24)/(3600L)).toDouble())
        var minuteDelay = Math.round((delayInSec - dayDelay*3600*24 - hourDelay*3600)/(60L).toDouble())
        Spacer(Modifier.weight(7f))
        Box(
            Modifier
                .weight(10f)
                .fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Vous avez révisé toutes les cartes de ce paquet" ,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Prochain mot dans $dayDelay j, $hourDelay h, $minuteDelay m" ,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )

            }
        }

    }



}





