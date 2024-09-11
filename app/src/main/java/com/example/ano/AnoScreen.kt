package com.example.ano

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import com.example.ano.ui.HomePageScreenDesign
import com.example.ano.ui.LearningPackage
import com.example.ano.ui.LearningPackageEmpty
import com.example.ano.ui.ListOfPackagesScreen
import com.example.ano.ui.ListOfWordsInAPackage
import com.google.accompanist.systemuicontroller.rememberSystemUiController


enum class AnoScreen(@StringRes val title :Int ){
    Homepage(title = R.string.app_name),
    Dictionary(title = R.string.dictionary),
    Searching(title = R.string.dictionary),
    Favorites(title= R.string.favorites),
    History(title= R.string.histrory),
    ListOfPackage(title=R.string.paquet),
    LearningPackage(title = R.string.learningPackage),
    ListOfWordsInAPackage(title=R.string.listWordsPackage)
}

// TODO: AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnoAppBar(
    navController: NavController,
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
    addWordToQueue : () -> Unit,
    modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false) }
    var modifyClicked by remember { mutableStateOf(false) }

    var title= stringResource(id = currentScreen.title)
    if(currentScreen.title == R.string.learningPackage || currentScreen.title==R.string.listWordsPackage){
        if (packageName != null) {
            title = packageName
        }
    }
    if(currentScreen.title != R.string.app_name){
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text =title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
            },

            modifier = modifier,
            navigationIcon = {
                if(canNavigateBack){
                    IconButton(
                        onClick = {
                            addWordToQueue()
                            navigateUp()
                        },
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
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded =false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text="Afficher la liste de mots")},
                        onClick = {
                            navController.navigate(AnoScreen.ListOfWordsInAPackage.name)
                            expanded =false
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    navController.navigate(AnoScreen.ListOfWordsInAPackage.name)
                                    expanded =false
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.List,
                                    contentDescription = "Supprimer ce mot du paquet",
                                )
                            }
                        }
                    )
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

    var listOfIcon = listOf(R.drawable.round_home_24,R.drawable.round_menu_book_24,R.drawable.round_star_24,R.drawable.round_history_24)
    var selectedItem by remember { mutableIntStateOf(0) }
    val labels = listOf(R.string.Accueil, R.string.paquet, R.string.favorites, R.string.histrory)
    var listOfDestination = listOf<AnoScreen>(AnoScreen.Homepage,AnoScreen.ListOfPackage,AnoScreen.Favorites,AnoScreen.History)

    val systemUiController = rememberSystemUiController()

    // Set the color of the status bar
    LaunchedEffect(Unit) {
        if (currentScreen.title ==AnoScreen.Homepage.title){
            systemUiController.setStatusBarColor(color = Color(0xFFFFDAD7))
        }
        else{
            systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnoAppBar(
                navController = navController,
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
                navigateUp = { navController.navigateUp() },
                addWordToQueue = {viewModel.addWordToReviewQueueMap()}
            )
        },
        bottomBar = {
            if(currentScreen.title !=R.string.learningPackage){
                NavigationBar {
                    labels.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {Icon(painter = painterResource(id =listOfIcon[index]),contentDescription = null)},
                            label = { Text(text = stringResource(id = labels[index])) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                viewModel.reinitListCompletion()
                                viewModel.updateSearchedWord("")
                                navController.navigate(listOfDestination[index].name)

                            }
                        )
                    }
                }
            }

        }
    ) { innerPadding ->
        NavHost(
            navController=navController,
            startDestination =AnoScreen.Homepage.name,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(route = AnoScreen.Homepage.name){
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFDAD7))
                }
                viewModel.countCardsByTypeAll()
                HomePageScreenDesign(
                    totalReviewCount = viewModel.totalReviewCount,
                    totalLearningCount = viewModel.totalLearningCount,
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
                    },
                    paquets = uiState.paquets,
                    onPackageClicked = { viewModel.onPackageClicked(it)
                        if(viewModel.packageIsNotEmpty()){
                            navController.navigate(AnoScreen.LearningPackage.name)}
                    },
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)},
                    listForCompletion = viewModel.listForCompletion,
                )
            }
            composable(route = AnoScreen.Searching.name){
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
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
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
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
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
                FavoritesScreen(
                    isFavorite = {viewModel.isWordInMyWords(it)},
                    words = uiState.paquets[0]?.mapWordToCard?.keys?.toList(),
                    onFavoriteButtonClicked = { viewModel.onAddButtonClickedList(it,context=context.applicationContext)},
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)})
            }
            composable(route = AnoScreen.History.name){
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
                HistoryScreen(
                    isFavorite = {viewModel.isWordInMyWords(it)},
                    words = uiState.wordsInHistory,
                    onFavoriteButtonClicked = { viewModel.onAddButtonClickedList(it,context=context.applicationContext)},
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)}
                )
            }
            composable(route = AnoScreen.ListOfPackage.name){
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
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
                    onLongClick = {viewModel.onLongClickedPackage(it)},
                    reviewQueueMap = AnoAnki.ReviewReceiver.reviewQueueMap,
                    onListOfWords = {navController.navigate(AnoScreen.ListOfWordsInAPackage.name)}
                )
            }
            composable(route = AnoScreen.LearningPackage.name){
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                    if(viewModel.nowMoreCardToShow){
                        viewModel.calculateDelayBeforeNextCard()
                        AnoAnki.delayBeforeNextCardByPackage[viewModel.currentPackageId]?.let { it1 ->
                            LearningPackageEmpty(
                                it1
                            )
                        }
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
            composable(route = AnoScreen.ListOfWordsInAPackage.name){
                LaunchedEffect(Unit) {
                    systemUiController.setStatusBarColor(color = Color(0xFFFFFBFF))
                }
                ListOfWordsInAPackage(
                    isFavorite = {viewModel.isWordInMyWords(it)},
                    words = uiState.paquets[viewModel.currentPackageId]?.mapWordToCard?.keys?.toList(),
                    onFavoriteButtonClicked = { viewModel.onAddButtonClickedList(it,context=context.applicationContext)},
                    onWordClicked = { viewModel.onWordClicked(it)
                        navController.navigate(AnoScreen.Dictionary.name)})
            }


        }
    }
}






