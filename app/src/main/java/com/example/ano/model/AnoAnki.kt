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
        var currentTimeOfTheLastUpdate : Long = 0L
        var delayBeforeNextCard : Long = 0L
        var minDelayCard : Long = ReviewTime.LONG_TERM.delayInMillis

        fun updateLastUpdate(currentTime : Long){
            currentTimeOfTheLastUpdate = currentTime
        }

        fun updateMinDelayCard(delay : Long){
            if (delay< minDelayCard){
                minDelayCard=delay
            }
        }

        fun calculateDelayBeforeNextCard() {
            delayBeforeNextCard = -(System.currentTimeMillis() - currentTimeOfTheLastUpdate - minDelayCard)

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
            updateLastUpdate(System.currentTimeMillis())
            updateMinDelayCard(delayInMillis)
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
                queue.add(word)
                Log.d("ReviewReceiver", "Manually added word: $word to packageId: $packageId")
                Log.d("ReviewReceiver", "Updated reviewQueueMap: $reviewQueueMap")
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
