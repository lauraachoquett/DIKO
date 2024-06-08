package com.example.ano.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ano.R

@Composable
fun HistoryScreen(
    isFavorite : (String) ->Boolean,
    onWordClicked :(String)->Unit,
    words : List<String>?,
    onFavoriteButtonClicked :(String)->Unit,
    modifier : Modifier = Modifier,
){
    ListOfWordsScreen(
        isFavorite=isFavorite,
        words = words ,
        onFavoriteButtonClicked = onFavoriteButtonClicked,
        idIcon1 = R.drawable.round_check_circle_outline_24,
        idIcon2 = R.drawable.round_check_circle_24,
        onWordClicked = onWordClicked,
        idImage = R.drawable.ano_history)
}