package com.example.ano.ui

import android.app.Activity
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ano.R
import com.example.ano.dataSource.paquetAttributes
import com.example.ano.model.AnoAnki.ReviewReceiver.Companion.reviewQueueMap
import java.util.Locale


@Composable
fun HomePageScreen(
    onDictionaryButtonClicked : ()->Unit,
    onFavoriteButtonClicked : ()->Unit,
    onHistoryButtonClicked : ()->Unit,
    onPackageButtonClicked : ()->Unit,
    modifier : Modifier = Modifier,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.ano_loge),
            contentDescription =null,
            contentScale= ContentScale.Crop,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .width(240.dp)
                .clip(RoundedCornerShape(8.dp))

        )
        CustomizedButton(
            idText = R.string.dictionary,
            onClick = onDictionaryButtonClicked ,
            idIcon = R.drawable.round_search_24,)
        CustomizedButton(
            idText = R.string.favorites,
            onClick =  onFavoriteButtonClicked ,
            idIcon = R.drawable.round_star_24,)
        CustomizedButton(
            idText = R.string.histrory,
            onClick =  onHistoryButtonClicked ,
            idIcon = R.drawable.round_history_24,)
        CustomizedButton(
            idText = R.string.paquet,
            onClick =  onPackageButtonClicked ,
            idIcon = R.drawable.round_menu_book_24,)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScreenDesign(
    totalReviewCount : Int,
    totalLearningCount : Int,
    searchWord : String,
    onUserSearchedChanged: (String) -> Unit,
    onKeyboardSearch: () -> Unit,
    paquets :Map<Int, paquetAttributes>,
    onPackageClicked: (Int) -> Unit,
    onWordClicked :(String)->Unit,
    listForCompletion: MutableState<List<String>>,

    modifier : Modifier = Modifier,
    ){
    val currentContext = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CurvedBottomImage()
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 210.dp)
                .padding(16.dp)
                .verticalScroll(state = rememberScrollState())
        ) {
            TextField(
                value = searchWord,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            hideKeyboard(currentContext as Activity)
                            onKeyboardSearch()
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.surfaceTint)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_search_24),
                            null
                        )
                    }
                },
                onValueChange = onUserSearchedChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.searching),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        hideKeyboard(currentContext as Activity)
                        onKeyboardSearch()
                    }
                ),
                colors = TextFieldDefaults.textFieldColors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    containerColor =  Color(0xFFFFFFFF)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .fillMaxWidth()

            )

            if(listForCompletion.value.isNotEmpty() && searchWord.isNotBlank()){
                for (word in listForCompletion.value){
                    CardWordCompletion(word,onWordClicked,currentContext as Activity)
                }
            }
            else{
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "Paquets",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(paquets.keys.toList()) { id ->
                            MyCardPackageVertical(
                                id = id,
                                name = paquets[id]?.name,
                                size = paquets[id]?.mapWordToCard?.size,
                                wordInQueue = reviewQueueMap[id]?.size,
                                onPackageClicked = onPackageClicked,
                                listOfTag = listOf("Journal","Politique"),
                                onLongClick = {
                                    TODO()
                                }
                            )
                        }
                    }
                }
                val chartColors = listOf(
                    Color(0xFF3869B0),
                    Color(0xFF86231B)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                ){
                    Spacer(modifier = Modifier.weight(1f))
                    if(totalReviewCount == 0 && totalLearningCount == 0){
                        DonutChart(
                            inputValues = listOf(50f,50f),
                            colors = chartColors,
                            listOfDescription = listOf("Mots en cours \nd'apprentissage", "Mots appris")
                        )
                    }
                    else{
                        var listOfFloat = listOf(totalReviewCount.toFloat(),totalLearningCount.toFloat())
                        listOfFloat
                        Log.d("TAG", "HomePageScreenDesign: $listOfFloat")
                        DonutChart(
                            inputValues = listOfFloat,
                            colors = chartColors,
                            listOfDescription = listOf("Mots en cours \nd'apprentissage", "Mots connus")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                }
            }

        }

    }

}

@Composable
fun CurvedBottomImage(
    modifier: Modifier = Modifier,
) {
    Image(
        painter =  painterResource(id = R.drawable.jpg4xbottomimage),
        contentDescription = null,
        alignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxWidth()
            .height(272.dp)
            .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
    )
}


@Composable
fun CustomizedButton(
    onClick : ()->Unit,
    @StringRes idText : Int,
    idIcon : Int,
    modifier : Modifier = Modifier
){
    Button(
        onClick = onClick,
        elevation = ButtonDefaults.elevatedButtonElevation(3.dp),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(64.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = stringResource(id = idText),
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id =idIcon),
                contentDescription = stringResource(id = idText)
            )
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyCardPackageVertical(
    id : Int,
    name : String?,
    size : Int?,
    wordInQueue : Int?,
    listOfTag : List<String>,
    onPackageClicked: (Int) -> Unit,
    onLongClick: (Int)->Unit,
    modifier: Modifier = Modifier
) {
    var size = size
    var wordInQueue = wordInQueue
    if (wordInQueue==null){
        wordInQueue=0
    }
    if(size==null){
        size = 0
    }
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(190.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onPackageClicked(id) },
                onLongClick = {
                    if (id != 0) {
                        onLongClick(id)
                    }
                }
            )

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFB68C), Color(0xFFE46962))
                        )
                    )
                    .padding(8.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    if(name.isNullOrEmpty()){
                        Text(
                            text = "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.5.sp,
                        )
                    }
                    else{
                        Text(
                            text = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.5.sp,
                        )
                    }

                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "$wordInQueue",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFFDD2626)
                    )
                    Text(
                        text = "$size",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.Black
                    )
                }

            }

        }

    }
}




@Preview(showBackground = true)
@Composable
fun StartOrderPreview(){

}