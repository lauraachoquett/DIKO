package com.example.ano.dataSource
import android.content.Context
import android.util.Log
import com.example.ano.R
import com.example.ano.model.AnoAnki
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates


@Serializable
data class InfoDefinitions(
    val lexique: String?,
    val exemples: List<String>,
)

@Serializable
data class InformationWordByNature(
    val nature: String,
    val definitions: Map<String, InfoDefinitions>,
    val synonymes: List<String>?,
    val derives: List<String>?,
)

@Serializable
data class InformationWordByNatureAndFavorite(
    val infoWordByNature : List<InformationWordByNature>,
    var isFavorite : Boolean = false
)

data class wordAttributes(
    var listInfoWordByNature: List<InformationWordByNature>?,
    var delay: Long = 60 * 1000L, // Default delay in milliseconds
    var ease: Double = 2.5
)


data class paquetAttributes(
    var name : String?,
    var mapWordToCard: Map<String, AnoAnki.Card>?,
)


object DataSource {
    // Initialisation de l'objet DataSource avec la mapOfWords
    lateinit var mapOfWords: Map<String, InformationWordByNatureAndFavorite>
    lateinit var listHistory: MutableList<String>
    lateinit var listFavorites : MutableList<String>
    lateinit var mapOfPackages: MutableMap<Int, paquetAttributes>
    var currentId by Delegates.notNull<Int>()

    // Initialisation de la mapOfWords à partir du fichier JSON
    fun loadJSONFromRaw(context: Context, resourceId: Int) {
        //Procédure pour sauvegarder les données :
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val delimiters = "///"
        //Initialisation de mapOfWords donc du dictionnaire
        val jsonString = context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        val map: Map<String, List<InformationWordByNature>> = Json.decodeFromString(jsonString)
        mapOfWords = map.mapValues { (key, value) ->
            // Récupérer isFavorite depuis SharedPreferences
            val isFavorite = sharedPreferences.getBoolean("isFavorite_$key", false)
            InformationWordByNatureAndFavorite(value, isFavorite)
        }

        //Initialisation de la liste des favoris et de l'historique
        // Charger les chaînes à partir des préférences partagées
        val historyString = sharedPreferences.getString("history", null)
        val favoritesString = sharedPreferences.getString("favorites", null)

        // Convertir les chaînes en listes
        listHistory = historyString?.split(delimiters)?.toMutableList() ?: mutableListOf()
        listFavorites =favoritesString?.split(delimiters)?.toMutableList() ?: mutableListOf()

        //Initialisation des paquets :
        val idOfPackages = sharedPreferences.getString("idOfPackages",null)
        if(idOfPackages.isNullOrEmpty()){
            var id = 0
            var wordsAndInfoFavorites = extractPairs(map, listFavorites);

            var wordsAndInfoMyWords : MutableMap<String,AnoAnki.Card>? = mutableMapOf()
            wordsAndInfoFavorites.keys.forEach{ word ->
                var wordAttributes = wordAttributes(listInfoWordByNature = wordsAndInfoFavorites[word])
                var card = AnoAnki.Card(wordAttributes)
                wordsAndInfoMyWords?.put(word,card)
            }

            mapOfPackages= mutableMapOf(id to paquetAttributes(mapWordToCard =  wordsAndInfoMyWords,name="Mes mots"))
        }
        else{
            val idOfPackages = sharedPreferences.getString("idOfPackages", null)
            val listOfIdPackages = idOfPackages?.split(delimiters)?.toMutableList() ?: mutableListOf()

            mapOfPackages = listOfIdPackages.associateBy(
                { it.toInt() },
                {
                    val stringOfWords = sharedPreferences.getString("wordsInPackage_$it", null)
                    val listOfWords = stringOfWords?.split(delimiters)?.toMutableList() ?: mutableListOf()
                    val wordsAndInfo = mutableMapOf<String, List<InformationWordByNature>>()

                    listOfWords.forEach { word ->
                        val listInfoWordByNature = mutableListOf<InformationWordByNature>()
                        val stringOfDifferentNature = sharedPreferences.getString("naturesOf_$word", null)
                        val listOfNature = stringOfDifferentNature?.split(delimiters)?.toMutableList() ?: mutableListOf()

                        listOfNature.forEach { nature ->
                            mapOfWords[word]?.infoWordByNature?.forEach { infoByNature ->
                                if (infoByNature.nature == nature && listInfoWordByNature.none {info-> info.nature == nature }) {
                                    val stringOfDefinition = sharedPreferences.getString("defSelected_$word $nature", null)
                                    val listOfDefinitions = stringOfDefinition?.split(delimiters)?.toMutableList() ?: mutableListOf()
                                    val mapOfSelectedDefinitions = extractPairs(infoByNature.definitions, listOfDefinitions)
                                    listInfoWordByNature.add(
                                        InformationWordByNature(
                                            nature = nature,
                                            definitions = mapOfSelectedDefinitions,
                                            derives = null,
                                            synonymes = null
                                        )
                                    )
                                    var show = infoByNature.definitions
                                    Log.d("LoadPackages", "Loaded definitions for $word $nature: $listOfDefinitions")
                                    Log.d("LoadPackages", "MapOfSelectedDefinition $word $nature: $mapOfSelectedDefinitions")
                                    Log.d("LoadPackages", "Show $word $nature: $show")
                                }
                            }
                        }
                        Log.d("LoadPackages", "List $word : $listInfoWordByNature")

                        wordsAndInfo[word] = listInfoWordByNature
                    }

                    val nameOfPackage = sharedPreferences.getString("nameOfPackage_$it", null)
                    var wordsAndInfoPackage : MutableMap<String,AnoAnki.Card>? = mutableMapOf()
                    wordsAndInfo.keys.forEach{ word ->
                        var wordAttributes = wordAttributes(listInfoWordByNature = wordsAndInfo[word])
                        var card = AnoAnki.Card(wordAttributes)
                        wordsAndInfoPackage?.put(word,card)
                    }
                    paquetAttributes(mapWordToCard = wordsAndInfoPackage, name = nameOfPackage)
                }
            ) as MutableMap<Int, paquetAttributes>
        }
    }
    fun loadCurrentId(context: Context) {
        val sharedPreferences = context.getSharedPreferences("IdGeneratorPrefs", Context.MODE_PRIVATE)
        currentId = sharedPreferences.getInt("currentId", 1) // 1 est la valeur par défaut
    }
}

fun <E> extractPairs(map: Map<String,E>, keys: List<String>): MutableMap<String, E> {
    val extractedPairs: MutableMap<String,E > = mutableMapOf()
    for (key in keys) {
        if (map.containsKey(key)) {
            map[key]?.let { extractedPairs.put(key, it) }
        }
    }
    Log.d("LoadPackages", "Extracted pairs: $extractedPairs")
    return extractedPairs
}

fun main() {
    val jsonString = R.raw.donnees.toString()
    //val jsonString = File("/Users/laura/Documents/Informatique/Android studio/xmlPython/donnees.json").readText()
    // Désérialisation du JSON en une Map<String, List<InformationWordByNature>>
    val map: Map<String, List<InformationWordByNature>> = Json.decodeFromString(jsonString)
    println(map)
}
