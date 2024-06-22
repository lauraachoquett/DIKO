package com.example.ano.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ano.R
import com.example.ano.dataSource.InfoDefinitions
import com.example.ano.dataSource.InformationWordByNature
import com.example.ano.dataSource.paquetAttributes
import com.example.ano.model.typeSelectedInfoForLearning
import com.example.readinggoals.ui.theme.Barlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryDefinitionScreen(
    modifier : Modifier = Modifier,
    word : String,
    wordInfos : List<InformationWordByNature>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    paquets : Map<Int,paquetAttributes>,
    selectedPackage: MutableMap<Int,Boolean>,
    selectedDefinitionAndNature : MutableMap<String, typeSelectedInfoForLearning>,
    onCheckedClickedPackage :(Int)->Unit,
    addWordToPackages : () -> Unit,
    onCheckedDefinition: (String, String) -> Unit,
    onConfirmationDefinition : ()->Unit,
    isThereAnyDefinitionSelected :()->Boolean
    ){
    Log.d("MyTag", "définitions seléctionnées : $selectedDefinitionAndNature")

    var isWordInAPackage = selectedPackage.values.any{it}

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var PackageModifierChoice by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDefinitionsScreen by remember {
        mutableStateOf(false)
    }

    val isThereAnyDefinitionSelected = isThereAnyDefinitionSelected()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if(!isWordInAPackage){
                        onCheckedClickedPackage(0)
                        addWordToPackages()
                        scope.launch {
                            val result = snackbarHostState
                                .showSnackbar(
                                    message = "Ajouté au paquet Mes Mots",
                                    actionLabel = "Modifier",
                                    // Defaults to SnackbarDuration.Short
                                    duration = SnackbarDuration.Long
                                )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    showBottomSheet= true
                                }
                                SnackbarResult.Dismissed -> {
                                    /* Handle snackbar dismissed */
                                }
                            }
                        }
                    }
                    else if(selectedDefinitionsScreen){
                        Log.d("MonTag", "définitions seléctionnées : $selectedDefinitionAndNature")
                        if(isThereAnyDefinitionSelected()){
                            onConfirmationDefinition()
                            selectedDefinitionsScreen=false
                        }
                        else{//si aucune définition n'est sélectionnée message d'erreur
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Une définition au moins doit être sélectionnée",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                    else{
                        showBottomSheet=true
                    }
                          },
                content = {
                    if(isWordInAPackage and !selectedDefinitionsScreen) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    else if(selectedDefinitionsScreen){
                        Icon(Icons.Filled.Check,contentDescription = null)
                    }
                    else{
                        Icon(Icons.Rounded.Add, contentDescription = "Add")
                    }
                },
                modifier = Modifier
                    .height(88.dp)
                    .width(88.dp)
                    .padding(8.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        if (showBottomSheet) {
            ModalBottomSheetChoice(
                sheetState = sheetState,
                scope = scope,
                onDismissRequest = { showBottomSheet = false},
                hideModalBottomSheet = { showBottomSheet = false},
                onModifyName = {
                    PackageModifierChoice=true
                    showBottomSheet = false},
                onModifyDefinitionChoice = {
                    selectedDefinitionsScreen = true
                    showBottomSheet = false
                }
            )
        }
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(16.dp)
        ) {
            items(wordInfos.size){
                    index ->
                var elt = wordInfos[index]

                val nature = elt.nature
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        fontFamily = Barlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = "   -   [$nature]",
                        fontFamily = Barlow,
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            //PackageModifierChoice=! PackageModifierChoice
                        },
                        enabled = isWordInAPackage,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)


                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_check_circle_24 ),
                            contentDescription = null,
                        )
                    }
                }

                Choice(
                    wordInfo=elt,
                    selectedDefinitionsScreen = selectedDefinitionsScreen,
                    selectedDefinitionAndNature =selectedDefinitionAndNature,
                    onCheckedDefinition=onCheckedDefinition,
                )
            }

        }
        Spacer(modifier = Modifier.height(72.dp))
        if(PackageModifierChoice){
            DialogChoice(
                onDismissRequest = {  PackageModifierChoice=!PackageModifierChoice },
                onConfirmation = {
                    addWordToPackages()
                    PackageModifierChoice=!PackageModifierChoice
                },
                packages = paquets,
                selectedPackage = selectedPackage,
                onCheckedClickedPackage=onCheckedClickedPackage
            )
        }
    }
}



