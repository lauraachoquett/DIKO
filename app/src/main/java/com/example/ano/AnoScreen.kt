package com.example.ano

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ano.model.AnoAnki
import com.example.ano.model.AnoViewModel
import com.example.ano.ui.DialogChoice
import com.example.ano.ui.DictionaryDefinitionScreen
import com.example.ano.ui.DictionarySearchingScreen
import com.example.ano.ui.FavoritesScreen
import com.example.ano.ui.HistoryScreen
import com.example.ano.ui.HomePageScreen
import com.example.ano.ui.LearningPackage
import com.example.ano.ui.LearningPackageEmpty
import com.example.ano.ui.ListOfPackagesScreen
import com.example.readinggoals.ui.theme.Barlow


enum class AnoScreen(@StringRes val title :Int ){
    Homepage(title = R.string.app_name),
    Dictionary(title = R.string.dictionary),
    Searching(title = R.string.dictionary),
    Favorites(title= R.string.favorites),
    History(title= R.string.histrory),
    ListOfPackage(title=R.string.paquet),
    LearningPackage(title = R.string.learningPackage)
}

// TODO: AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnoAppBar(
    currentScreen: AnoScreen,
    packageName : String?,
    newPackageName : String,
    onUserModificationdChanged: (String) -> Unit,
    onModifyName : ()->Unit,
    CloseClicked :()->Unit,
    onDeleteWordClicked:()->Unit,
    onDeleteClicked : ()->Unit,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    scrollBehavior : TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false) }
    var modifyClicked by remember { mutableStateOf(false) }

    var title= stringResource(id = currentScreen.title)
    if(currentScreen.title == R.string.learningPackage){
        if (packageName != null) {
            title = packageName
        }
    }
    CenterAlignedTopAppBar(
        title = {
            Text(
                text =title,
                fontFamily = Barlow,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
                },

        modifier = modifier,
        navigationIcon = {
            if(canNavigateBack){
                IconButton(
                    onClick = navigateUp,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )

                }
            }
        },
        actions = {
            if(currentScreen.title == R.string.learningPackage){
                IconButton(
                    onClick = { expanded =! expanded},
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Localized description"
                    )
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded =false }) {
                DropdownMenuItem(
                    text = { Text(text="Supprimer ce mot du paquet")},
                    onClick = {
                        onDeleteWordClicked()
                        expanded =false
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                onDeleteWordClicked()
                                expanded =false
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Supprimer ce mot du paquet",
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(text="Afficher la liste de mots")},
                    onClick = {

                        expanded =false
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                onDeleteWordClicked()
                                expanded =false
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Supprimer ce mot du paquet",
                            )
                        }
                    }
                )
                if(packageName != "Mes mots"){
                    DropdownMenuItem(
                        text = { Text(text="Modifier le nom du paquet")},
                        onClick = {
                            modifyClicked=true
                            expanded =false
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    modifyClicked=true
                                    expanded =false
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Change",
                                )
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = {Text(text ="Supprimer ce paquet")} ,
                        onClick = {
                            onDeleteClicked()
                            navigateUp()
                            expanded =false
                        }
                        ,
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    onDeleteClicked()
                                    navigateUp()
                                    expanded =false
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                )

                            }
                        }
                    )
                }

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
                    newPackageName = newPackageName,
                    onUserModificationdChanged = onUserModificationdChanged,
                    idIcon = R.drawable.round_edit_24
                )
            }
            
        },
        scrollBehavior = scrollBehavior,
        )
}


