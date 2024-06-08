package com.example.ano.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.ano.dataSource.DataSource
import com.example.ano.dataSource.DataSource.mapOfWords
import com.example.ano.dataSource.InformationWordByNature
import com.example.ano.dataSource.extractPairs
import com.example.ano.dataSource.paquetAttributes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class typeSelectedInfoForLearning (
    val isNatureSelected :Boolean,
    val definitionSelected : Map<String,Boolean>
)

class AnoViewModel : ViewModel(){

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    //Word actually on the Screen
    var currentWord by mutableStateOf("")
        private set

    //Word in the textfield of SearchingScreen
    var searchWord by mutableStateOf("")
        private set

    // TopBar in LearningpackageScreen
    var currentPackageId by mutableStateOf(0)
        private set

    //For the creation of a new Package
    var newPackageName by mutableStateOf("")
        private set

    //List for searchScreen auto-completion
    var listForCompletion: MutableState<List<String>> = mutableStateOf(emptyList())
        private set

    //Informations about the word displayed on DefinitionScreen
    lateinit var infoDefCurrentWord: List<InformationWordByNature>

    var wordOnLearningPackageScreen by mutableStateOf("")
        private set

    lateinit var selectedPackage: MutableMap<Int,Boolean>

    //Première clé : nature
    lateinit var selectedDefinitionByNature : MutableMap<String,typeSelectedInfoForLearning>

    ///////////////////////////////////////////IdGenerator pour les paquets//////////////////////

    companion object IdGenerator {
        private var currentId: Int = 1

        fun generateId(): Int {
            return currentId++
        }
    }

    ////////////////////////////////////////////////Paquets :///////////////////////////////////////
    fun updateNewPackageName(name:String){
        newPackageName=name
    }

    fun onPackageClicked(id: Int) {
        updateCurrentPackageId(id)
        if(packageIsNotEmpty()){
            wordToDisplayInAPackage()
        }
    }

    fun onLongClickedPackage(id: Int){
        updateCurrentPackageId(id)
        Log.d("MonTag", "La valeur de la variable est : $currentPackageId")
    }

    fun updateCurrentPackageId(id : Int){
        currentPackageId=id
    }

    fun createNewPackage(name : String){
        val id = generateId()
        _uiState.update { currentState ->
            val updatedPaquets = currentState.paquets.toMutableMap().apply {
                // Ajouter ou mettre à jour la paire clé-valeur dans la carte
                this[id] = paquetAttributes(wordsAndInfo = mapOf(),name =name)
            }
            currentState.copy(
                paquets = updatedPaquets.toMap(),
            )
        }
    }

    fun onKeyboardConfirmName(){
        createNewPackage(newPackageName.trim())
        reinitNewPackageName()
    }

    fun reinitNewPackageName(){
        newPackageName=""
    }

    fun packageIsNotEmpty():Boolean{
        val wordsInThePackage = uiState.value.paquets[currentPackageId]?.wordsAndInfo?.keys?.toList()
        val size = wordsInThePackage?.size
        if(size ==null || size ==0){
            return false
        }
        else{
            return true
        }
    }
    fun wordToDisplayInAPackage(){
        val paquetAttributes =uiState.value.paquets[currentPackageId]
        if (paquetAttributes != null) {
            if(paquetAttributes.wordsAndInfo != null ){
                val listOfWordsInThePackage= paquetAttributes.wordsAndInfo!!.keys.toList()
                if(listOfWordsInThePackage != null){
                    val randomIndex = listOfWordsInThePackage?.let { Random.nextInt(it.size) }
                    wordOnLearningPackageScreen = listOfWordsInThePackage[randomIndex!!]
                    infoDefCurrentWord = uiState.value.paquets[currentPackageId]?.wordsAndInfo?.get(wordOnLearningPackageScreen)!!
                    updateCurrentWord(wordOnLearningPackageScreen)
                }
            }
        }
    }

    fun initSelectedPackage() {
        val paquets = uiState.value.paquets
        selectedPackage = mutableMapOf() // Initialisez selectedPackage
        for (id in paquets.keys.toList()) {
            val words = paquets[id]?.wordsAndInfo?.keys?.toList()
            val isSelected = (words?.contains(currentWord)  == true)
            selectedPackage[id] = isSelected
        }
    }