@Composable
fun DialogChoice(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    packages : Map<Int,paquetAttributes>,
    selectedPackage: MutableMap<Int,Boolean>,
    onCheckedClickedPackage :(Int)->Unit
) {
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
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Choix des paquets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp),
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    thickness = 1.dp,
                )
                for (key in packages.keys.toList()){
                    selectedPackage[key]?.let {
                        checkedPackage(
                            namePackage = packages[key]?.name,
                            idPackage = key,
                            checkedState = it,
                            onCheckedClickedPackage=onCheckedClickedPackage
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                    ) {
                        Text("Fermer")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirmation() },
                    ) {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}

@Composable
fun checkedPackage(
    namePackage : String?,
    idPackage : Int,
    checkedState : Boolean,
    onCheckedClickedPackage :(Int)->Unit

){
    var checkedStateLocal by remember { mutableStateOf(checkedState) }
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 4.dp)
            .clickable {
                checkedStateLocal = !checkedStateLocal
                onCheckedClickedPackage(idPackage)
            }
    ){
        Text(
            text = "$namePackage"
        )
        Spacer(modifier = Modifier.weight(0.5F))
        Checkbox(
            checked = checkedStateLocal,
            onCheckedChange ={
                checkedStateLocal =! checkedStateLocal
                onCheckedClickedPackage(idPackage)
            }
        )
    }
}




@Composable
fun DefinitionsSelected(
    nature : String,
    wordInfosDef : Map<String, InfoDefinitions>,
    selectedDefinitionsScreen : Boolean,
    selectedDefinitionAndNature : MutableMap<String, typeSelectedInfoForLearning>,
    onCheckedDefinition: (String,String) -> Unit,

    ){
    val listOfdef = wordInfosDef.keys.toList()
    var index =0
    Column{
        for(def in listOfdef){
            wordInfosDef[def]?.exemples?.let {
                    DefinitionRow(
                        nature = nature,
                        index = index,
                        definition = def,
                        lexique = wordInfosDef[def]?.lexique,
                        exemples = it,
                        selectedDefinitionsScreen = selectedDefinitionsScreen,
                        selectedDefinitionAndNature =selectedDefinitionAndNature,
                        onCheckedDefinition = onCheckedDefinition,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp,
            )
            index += 1
        }
    }
}

@Composable
fun DefinitionRow(
    nature : String,
    index : Int,
    definition : String,
    lexique : String?,
    exemples : List<String>,
    selectedDefinitionsScreen : Boolean,
    selectedDefinitionAndNature : MutableMap<String, typeSelectedInfoForLearning>,
    onCheckedDefinition: (String,String) -> Unit,

    ){
    var expanded by remember { mutableStateOf(false) }
    val indexAff = index+1
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .clickable { onCheckedDefinition }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "$indexAff.",
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (lexique != null) {
                Text(
                    text = "[$lexique]",
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if(selectedDefinitionsScreen){
                selectedDefinitionAndNature[nature]?.definitionSelected?.get(definition)
                    ?.let {
                        Log.d("MyTag", "CheckedState : $it")
                        CheckBoxSelectedDefinition(
                        nature = nature,
                        def = definition,
                        checkedState = it,
                        onCheckedDefinition = onCheckedDefinition
                    )
                    }
            }
            else{
                if(exemples.isNotEmpty()){
                    ButtonExemples(expanded = expanded , onClick = { expanded=!expanded })
                }
            }
        }
        Text(
            text = definition,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start=4.dp, end=4.dp,bottom=8.dp)
        )
    }
    if(expanded){
        Spacer(modifier=Modifier.height(4.dp))
        funExemples(exemples = exemples)
    }
}

