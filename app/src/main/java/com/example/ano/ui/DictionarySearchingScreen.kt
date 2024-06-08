package com.example.ano.ui

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.readinggoals.ui.theme.Barlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySearchingScreen(
    onWordClicked :(String)->Unit,
    listForCompletion: MutableState<List<String>>,
    searchWord : String,
    onUserSearchedChanged: (String) -> Unit,
    onKeyboardSearch: () -> Unit,
    modifier : Modifier = Modifier,
){
    val currentContext = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())

    ){
        Spacer(modifier = Modifier.height(64.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
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
                        fontFamily = Barlow,
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
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .fillMaxWidth()

            )
        }
        if(searchWord.isBlank()) {
            Spacer(modifier = Modifier.height(96.dp))
            Image(
                painter = painterResource(id = R.drawable.ano_search_book),
                contentDescription = null,
                modifier = modifier
                    .width(318.dp)
                    .padding(32.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        if(listForCompletion.value.isNotEmpty()){
            for (word in listForCompletion.value){
                CardWordCompletion(word,onWordClicked,currentContext as Activity)
            }

        }

    }
}

fun hideKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusedView: View? = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

@Composable
fun CardWordCompletion(word:String,onWordClicked :(String)->Unit,activity: Activity){
    OutlinedButton(
        onClick = {
            onWordClicked(word)
            hideKeyboard(activity)
                  },
        shape= MaterialTheme.shapes.small,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 2.dp)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
            text = word,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 8.dp)
        )}

    }
}



@Preview(showBackground = true)
@Composable
fun HomepagePreview(){
    //DictionarySearchingScreen(onValidateClicked = { /*TODO*/ })
}