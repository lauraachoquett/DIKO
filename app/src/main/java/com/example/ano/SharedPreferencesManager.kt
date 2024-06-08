package com.example.ano
import android.content.Context
import com.example.ano.dataSource.DataSource
import com.example.ano.dataSource.DataSource.listFavorites
import com.example.ano.dataSource.DataSource.listHistory

object SharedPreferencesManager {
    private const val PREF_NAME = "myPrefs"
    fun saveHistoryListAndFavoriteList(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var favoritesString : String?
        // Convertir les listes en chaînes de caractères
        if (listFavorites.isNotEmpty()) {
            favoritesString = listFavorites.joinToString(separator = ",")
        }
        else{
            favoritesString = null
        }
        editor.putString("favorites", favoritesString)
        val historyString = listHistory.joinToString(separator = ",")
        editor.putString("history", historyString)
        // Enregistrer les chaînes dans les préférences partagées
        editor.apply()
    }


    // Sauvegarder les isFavorite lors de la fermeture de l'application pour mapOfWords
    fun saveFavorites(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        DataSource.mapOfWords.forEach { (key, value) ->
            editor.putBoolean("isFavorite_$key", value.isFavorite)
        }
        editor.apply()
    }

    fun savePackages(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Enregistrement des ID de paquets
        val listOfIdPackages = DataSource.mapOfPackages.keys.joinToString(separator = ",")
        editor.putString("idOfPackages", listOfIdPackages)

        DataSource.mapOfPackages.forEach { (key, value) ->
            // Enregistrement des mots dans chaque paquet
            editor.putString("wordsInPackage_$key", value.wordsAndInfo?.keys?.joinToString(separator = ","))
            // Enregistrement des noms de paquets
            editor.putString("nameOfPackage_$key", value.name)

            value.wordsAndInfo?.forEach { (word, info) ->
                val natures = mutableListOf<String>()
                val definitions = mutableListOf<String>()

                info.forEach { infoByNature ->
                    val nature = infoByNature.nature
                    natures.add(nature)

                    infoByNature.definitions.keys.forEach { def ->
                        // Enregistrement des définitions sélectionnées
                        definitions.add(def)
                    }

                    val stringOfDefinitions = definitions.joinToString(separator = ",")
                    editor.putString("defSelected_$word $nature", stringOfDefinitions)
                }

                val stringOfNatures = natures.joinToString(separator = ",")
                // Enregistrement des différentes natures du mot
                editor.putString("naturesOf_$word", stringOfNatures)
            }
        }

        editor.apply()
    }


    fun clearSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