    fun onCheckedClickedPackage(id: Int) {
        if (selectedPackage != null) {
            selectedPackage[id] = !selectedPackage[id]!!
        }
    }
    //Choix des définitions
    fun initSelectedDefintion() {
        Log.d("MonTag", "initialisation des définitions")
        selectedDefinitionByNature = mutableMapOf()
        Log.d("MonTag", "current word : $currentWord")
        val listInfoByNature = mapOfWords[currentWord]?.infoWordByNature
        //Compare par rapport aux définitions dans le paquet 0 ( cela pose question quand le mot n'est pas dans le paquet 0)
        var idOfAPackage = findPackageWithWord()
        var mapOfSelectedDefinition : MutableMap<String,Boolean> = mutableMapOf()
        if(idOfAPackage > -1){
            Log.d("MyTag","paquets ok")
            val listInfoByNatureInPackage = DataSource.mapOfPackages[idOfAPackage]?.wordsAndInfo?.get(currentWord)
            if (listInfoByNature != null) {
                for (info in listInfoByNature){
                    val definitions = info.definitions.keys.toList()
                    if (listInfoByNatureInPackage != null) {
                        val elementWithNature = listInfoByNatureInPackage.find { it.nature == info.nature }
                        if (elementWithNature != null) {
                            mapOfSelectedDefinition = definitions.associateBy({it},{elementWithNature.definitions.containsKey(it)}) as MutableMap<String, Boolean>
                        }
                        else{
                            mapOfSelectedDefinition = definitions.associateBy({it},{false}) as MutableMap<String, Boolean>
                        }
                    }
                    selectedDefinitionByNature.put(
                        info.nature,
                        typeSelectedInfoForLearning(
                            isNatureSelected = mapOfSelectedDefinition.values.any{it},
                            definitionSelected = mapOfSelectedDefinition )
                    )
                }
            }
        }
        else{
            if (listInfoByNature != null) {
                Log.d("MyTag","pas de paquets crées")
                for (info in listInfoByNature){
                    val definitions = info.definitions.keys.toList()
                    mapOfSelectedDefinition = definitions.associateBy({it},{true}) as MutableMap<String, Boolean>
                    selectedDefinitionByNature.put(
                        info.nature,
                        typeSelectedInfoForLearning(
                            isNatureSelected = true,
                            definitionSelected = mapOfSelectedDefinition )
                    )
                }
            }
        }
    }

    fun findPackageWithWord() : Int {
        val packages = DataSource.mapOfPackages
        var idWord : Int = -1
        for(id in packages.keys.toList()){
            if(packages[id]?.wordsAndInfo?.keys?.toList()?.contains(currentWord) == true){
                idWord = id
            }
        }
    return idWord
    }

    //Ajoute toutes les définitions
    fun onCheckedNature(nature : String) {
        val valueDef = selectedDefinitionByNature[nature]?.definitionSelected?.toMutableMap()
        valueDef?.replaceAll { _, _ -> true }
        var newValue = valueDef?.let { typeSelectedInfoForLearning(isNatureSelected = true, definitionSelected = it) }
        if(selectedDefinitionByNature !=null){
            if( newValue != null){
                selectedDefinitionByNature[nature]= newValue
            }
        }
    }

    //Ajoute seulement une définition et ajoute que la nature est sélectionnée
    fun onCheckedDefinition (def : String, nature : String){
        val actualValue = selectedDefinitionByNature[nature]?.definitionSelected?.toMutableMap()
        val isSelected = actualValue?.get(def)
        actualValue?.set(def, !isSelected!!)
        if(selectedDefinitionByNature !=null){
            val newValue = actualValue?.let { typeSelectedInfoForLearning(isNatureSelected = true, definitionSelected = it) }
            if(newValue !=null){
                selectedDefinitionByNature[nature] = newValue
                Log.d("MyTag"," onCheckedDefinition : $newValue")

            }
        }
    }


