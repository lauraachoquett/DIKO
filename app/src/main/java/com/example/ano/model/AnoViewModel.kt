package com.example.ano.model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.ano.dataSource.DataSource
import com.example.ano.dataSource.DataSource.mapOfPackages
import com.example.ano.dataSource.DataSource.mapOfWords
import com.example.ano.dataSource.InformationWordByNature
import com.example.ano.dataSource.extractPairs
import com.example.ano.dataSource.paquetAttributes
import com.example.ano.dataSource.wordAttributes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class typeSelectedInfoForLearning (
    val isNatureSelected :Boolean,
    val definitionSelected : Map<String,Boolean>
)

class AnoViewModel() : ViewModel(){

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

    var nowMoreCardToShow : Boolean = true

    var totalLearningCount = 0
    var totalReviewCount = 0

    //Première clé : nature
    lateinit var selectedDefinitionByNature : MutableMap<String,typeSelectedInfoForLearning>

    lateinit var currentCard : AnoAnki.Card


    ///////////////////////////////////////////IdGenerator pour les paquets//////////////////////

    companion object IdGenerator {
        var currentId = DataSource.currentId

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
            wordOnLearningPackageScreen = getNextReviewCard(currentPackageId)
            val condition1 = (wordOnLearningPackageScreen != "")
            if (condition1 ) {
                nowMoreCardToShow = false
                infoDefCurrentWord = uiState.value.paquets[currentPackageId]?.mapWordToCard?.get(wordOnLearningPackageScreen)!!.wordAttributes.listInfoWordByNature!!
                if (wordOnLearningPackageScreen != null) {
                    updateCurrentWord(wordOnLearningPackageScreen)
                }
                updateCurrentCard()
            }
            else{
                nowMoreCardToShow = true
            }
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
                this[id] = paquetAttributes(mapWordToCard = mapOf(),name =name)
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
        val wordsInThePackage = uiState.value.paquets[currentPackageId]?.mapWordToCard?.keys?.toList()
        val size = wordsInThePackage?.size
        if(size ==null || size ==0){
            return false
        }
        else{
            return true
        }
    }


    fun wordToDisplayInAPackage(index : Int ){
        // Appel de la fonction correspondant à l'index
        when (index) {
            0 -> onEncore()
            1 -> onDifficile()
            2 -> onBien()
            3 -> onFacile()

        }
        wordOnLearningPackageScreen = getNextReviewCard(currentPackageId)
        Log.d("Anki","current word on package : $wordOnLearningPackageScreen")
        var condition1 = wordOnLearningPackageScreen != ""
        Log.d("Anki","condition 1 : $condition1")
        if (condition1 ) {
            Log.d("Anki","not empty")
            nowMoreCardToShow = false
            infoDefCurrentWord = uiState.value.paquets[currentPackageId]?.mapWordToCard?.get(wordOnLearningPackageScreen)!!.wordAttributes.listInfoWordByNature!!
            updateCurrentWord(wordOnLearningPackageScreen)
            updateCurrentCard()
            Log.d("Anki","current word après le if : $currentWord")
        }
        else{
            Log.d("Anki","empty")
            nowMoreCardToShow = true
        }
    }

    fun addWordToReviewQueueMap(){
        if (nowMoreCardToShow){
            return
        }
        else{
            AnoAnki.ReviewReceiver.addWordToReviewQueue(currentPackageId,currentWord)
        }
    }

    fun calculateDelayBeforeNextCard(){
        AnoAnki.calculateDelayBeforeNextCard(currentPackageId)
    }

    fun getNextReviewCard(packakeId: Int): String {
        val print = AnoAnki.ReviewReceiver.reviewQueueMap[packakeId]
        Log.d("Anki","current queue : $print")
        val cond1 = isReviewQueueEmpty(packakeId) == false
        Log.d("Anki","isReviewQueueNotEmpty $cond1")
        return if(cond1) {
            val word =  AnoAnki.ReviewReceiver.reviewQueueMap[packakeId]!!.poll()
            val wordStillInThePackage = uiState.value.paquets[currentPackageId]?.mapWordToCard?.containsKey(word) == true
            if(wordStillInThePackage){
                word
            } else{
                ""
            }
        } else{
            ""
        }
    }

