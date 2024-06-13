package com.example.ano.model

import com.example.ano.dataSource.DataSource
import com.example.ano.dataSource.paquetAttributes


data class UiState(
    val favorites: List<String>? = DataSource.listFavorites.toList().reversed(),
    val wordsInHistory: List<String>? = DataSource.listHistory.toList().reversed(),
    val paquets : Map<Int, paquetAttributes> = DataSource.mapOfPackages,
    //Clé : nom du paquet
    // Valeurs : Map<String,InfoToDisplay>

)
