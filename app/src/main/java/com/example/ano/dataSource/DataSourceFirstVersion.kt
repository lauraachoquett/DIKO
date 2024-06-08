package com.example.ano.dataSource

data class WordInfoF(
    var definitions: List<String>,
    var isFavorite : Boolean = false
)

object DataSourceFirstVersion {
    val mapOfWords: Map<String, WordInfoF> = mapOf(
        "badaud" to WordInfoF( listOf("Niais qui admire tout, s'amuse à tout, qui est d'une curiosité frivole",
            "Flâneur qui passe le temps en regardant tout ce qui lui semble extraordinaire ou nouveau"),),
        "affre" to WordInfoF(listOf("Grande peur, extrême frayeur, effroi")),
        "salvatrice" to WordInfoF(listOf("Celle qui sauve, qui libère")),
        "goguenard" to WordInfoF(listOf("Qui affecte la moquerie, la raillerie")),
        "ersatz" to WordInfoF(listOf("Produit de consommation destiné à remplacer un produit naturel devenu rare ; succédané","Imitation médiocre"))
    )
}