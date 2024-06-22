package com.example.ano.model

import com.example.ano.dataSource.wordAttributes

class AnoAnki {

    interface CardState {
        fun onEncore()
        fun onDifficile()
        fun onBien()
        fun onFacile()
    }

    class LearningCard(private val card: Card) : CardState {
        var step : Int = 0
        override fun onEncore() {
            card.delay = (1*60*1000L)
            step = 0
        }
        override fun onDifficile() {
            step = step
            card.delay = (1*60*1000L + card.delay!!)/2

        }
        override fun onBien() {
            card.delay = delayBien(step)
            step+=1
        }
        override fun onFacile() {
            card.state = ReviewCard(card) // Transition vers la phase de révision
        }
        private fun scheduleNextReview(delayInMillis: Long) {
            // Logique de planification (à implémenter)
        }

        private fun delayBien(step : Int) : Long {
            if (step ==0){
                return 10*60*1000L
            }
            else if(step ==1){
                return 3600*24*1000L
            }
            else{
                card.state = ReviewCard(card) // Transition vers la phase de révision
                return 0
            }
        }
    }

    class ReviewCard(private val card: Card) : CardState {

        override fun onEncore() {
            card.state = LearningCard(card) // Transition vers la phase de révision
        }
        override fun onDifficile() {
            card.ease *= 0.85
            card.delay = (1.2 * card.delay).toLong()

        }

        override fun onBien() {
            card.ease=card.ease
            card.delay = (card.ease *card.delay).toLong()
        }

        override fun onFacile() {
            card.ease = card.ease*1.15
            card.delay = (card.delay * 1.3).toLong()
        }
        private fun scheduleNextReview(delayInMillis: Long) {
            // Logique de planification (à implémenter)
        }
    }

    class Card(val wordAttributes: wordAttributes) {
        var state: CardState = LearningCard(this)// L'état initial est "LearningCard"
        // Propriétés pour accéder au delay et ease
        // Properties to access delay and ease
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
            state.onEncore()
        }
        fun onDifficile() {
            state.onDifficile()
        }
        fun onBien() {
            state.onBien()
        }
        fun onFacile() {
            state.onFacile()
        }


    }










}