    //Cette fonction créer le type à mettre pour savoir quelles natures et quelles définitions ont été choisies
    fun setIntrestingInfo() : MutableList<InformationWordByNature> {
        var listInfoWordByNature: MutableList<InformationWordByNature> = mutableListOf()
        var listOfDefinitions: MutableList<String>
        selectedDefinitionByNature?.forEach { (nature, value) ->
            listOfDefinitions = mutableListOf()
            for (infoByNature in mapOfWords[currentWord]?.infoWordByNature!!) {
                if (value.isNatureSelected) {
                    value.definitionSelected.forEach { (def, isSelected) ->
                        if (isSelected) {
                            listOfDefinitions.add(def)
                        }
                    }
                    var mapOfSelectedDefinitions =
                        extractPairs(infoByNature.definitions, listOfDefinitions)
                    listInfoWordByNature.add(
                        InformationWordByNature(
                            nature = nature,
                            definitions = mapOfSelectedDefinitions,
                            derives = null,
                            synonymes = null
                        )
                    )
                }
            }
        }
        return listInfoWordByNature
    }

    fun isThereAnyDefinitionSelected():Boolean{
        val totalSelectedDefinitions = selectedDefinitionByNature.values.sumOf { info ->
            info.definitionSelected.values.count { it }
        }
        val isAtLeastOneDefinitionSelected = (totalSelectedDefinitions >= 1)
        return isAtLeastOneDefinitionSelected
    }

    fun onConfirmationDefinition(){
        Log.d("MyTag","Début de la fonction onConfirmationDefinition")
        _uiState.update { currentState->
            val updatedPaquets = currentState.paquets.toMutableMap()
            for(id in updatedPaquets.keys.toList()){
                if(updatedPaquets[id]?.wordsAndInfo?.keys?.toList()?.contains(currentWord) == true){
                    val newWordsAndInfo = updatedPaquets[id]?.wordsAndInfo?.toMutableMap()
                    newWordsAndInfo?.set(currentWord, setIntrestingInfo())
                    val newpaquetAttributes = paquetAttributes(name = updatedPaquets[id]?.name, wordsAndInfo =newWordsAndInfo)
                    updatedPaquets[id]=newpaquetAttributes
                }
            }
            currentState.copy(
                paquets = updatedPaquets.toMap()
            )
        }
        DataSource.mapOfPackages = uiState.value.paquets as MutableMap<Int, paquetAttributes>
    }

    fun supressOrDoNothingPackage(id: Int) {
        _uiState.update { currentState ->
            val updatedPaquets = currentState.paquets.toMutableMap()
            val currentPackageAttributes = currentState.paquets[id]
            if (currentPackageAttributes != null) {
                val words = currentPackageAttributes.wordsAndInfo?.keys?.toList()
                if (words != null) {
                    if (words.contains(currentWord)) {
                        val newMap = currentPackageAttributes.wordsAndInfo?.toMutableMap()
                        newMap?.remove(currentWord)
                        val newPackageAttributes = paquetAttributes(
                            wordsAndInfo = newMap,
                            name = currentPackageAttributes.name
                        )
                        updatedPaquets[id] = newPackageAttributes
                    }
                }
            }
            currentState.copy(
                paquets = updatedPaquets.toMap()
            )
        }
    }

    fun addWordToPackage(id: Int) {
        _uiState.update { currentState ->
            val updatedPaquets = currentState.paquets.toMutableMap()
            //Ajout d'un mot dans un paquet s'il est selectionné
            val currentPackageAttributes = currentState.paquets[id]
            if (currentPackageAttributes != null) {
                //words in the package
                val words = currentPackageAttributes.wordsAndInfo?.keys?.toList()
                if (words != null) {
                    if (!words.contains(currentWord)) {
                        val newMap = currentPackageAttributes.wordsAndInfo?.toMutableMap()
                        mapOfWords[currentWord]?.infoWordByNature?.let {
                            newMap!!.put(
                                currentWord,
                                it
                            )
                        }
                        val newPackageAttributes = paquetAttributes(
                            wordsAndInfo = newMap,
                            name = currentPackageAttributes.name
                        )
                        updatedPaquets[id] = newPackageAttributes
                    }
                }
            }
            currentState.copy(
                paquets = updatedPaquets.toMap()
            )
        }
    }


