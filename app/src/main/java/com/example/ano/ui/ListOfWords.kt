package com.example.ano.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfWordsScreen(
    isFavorite : (String)-> Boolean,
    onWordClicked :(String)->Unit,
    words: List<String>?,
    onFavoriteButtonClicked: (String) -> Unit,
    idIcon1: Int,
    idIcon2: Int,
    @DrawableRes idImage : Int,
    modifier: Modifier = Modifier
){  if(words==null){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painterResource(id = idImage),
                contentDescription =null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    else {
        var listOfWords : List<String>
        //liste de mots par ordre d'ajouts à l'historique
        val wordsChrono = words
        var selectedIndex by remember { mutableIntStateOf(0) }
        val options = listOf(
            "Chronologique",
            "A  ->  Z",
        )
        Column {
            SingleChoiceSegmentedButtonRow(
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = { selectedIndex = index },
                        selected = index == selectedIndex,
                        icon = { SegmentedButtonDefaults.Icon(active = false)}
                    ) {
                        Text(label)
                    }
                }
            }
            if(selectedIndex==0){
                listOfWords=wordsChrono
            }
            else{
                listOfWords=words.sorted()
            }
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(listOfWords ?: emptyList()) { item ->
                    Element(
                        isFavorite,
                        item,
                        onFavoriteButtonClicked = onFavoriteButtonClicked,
                        idIcon1 = idIcon1,
                        idIcon2 = idIcon2,
                        modifier = modifier.padding(8.dp),
                        onWordClicked = onWordClicked
                    )

                }
            }
        }


    }
}

@Composable
fun Element(
    isFavorite: (String) ->Boolean,
    word : String,
    onWordClicked :(String)->Unit,
    onFavoriteButtonClicked : (String)->Unit,
    @DrawableRes idIcon1 : Int,
    @DrawableRes idIcon2: Int,
    modifier : Modifier = Modifier
){
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,

        ){
            Button(
                onClick = { onWordClicked(word) },
                shape= MaterialTheme.shapes.small
            ) {
                Text(
                    text = word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onFavoriteButtonClicked(word)}) {
                    Icon(
                        painter = painterResource(id = if(isFavorite(word)) idIcon2 else idIcon1),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

