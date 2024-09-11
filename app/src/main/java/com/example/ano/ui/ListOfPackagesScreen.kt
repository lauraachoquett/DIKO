package com.example.ano.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ano.R
import com.example.ano.dataSource.paquetAttributes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Queue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfPackagesScreen(
    paquets :Map<Int,paquetAttributes>,
    onPackageClicked: (Int) -> Unit,
    newPackageName : String,
    onUserModificationdChanged: (String) -> Unit,
    onKeyboardConfirm: () -> Unit,
    CloseClicked:()->Unit,
    onDeleteClicked : ()->Unit,
    onModifyName : ()->Unit,
    onLongClick: (Int) -> Unit,
    onListOfWords:()->Unit,
    reviewQueueMap : MutableMap<Int, Queue<String>>
){
    var addClicked by remember { mutableStateOf(false) }
    var modifyClicked by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if(paquets.isNullOrEmpty()){
        Text(text = "Créer ton premier paquet ! ")
    }
    else {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { addClicked =! addClicked },
                    content = {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    },
                    modifier = Modifier
                        .height(88.dp)
                        .width(88.dp)
                        .padding(8.dp)
                )
            },
            floatingActionButtonPosition = FabPosition.End,
        ) {innerPadding ->
            if (showBottomSheet) {
                ModalBottomSheetDisplay(
                    sheetState = sheetState,
                    scope = scope,
                    onDismissRequest = { showBottomSheet = false},
                    hideModalBottomSheet = { showBottomSheet = false},
                    onModifyClicked = {
                        modifyClicked=true
                        showBottomSheet = false},
                    onDeleteClicked = {
                        scope.launch {
                            val result = snackbarHostState
                                .showSnackbar(
                                    message = "Voulez-vous vraiment supprimer ce paquet ? ",
                                    actionLabel = "Supprimer",
                                    // Defaults to SnackbarDuration.Short
                                    duration = SnackbarDuration.Short
                                )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    onDeleteClicked()
                                    showBottomSheet=false
                                }
                                SnackbarResult.Dismissed -> {

                                }
                            }
                        }
                        showBottomSheet = false
                    },
                    onListOfWords = onListOfWords
                )
            }
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(paquets.keys.toList()) { id ->
                    MyCardPackage(
                        id = id,
                        name = paquets[id]?.name,
                        size = paquets[id]?.mapWordToCard?.size,
                        wordInQueue = reviewQueueMap[id]?.size,
                        onPackageClicked = onPackageClicked,
                        listOfTag = listOf("Journal","Politique"),
                        modifier = Modifier.padding(innerPadding),
                        onLongClick = {
                            showBottomSheet=true
                            onLongClick(it)
                        }
                    )
                }
            }
            if(addClicked){
                DialogChoice(
                    text ="Création d'un nouveau paquet",
                    onDismissRequest = {
                        addClicked =! addClicked
                        CloseClicked()
                                       },
                    onConfirmation = {
                        addClicked =!addClicked
                        onKeyboardConfirm()
                                     },
                    newPackageName=newPackageName,
                    onUserModificationdChanged=onUserModificationdChanged,
                    idIcon = R.drawable.baseline_post_add_24
                )
            }
            if(modifyClicked){
                DialogChoice(
                    text ="Modification du nom du paquet",
                    onDismissRequest = {
                        modifyClicked =! modifyClicked
                        CloseClicked()
                    },
                    onConfirmation = {
                        modifyClicked =!modifyClicked
                        onModifyName()

                    },
                    newPackageName=newPackageName,
                    onUserModificationdChanged=onUserModificationdChanged,
                    idIcon = R.drawable.round_edit_24
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetDisplay(
    sheetState : SheetState,
    scope : CoroutineScope,
    onDismissRequest: () -> Unit,
    hideModalBottomSheet : ()->Unit,
    onModifyClicked : ()->Unit,
    onDeleteClicked : ()->Unit,
    onListOfWords : () -> Unit
){
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        // Sheet content
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ){
            actionForBottomSheet(
                text = "Modifier le nom du paquet",
                onClick = onModifyClicked,
                idIcon = R.drawable.round_edit_24
            )
            actionForBottomSheet(
                text = "Supprimer le paquet",
                onClick =onDeleteClicked,
                idIcon = R.drawable.round_delete_24
            )
            actionForBottomSheet(
                text = "Afficher la liste de mots",
                onClick =onListOfWords,
                idIcon = R.drawable.baseline_format_list_bulleted_24
            )

        }
    }
}

@Composable
fun actionForBottomSheet(
    text : String,
    onClick : ()-> Unit,
    @DrawableRes idIcon : Int,
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                onClick()
            }
    ){
      Text(
          text = text
      )
      Spacer(modifier = Modifier.weight(1f))
      IconButton(onClick = onClick) {
          Icon(
              painterResource(id = idIcon),
              contentDescription = null,
          )
          
      }  
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogChoice(
    text : String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    newPackageName : String,
    onUserModificationdChanged: (String) -> Unit,
    @DrawableRes idIcon : Int
)  {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(16.dp),
                )
                TextFieldPackage(
                    newPackageName = newPackageName,
                    onUserModificationdChanged =onUserModificationdChanged,
                    onKeyboardConfirm ={onConfirmation()},
                    idIcon = idIcon
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = {
                            onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Fermer")
                    }
                    TextButton(
                        onClick = {
                            onConfirmation() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldPackage(
    newPackageName : String,
    onUserModificationdChanged: (String) -> Unit,
    onKeyboardConfirm: () -> Unit,
    @DrawableRes idIcon : Int
    ) {
    TextField(
        value = newPackageName,
        onValueChange = onUserModificationdChanged,
        placeholder = {
            Text(
                text = stringResource(id = R.string.namePackage),
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        leadingIcon = {Icon(painter = painterResource(id = idIcon),contentDescription=null)},
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Sentences
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onKeyboardConfirm()
            }
        ),
        colors = TextFieldDefaults.textFieldColors(
            focusedTextColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .padding(8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyCardPackage(
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
            .fillMaxWidth()
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
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
                Spacer(modifier = modifier.height(16.dp))
                Text(
                    text = "$size",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFB68C), Color(0xFFE46962))
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardElement(
    id : Int,
    name: String?,
    size: Int?,
    wordInQueue : Int?,
    onPackageClicked: (Int) -> Unit,
    onLongClick: (Int)->Unit,
    modifier: Modifier = Modifier
){
    var size = size
    var wordInQueue = wordInQueue
    if (wordInQueue==null){
        wordInQueue=0
    }
    if(size==null){
        size = 0
    }
    ElevatedCard(
        //onClick = { onPackageClicked(name)},
        shape= MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = { onPackageClicked(id) },
                onLongClick = {
                    if (id != 0) {
                        onLongClick(id)
                    }
                }
            )

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)

            ){
            Column(
                horizontalAlignment = AbsoluteAlignment.Left
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
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider()
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(
                    text = "$wordInQueue",
                    fontWeight = FontWeight.Normal,
                    color = Color.Red,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$size",
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp,
                )
            }



        }
    }
}





