package com.example.ano.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.ano.dataSource.wordAttributes
import java.util.LinkedList
import java.util.Queue

class AnoAnki {
    companion object{
        val delimiters = "///"
        var currentTimeOfTheLastUpdateByPackage :  MutableMap<Int,Long?> = mutableMapOf()
        var delayBeforeNextCardByPackage :  MutableMap<Int,Long?> = mutableMapOf()
        var minDelayCardByPackage :  MutableMap<Int,Long?> = mutableMapOf()

        fun updateLastUpdate(currentTime : Long,id : Int){
            currentTimeOfTheLastUpdateByPackage[id] = currentTime
        }

        fun updateMinDelayCard(delay : Long, id: Int){
            minDelayCardByPackage[id] = ReviewTime.LONG_TERM.delayInMillis
            if (delay< minDelayCardByPackage[id]!!){
                minDelayCardByPackage[id]=delay
            }
        }

        fun calculateDelayBeforeNextCard(id : Int) {
            delayBeforeNextCardByPackage[id] = -(System.currentTimeMillis() - currentTimeOfTheLastUpdateByPackage[id]!! - minDelayCardByPackage[id]!!)
            if (delayBeforeNextCardByPackage[id]!! < 0.toLong()){
                delayBeforeNextCardByPackage[id] = 0
            }
        }


        fun restoreDelay(context: Context) {
            val sharedPreferences = context.getSharedPreferences("DelayPrefs", Context.MODE_PRIVATE)
            val idOfPackages = sharedPreferences.getString("idOfPackages",null)
            val listOfIdPackages = idOfPackages?.split(delimiters)?.toMutableList() ?: mutableListOf()
            currentTimeOfTheLastUpdateByPackage = listOfIdPackages.associateBy(
                { it.toInt() },
                {sharedPreferences.getString("lastupdate_$it",null)?.toLong()}
                ).toMutableMap()

            minDelayCardByPackage = listOfIdPackages.associateBy(
                { it.toInt() },
                {sharedPreferences.getString("mindelay_$it",null)?.toLong()}
            ).toMutableMap()
        }


    }



    enum class ReviewTime(val delayInMillis: Long){
        IMMEDIATE(1000L *1 *60), //1 minutes
        SHORT_TERM(1000L*1*3600), //1 heure
        LONG_TERM(1000L*1*3600*24), //1 jour
    }

    interface CardState {
        fun onEncore()
        fun onDifficile()
        fun onBien()
        fun onFacile()
    }

    class LearningCard(private val card: Card, private val packageId: Int) : CardState {
        var step: Int = 0

        override fun onEncore() {
            card.delay = ReviewTime.IMMEDIATE.delayInMillis
            step = 0
            card.scheduleNextReview(card.delay, packageId)
        }

        override fun onDifficile() {
            card.delay = (ReviewTime.IMMEDIATE.delayInMillis+ card.delay) / 2
            card.scheduleNextReview(card.delay, packageId)
        }

        override fun onBien() {
            card.delay = delayBien(step)
            step += 1
            card.scheduleNextReview(card.delay, packageId)
        }

        override fun onFacile() {
            card.delay = ReviewTime.SHORT_TERM.delayInMillis
            card.state = ReviewCard(card, packageId)
            card.scheduleNextReview(card.delay, packageId)
        }

        private fun delayBien(step: Int): Long {
            return when (step) {
                0 -> 10 * ReviewTime.IMMEDIATE.delayInMillis
                1 -> ReviewTime.SHORT_TERM.delayInMillis
                else -> {
                    card.state = ReviewCard(card, packageId)
                    ReviewTime.LONG_TERM.delayInMillis
                }
            }
        }


    }

    class ReviewCard(private val card: Card, private val packageId: Int) : CardState {

        override fun onEncore() {
            card.state = LearningCard(card, packageId)
            card.scheduleNextReview(card.delay, packageId)
        }

        override fun onDifficile() {
            card.ease *= 0.85
            card.delay = (1.2 * card.delay).toLong()
            card.scheduleNextReview(card.delay, packageId)
        }

        override fun onBien() {
            card.delay = (card.ease * card.delay).toLong()
            card.scheduleNextReview(card.delay, packageId)
        }

        override fun onFacile() {
            card.ease *= 1.15
            card.delay = (card.delay * 1.3).toLong()
            card.scheduleNextReview(card.delay, packageId)
        }


    }


    class Card(val wordAttributes: wordAttributes, val context: Context,packageId: Int ) {
        var state: CardState = LearningCard(this, packageId ) // L'état initial est "LearningCard"
        // Propriétés pour accéder au delay et ease
        var delay: Long
            get() = wordAttributes.delay
            set(value) {
                wordAttributes.delay = value
            }