@Composable
fun funExemples(
    exemples: List<String>
) {
    val specialColor = MaterialTheme.colorScheme.secondary // Couleur spéciale
    Column {
        for (exemple in exemples) {
            Row {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "- ")
                val annotatedString = buildAnnotatedString {
                    val pattern = "'''(.*?)'''".toRegex()
                    val matches = pattern.findAll(exemple)
                    var currentIndex = 0

                    matches.forEach { matchResult ->
                        val range = matchResult.range
                        val boldText = matchResult.value
                        val start = range.first
                        val end = range.last

                        // Ajouter le texte avant le match
                        append(exemple.substring(currentIndex, start))

                        // Appliquer le style gras sur le texte correspondant à la regex
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = specialColor // Changez la couleur si nécessaire
                            )
                        ) {
                            append(boldText.trim('\'')) // Retirez les ''' du texte gras
                        }

                        // Mettre à jour l'index courant
                        currentIndex = end + 1
                    }

                    // Ajouter le texte restant après le dernier match
                    append(exemple.substring(currentIndex, exemple.length))
                }
                Text(text = annotatedString)
            }
        }
    }
}

@Composable
fun ButtonExemples(
    expanded : Boolean,
    onClick : () -> Unit
){
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(0.dp)
    ){
        Icon(
            painter = if(expanded) painterResource(id = R.drawable.baseline_expand_less_24)
            else painterResource(id = R.drawable.baseline_expand_more_24),
            contentDescription = null,
            modifier = Modifier.padding(0.dp)
        )
    }
}

@Composable
fun CheckBoxSelectedDefinition(
    nature : String,
    def : String,
    checkedState : Boolean,
    onCheckedDefinition :(String,String)->Unit,
){
    var checkedStateLocal by remember { mutableStateOf(checkedState) }
    Checkbox(
        checked = checkedStateLocal,
        onCheckedChange ={
            checkedStateLocal =! checkedStateLocal
            onCheckedDefinition(def,nature)
        }
    )
}

@Composable
fun ListSelected(list: List<String>){
    Column {
        for(word in list){
            Text(
                text = " - $word" ,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(4.dp)
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Choice(
    wordInfo : InformationWordByNature,
    selectedDefinitionsScreen : Boolean,
    selectedDefinitionAndNature : MutableMap<String, typeSelectedInfoForLearning>,
    onCheckedDefinition: (String,String) -> Unit,

    ) {
    val nature = wordInfo.nature
    val wordInfoDef = wordInfo.definitions
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf(
        stringResource(id = R.string.definitions),
        stringResource(id  = R.string.derives ),
        stringResource(id = R.string.syn)
    )

    SingleChoiceSegmentedButtonRow {
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


    if (selectedIndex == 0) {
        DefinitionsSelected(
            nature= nature,
            wordInfoDef,
            selectedDefinitionsScreen =selectedDefinitionsScreen,
            selectedDefinitionAndNature=selectedDefinitionAndNature,
            onCheckedDefinition=onCheckedDefinition,
        )
    } else if (selectedIndex == 1) {
        wordInfo.derives?.let { ListSelected(list = it) }
    } else {
        wordInfo.synonymes?.let { ListSelected(list = it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetChoice(
    sheetState : SheetState,
    scope : CoroutineScope,
    onDismissRequest: () -> Unit,
    hideModalBottomSheet : ()->Unit,
    onModifyName : ()->Unit,
    onModifyDefinitionChoice : ()->Unit
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
                text = "Modifier le choix des paquets",
                onClick = onModifyName,
                idIcon = R.drawable.round_edit_24
            )
            actionForBottomSheet(
                text = "Modifier les définitions choisies",
                onClick =onModifyDefinitionChoice,
                idIcon = R.drawable.round_check_circle_24
            )
        }
    }
}



        

