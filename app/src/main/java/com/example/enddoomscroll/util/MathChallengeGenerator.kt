package com.example.enddoomscroll.util

import kotlin.random.Random

data class MathChallenge(
    val problem: String,
    val answer: Int,
    val difficulty: String
)

class MathChallengeGenerator {
    
    fun generateChallenge(difficulty: String): MathChallenge {
        return when (difficulty.lowercase()) {
            "easy" -> generateEasyChallenge()
            "hard" -> generateHardChallenge()
            else -> generateMediumChallenge()
        }
    }
    
    private fun generateEasyChallenge(): MathChallenge {
        val operations = listOf("+", "-", "*", "/")
        val operation = operations.random()
        
        return when (operation) {
            "+" -> {
                val a = Random.nextInt(1, 10)
                val b = Random.nextInt(1, 10)
                MathChallenge("$a + $b = ?", a + b, "Easy")
            }
            "-" -> {
                val a = Random.nextInt(5, 20)
                val b = Random.nextInt(1, a)
                MathChallenge("$a - $b = ?", a - b, "Easy")
            }
            "*" -> {
                val a = Random.nextInt(1, 10)
                val b = Random.nextInt(1, 10)
                MathChallenge("$a × $b = ?", a * b, "Easy")
            }
            "/" -> {
                val b = Random.nextInt(2, 10)
                val result = Random.nextInt(1, 10)
                val a = b * result
                MathChallenge("$a ÷ $b = ?", result, "Easy")
            }
            else -> generateEasyChallenge()
        }
    }
    
    private fun generateMediumChallenge(): MathChallenge {
        val operations = listOf("+", "-", "*")
        val operation = operations.random()
        
        return when (operation) {
            "+" -> {
                val a = Random.nextInt(10, 50)
                val b = Random.nextInt(10, 50)
                MathChallenge("$a + $b = ?", a + b, "Medium")
            }
            "-" -> {
                val a = Random.nextInt(20, 100)
                val b = Random.nextInt(10, a)
                MathChallenge("$a - $b = ?", a - b, "Medium")
            }
            "*" -> {
                val a = Random.nextInt(2, 15)
                val b = Random.nextInt(2, 15)
                MathChallenge("$a × $b = ?", a * b, "Medium")
            }
            else -> generateMediumChallenge()
        }
    }
    
    private fun generateHardChallenge(): MathChallenge {
        val challengeType = Random.nextInt(0, 3)
        
        return when (challengeType) {
            0 -> {
                // Two operations
                val a = Random.nextInt(10, 30)
                val b = Random.nextInt(5, 20)
                val c = Random.nextInt(5, 20)
                val result = a + b - c
                MathChallenge("$a + $b - $c = ?", result, "Hard")
            }
            1 -> {
                // Larger multiplication
                val a = Random.nextInt(10, 25)
                val b = Random.nextInt(5, 15)
                MathChallenge("$a × $b = ?", a * b, "Hard")
            }
            else -> {
                // Division with larger numbers
                val b = Random.nextInt(5, 15)
                val result = Random.nextInt(5, 20)
                val a = b * result
                MathChallenge("$a ÷ $b = ?", result, "Hard")
            }
        }
    }
}

