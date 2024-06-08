package com.example.ano.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ano.R
import com.example.readinggoals.ui.theme.Barlow


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
                .padding(bottom =32.dp)
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
            .padding(16.dp)
            .height(64.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = stringResource(id = idText),
                fontFamily = Barlow,
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


@Preview(showBackground = true)
@Composable
fun StartOrderPreview(){
    HomePageScreen(
        onDictionaryButtonClicked = { /*TODO*/ },
        onFavoriteButtonClicked = { /*TODO*/ },
        onHistoryButtonClicked = { /*TODO*/ },
        onPackageButtonClicked = {/*TODO*/})
}