@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnoApp() {
    val navController = rememberNavController()
    // Create ViewModel
    val viewModel: AnoViewModel = viewModel()
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AnoScreen.valueOf(
        backStackEntry?.destination?.route ?: AnoScreen.Homepage.name
    )
    val context = LocalContext.current

    var scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnoAppBar(
                currentScreen = currentScreen,
                newPackageName = viewModel.newPackageName,
                onUserModificationdChanged ={
                    viewModel.updateNewPackageName(it)
                },
                CloseClicked = {viewModel.reinitNewPackageName()},
                onModifyName = {viewModel.changePackageName()},
                onDeleteWordClicked = {viewModel.supressOrDoNothingPackage(viewModel.currentPackageId)},
                onDeleteClicked = { viewModel.deleteAPackage() },
                packageName = uiState.paquets[viewModel.currentPackageId]?.name,
                scrollBehavior=scrollBehavior,
                canNavigateBack = navController.previousBackStackEntry !=null,
                navigateUp = { navController.navigateUp() })
        }
    ) { innerPadding ->
        NavHost(
            navController=navController,
            startDestination =AnoScreen.Homepage.name,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(route = AnoScreen.Homepage.name){
                viewModel.reinitListCompletion()
                viewModel.updateSearchedWord("")
                HomePageScreen(
                    onDictionaryButtonClicked = { navController.navigate(AnoScreen.Searching.name) },
                    onFavoriteButtonClicked = { navController.navigate(AnoScreen.Favorites.name) },
                    onHistoryButtonClicked = { navController.navigate(AnoScreen.History.name) },
                    onPackageButtonClicked = {navController.navigate(AnoScreen.ListOfPackage.name)}
                )
            }
            composable(route = AnoScreen.Searching.name){
                DictionarySearchingScreen(
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)},
                    listForCompletion = viewModel.listForCompletion,
                    searchWord = viewModel.searchWord,
                    onUserSearchedChanged ={
                        viewModel.updateSearchedWord(it)
                        viewModel.autoCompletion()
                    },
                    onKeyboardSearch = {
                        viewModel.onKeyboardSearch()
                        if(viewModel.isWordInDico()){
                            viewModel.reinitListCompletion()
                            navController.navigate(AnoScreen.Dictionary.name)}
                    }
                )
            }
            composable(route = AnoScreen.Dictionary.name){
                viewModel.initSelectedPackage()
                viewModel.initSelectedDefintion()
                DictionaryDefinitionScreen(
                    word = viewModel.currentWord,
                    wordInfos =viewModel.infoDefCurrentWord,
                    paquets = uiState.paquets,
                    selectedPackage = viewModel.selectedPackage,
                    onCheckedClickedPackage = {viewModel.onCheckedClickedPackage(it)},
                    addWordToPackages = { viewModel.addWordToPackages(context.applicationContext) },
                    selectedDefinitionAndNature = viewModel.selectedDefinitionByNature,
                    onCheckedDefinition = { it1, it2 -> viewModel.onCheckedDefinition(it1, it2) },
                    onConfirmationDefinition = {
                        viewModel.onConfirmationDefinition(context.applicationContext)
                        viewModel.initSelectedDefintion()
                    },
                    isThereAnyDefinitionSelected ={viewModel.isThereAnyDefinitionSelected()}

                )
            }
            composable(route = AnoScreen.Favorites.name){
                FavoritesScreen(
                    isFavorite = {viewModel.isWordInMyWords(it)},
                    words = uiState.paquets[0]?.mapWordToCard?.keys?.toList(),
                    onFavoriteButtonClicked = { viewModel.onAddButtonClickedList(it,context=context.applicationContext)},
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)})
            }
            composable(route = AnoScreen.History.name){
                HistoryScreen(
                    isFavorite = {viewModel.isWordInMyWords(it)},
                    words = uiState.wordsInHistory,
                    onFavoriteButtonClicked = { viewModel.onAddButtonClickedList(it,context=context.applicationContext)},
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)}
                )
            }
            composable(route = AnoScreen.ListOfPackage.name){
                ListOfPackagesScreen(
                    paquets = uiState.paquets,
                    onPackageClicked ={ viewModel.onPackageClicked(it)
                        if(viewModel.packageIsNotEmpty()){
                            navController.navigate(AnoScreen.LearningPackage.name)}
                        },
                    newPackageName = viewModel.newPackageName,
                    onKeyboardConfirm ={viewModel.onKeyboardConfirmName()} ,
                    onUserModificationdChanged ={
                        viewModel.updateNewPackageName(it)
                    },
                    CloseClicked = {viewModel.reinitNewPackageName()},
                    onDeleteClicked = {viewModel.deleteAPackage()},
                    onModifyName = {viewModel.changePackageName()},
                    onLongClick = {viewModel.onLongClickedPackage(it)}
                )
            }
            composable(route = AnoScreen.LearningPackage.name){
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                    if(viewModel.nowMoreCardToShow){
                        viewModel.calculateDelayBeforeNextCard()
                        LearningPackageEmpty(AnoAnki.delayBeforeNextCard)
                    }
                    else{
                        LearningPackage(
                            word = viewModel.wordOnLearningPackageScreen,
                            wordInfos = viewModel.infoDefCurrentWord ,
                            onWordClicked = { viewModel.onWordClicked(it)
                                navController.navigate(AnoScreen.Dictionary.name)},
                            wordToDisplayInAPackage = {viewModel.wordToDisplayInAPackage(it)},
                        )
                    }
                }
            }
        }
    }