    fun isReviewQueueEmpty(packakeId: Int): Boolean? {
        var size = AnoAnki.ReviewReceiver.reviewQueueMap[packakeId]?.size
        Log.d("Anki","size queuemap : $size")
        if (size != null) {
            return size <= 0
        }
        else{
            return true
        }
    }

    fun randomChoiceWordToDisplay(){
        Log.d("wordError","nouveau mot !!!!")
        val paquetAttributes =uiState.value.paquets[currentPackageId]
        if (paquetAttributes != null) {
            if(paquetAttributes.mapWordToCard != null ){
                val listOfWordsInThePackage= paquetAttributes.mapWordToCard!!.keys.toList()
                if(listOfWordsInThePackage != null){
                    val randomIndex = listOfWordsInThePackage?.let { Random.nextInt(it.size) }
                    wordOnLearningPackageScreen = listOfWordsInThePackage[randomIndex!!]
                    infoDefCurrentWord = uiState.value.paquets[currentPackageId]?.mapWordToCard?.get(wordOnLearningPackageScreen)!!.wordAttributes.listInfoWordByNature!!
                    updateCurrentWord(wordOnLearningPackageScreen)
                    updateCurrentCard()
                }
            }
        }
    }

    fun initSelectedPackage() {
        val paquets = uiState.value.paquets
        selectedPackage = mutableMapOf() // Initialisez selectedPackage
        for (id in paquets.keys.toList()) {
            val words = paquets[id]?.mapWordToCard?.keys?.toList()
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
            val listInfoByNatureInPackage = DataSource.mapOfPackages[idOfAPackage]?.mapWordToCard?.get(currentWord)?.wordAttributes?.listInfoWordByNature
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
            if(packages[id]?.mapWordToCard?.keys?.toList()?.contains(currentWord) == true){
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
    fun setIntrestingInfo(context: Context,id:Int): AnoAnki.Card {
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
        var card = AnoAnki.Card(wordAttributes(word = currentWord,listInfoWordByNature = listInfoWordByNature), context =context,id)
        return card
    }

    fun isThereAnyDefinitionSelected():Boolean{
        val totalSelectedDefinitions = selectedDefinitionByNature.values.sumOf { info ->
            info.definitionSelected.values.count { it }
        }
        val isAtLeastOneDefinitionSelected = (totalSelectedDefinitions >= 1)
        return isAtLeastOneDefinitionSelected
    }

    fun onConfirmationDefinition(context: Context){
        Log.d("MyTag","Début de la fonction onConfirmationDefinition")
        _uiState.update { currentState->
            val updatedPaquets = currentState.paquets.toMutableMap()
            for(id in updatedPaquets.keys.toList()){
                if(updatedPaquets[id]?.mapWordToCard?.keys?.toList()?.contains(currentWord) == true){
                    val newmapWordToCard = updatedPaquets[id]?.mapWordToCard?.toMutableMap()
                    newmapWordToCard?.set(currentWord, setIntrestingInfo(context,id))
                    val newpaquetAttributes = paquetAttributes(name = updatedPaquets[id]?.name, mapWordToCard =newmapWordToCard)
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
                val words = currentPackageAttributes.mapWordToCard?.keys?.toList()
                if (words != null) {
                    if (words.contains(currentWord)) {
                        val newMap = currentPackageAttributes.mapWordToCard?.toMutableMap()
                        newMap?.remove(currentWord)
                        val newPackageAttributes = paquetAttributes(
                            mapWordToCard = newMap,
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

    fun countCardsByType(paquet: paquetAttributes): Pair<Int, Int> {
        var learningCount = 0
        var reviewCount = 0

        // Vérifie si mapWordToCard n'est pas null
        paquet.mapWordToCard?.let { map ->
            for (card in map.values) {
                when (card.state) {
                    is AnoAnki.LearningCard -> learningCount++
                    is AnoAnki.ReviewCard -> reviewCount++
                }
            }
        }

        return Pair(learningCount, reviewCount)
    }

    fun countCardsByTypeAll() {
        totalReviewCount=0
        totalLearningCount=0
        // Parcourir tous les paquets dans mapOfPackages
        for (paquet in mapOfPackages.values) {
            // Récupérer le nombre de cartes par type pour chaque paquet
            val (learningCount, reviewCount) = countCardsByType(paquet)
            Log.d("MyTag", "Nombre de cartes en cours d'apprentissage : $learningCount")
            Log.d("MyTag", "Nombre de cartes à réviser : $reviewCount")
            // Ajouter les résultats aux compteurs globaux
            totalLearningCount += learningCount
            totalReviewCount += reviewCount
        }
    }

    fun addWordToPackage(id: Int,context: Context) {
        Log.d("Anki","call addWordToPackage")
        _uiState.update { currentState ->
            val updatedPaquets = currentState.paquets.toMutableMap()
            //Ajout d'un mot dans un paquet s'il est selectionné
            val currentPackageAttributes = currentState.paquets[id]
            if (currentPackageAttributes != null) {
                //words in the package
                val words = currentPackageAttributes.mapWordToCard?.keys?.toList()
                if (words != null) {
                    if (!words.contains(currentWord)) {
                        val newMap = currentPackageAttributes.mapWordToCard?.toMutableMap()

                        mapOfWords[currentWord]?.infoWordByNature?.let {
                            var card = AnoAnki.Card(wordAttributes(word = currentWord,listInfoWordByNature= it),context=context,id)
                            newMap!!.put(
                                currentWord,
                                card
                            )
                        }
                        val newPackageAttributes = paquetAttributes(
                            mapWordToCard = newMap,
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
        AnoAnki.ReviewReceiver.addWordToReviewQueue(packageId = id,word = currentWord)
    }


        fun addWordToPackages(context: Context) {
            for (id in selectedPackage.keys.toList()) {
                //Ajout d'un mot dans un paquet s'il est selectionné
                if (selectedPackage[id] == true) {
                    if (id == 0) {
                        addToMyWords(context)
                    }
                    else{
                        addWordToPackage(id,context)
                    }
                }
                //Vérifie si le mot était dans les paquets non sélectionnés, si oui le supprime de ces paquets
                else {
                    if (id == 0) {
                        deleteOfMyWords()
                    }
                    AnoAnki.ReviewReceiver.deleteWord(id,currentWord)
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
                var words = DataSource.mapOfPackages[currentPackageId]?.mapWordToCard
                DataSource.mapOfPackages[currentPackageId] =
                    paquetAttributes(name = newName, mapWordToCard = words)
                _uiState.update { currentState ->
                    val updatedPaquets = currentState.paquets.toMutableMap()
                    updatedPaquets[currentPackageId] =
                        paquetAttributes(name = newName, mapWordToCard = words)
                    currentState.copy(
                        paquets = updatedPaquets.toMap()
                    )
                }
                reinitNewPackageName()
            }
        }

        ///////////////////////////////////////Bouton favoris ://///////////////////////////////////////

        fun onAddButtonClickedList(word: String,context: Context) {
            updateCurrentWord(word)
            if (isWordInMyWords(currentWord)) {
                return deleteOfMyWords()
            } else {
                addToMyWords(context)
            }

        }

        private fun addToMyWords(context: Context) {
            Log.d("Anki","call addToMyWords")
            mapOfWords[currentWord]?.isFavorite = true
            addWordToPackage(0,context)

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
            mapOfWords.filterKeys { it.startsWith(searchWord) }
                .keys
                .toList()
                .take(8)
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
    ////////////////////////////////////////Logique de révision/////////////////////////////////////////
    fun updateCurrentCard(){
        Log.d("wordError","current word before update card $currentWord")

        currentCard = DataSource.mapOfPackages[currentPackageId]?.mapWordToCard?.get(currentWord)!!
    }

    fun onEncore(){
        Log.d("wordError","Encore nouveau mot !!!!")
        currentCard.onEncore()
    }

     fun onDifficile(){
         Log.d("wordError","onDifficle nouveau mot !!!!")
         currentCard.onDifficile()
     }

    fun onBien(){
        Log.d("wordError","Bien nouveau mot !!!!")

        currentCard.onBien()
    }

    fun onFacile(){
        Log.d("wordError","Facile nouveau mot !!!!")

        currentCard.onFacile()
    }





}









