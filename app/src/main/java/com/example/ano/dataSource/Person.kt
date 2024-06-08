package com.example.ano.dataSource

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Définition de la classe de données Person
@Serializable
data class Person(
    val name: String,
    val age: Int,
    val email: String
)

fun main() {
    // Lire le contenu du fichier JSON
    val jsonString = """
        {
          "name": "John",
          "age": 30,
          "email": "john@example.com"
        }
    """.trimIndent() // Pour un exemple en ligne, vous pouvez remplacer cette ligne par la lecture du contenu du fichier JSON

    // Bloc de désérialisation
    val person = Json.decodeFromString<Person>(jsonString)

    // Utilisez l'objet Person désérialisé
    println("Name: ${person.name}")
    println("Age: ${person.age}")
    println("Email: ${person.email}")
}