        var ease: Double
            get() = wordAttributes.ease
            set(value) {
                wordAttributes.ease = value
            }

        // Méthodes pour les boutons
        fun onEncore() {
            Log.d("Anki","Encore : $state")
            state.onEncore()
        }

        fun onDifficile() {
            Log.d("Anki","Difficle : $state")

            state.onDifficile()
        }

        fun onBien() {
            Log.d("Anki","Bien : $state")

            state.onBien()
        }

        fun onFacile() {
            Log.d("Anki","Facile : $state")

            state.onFacile()
        }

        fun scheduleNextReview(delayInMillis: Long, packageId: Int) {
            Log.d("Anki"," delai : $delayInMillis")
            var word = this.wordAttributes.word
            val alarmManager = this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this.context, ReviewReceiver::class.java).apply {
                putExtra("word", word)
                Log.d("Anki"," packageId in schedule: $packageId")
                putExtra("packageId", packageId)
            }
            // Ajoutez FLAG_IMMUTABLE ou FLAG_MUTABLE ici
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE // ou PendingIntent.FLAG_IMMUTABLE
            } else {
                0 // Pas besoin de spécifier pour les versions antérieures à S
            }
            val pendingIntent = PendingIntent.getBroadcast(this.context, this.wordAttributes.word.hashCode(), intent, flags)
            updateLastUpdate(System.currentTimeMillis(),packageId)
            updateMinDelayCard(delayInMillis,packageId)
            val triggerAtMillis = System.currentTimeMillis() + delayInMillis
            Log.d("Anki","delay:  $delayInMillis")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        
        
        
    }

    class ReviewReceiver : BroadcastReceiver() {
        companion object {
            val reviewQueueMap: MutableMap<Int, Queue<String>> = mutableMapOf()

            fun addWordToReviewQueue(packageId: Int, word: String) {
                val queue = reviewQueueMap.getOrPut(packageId) { LinkedList() }
                if(!queue.contains(word)){
                    queue.add(word)
                    Log.d("ReviewReceiver", "Manually added word: $word to packageId: $packageId")
                    Log.d("ReviewReceiver", "Updated reviewQueueMap: $reviewQueueMap")
                }

            }

            fun deleteWord(packageId : Int, word : String){
                val queue = reviewQueueMap.getOrPut(packageId) { LinkedList() }
                if(queue.contains(word)){
                    queue.remove(word)
                }
            }

            fun restoreReviewQueueMap(context: Context) {
                val sharedPreferences = context.getSharedPreferences("ReviewQueuePrefs", Context.MODE_PRIVATE)
                val allEntries = sharedPreferences.all

                // Vérifier si les SharedPreferences sont vides
                if (allEntries.isEmpty()) {
                    Log.d("ReviewQueueMap", "Aucune donnée à restaurer.")
                    return
                }

                allEntries.forEach { (key, value) ->
                    try {
                        if (value is Set<*>) {
                            // Log pour vérifier le type de valeur
                            Log.d("ReviewQueueMap", "Clé: $key, Valeur: $value")

                            // Convertir la valeur en LinkedList<String>
                            val queue = LinkedList<String>(value.map { it.toString() })

                            // Vérifier et convertir la clé en Int
                            val packageId = key.toIntOrNull()
                            if (packageId != null && !(queue.isEmpty())) {
                                reviewQueueMap[packageId] = queue
                                Log.d("ReviewQueueMap", "Restauré: $packageId -> $queue")
                            } else {
                                Log.e("ReviewQueueMap", "Clé invalide (non convertible en Int): $key")
                            }
                        } else {
                            Log.e("ReviewQueueMap", "Valeur non attendue pour la clé: $key, type: ${value?.javaClass?.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("ReviewQueueMap", "Erreur lors de la restauration de la clé $key: ${e.message}")
                    }
                }
            }

        }

        override fun onReceive(context: Context, intent: Intent) {
            val word = intent.getStringExtra("word")
            val packageId = intent.getIntExtra("packageId", -1)

            Log.d("ReviewReceiver", "Received intent with word: $word, packageId: $packageId")

            if (word != null && packageId >= 0) {
                val queue = reviewQueueMap.getOrPut(packageId) { LinkedList() }

                Log.d("ReviewReceiver", "Current queue before adding word: $queue")
                queue.add(word)
                Log.d("ReviewReceiver", "Updated queue after adding word: $queue")

                Log.d("ReviewReceiver", "Updated reviewQueueMap: $reviewQueueMap")
            } else {
                Log.e("ReviewReceiver", "Received invalid data: word = $word, packageId = $packageId")
            }
        }
    }





}