        fun addWordToPackages() {
            for (id in selectedPackage.keys.toList()) {
                //Ajout d'un mot dans un paquet s'il est selectionné
                if (selectedPackage[id] == true) {
                    if (id == 0) {
                        addToMyWords()
                    }
                    addWordToPackage(id)
                }
                //Vérifie si le mot était dans les paquets non sélectionnés, si oui le supprime de ces paquets
                else {
                    if (id == 0) {
                        deleteOfMyWords()
                    }
                    supressOrDoNothingPackage(id)
                }
            }
            DataSource.mapOfPackages = uiState.value.paquets as MutableMap<Int, paquetAttributes>
        }

        fun deleteAPackage() {
            Log.d("MonTag", "La valeur de la variable est Delete : $currentPackageId")
            if (currentPackageId != 0) {
                DataSource.mapOfPackages.remove(currentPackageId)
                _uiState.update { currentState ->
                    val updatedPaquets = currentState.paquets.toMutableMap()
                    updatedPaquets.remove(currentPackageId)
                    currentState.copy(
                        paquets = updatedPaquets.toMap()
                    )
                }
            }
        }

        fun changePackageName() {
            if (currentPackageId != 0) {
                var newName = newPackageName.trim()
                var words = DataSource.mapOfPackages[currentPackageId]?.wordsAndInfo
                DataSource.mapOfPackages[currentPackageId] =
                    paquetAttributes(name = newName, wordsAndInfo = words)
                _uiState.update { currentState ->
                    val updatedPaquets = currentState.paquets.toMutableMap()
                    updatedPaquets[currentPackageId] =
                        paquetAttributes(name = newName, wordsAndInfo = words)
                    currentState.copy(
                        paquets = updatedPaquets.toMap()
                    )
                }
                reinitNewPackageName()
            }
        }

        ///////////////////////////////////////Bouton favoris ://///////////////////////////////////////

        fun onAddButtonClickedList(word: String) {
            updateCurrentWord(word)
            if (isWordInMyWords(currentWord)) {
                return deleteOfMyWords()
            } else {
                addToMyWords()
            }

        }

        private fun addToMyWords() {
            mapOfWords[currentWord]?.isFavorite = true
            addWordToPackage(0)
        }


        private fun deleteOfMyWords() {
            mapOfWords[currentWord]?.isFavorite = false
            supressOrDoNothingPackage(0)
        }

        //Historique
        fun addToHistory(word: String) {
            DataSource.listHistory.add(currentWord)
            _uiState.update { currentState ->
                currentState.copy(
                    favorites = currentState.favorites,
                    wordsInHistory = listOf(word) + currentState.wordsInHistory.orEmpty()
                )
            }
        }

        fun updateSearchedWord(word: String) {
            searchWord = word
        }

        fun autoCompletion() {
            listForCompletion.value =
                mapOfWords.filterKeys { it.startsWith(searchWord) }.keys.toList()
        }

        fun reinitListCompletion() {
            listForCompletion.value = emptyList()
        }

        fun updateCurrentWord(word: String) {
            currentWord = word
        }


        fun definitionOfTheWord(word: String) {
            infoDefCurrentWord = mapOfWords[word]?.infoWordByNature!!
        }

        fun isWordInDico(): Boolean {
            var isWordInDico = mapOfWords.containsKey(currentWord.trim())
            //var isWordInDico = true
            return isWordInDico
        }

        fun isWordInMyWords(word: String): Boolean {
            return mapOfWords[word]?.isFavorite ?: false
        }

        fun onKeyboardSearch() {
            updateCurrentWord(searchWord.trim())
            if (isWordInDico()) {
                addToHistory(currentWord)
                definitionOfTheWord(currentWord)
                searchWord = ""
            } else {
                currentWord = ""
            }
        }

        fun onWordClicked(word: String) {
            updateCurrentWord(word)
            definitionOfTheWord(currentWord)
            addToHistory(word)
        }

}






