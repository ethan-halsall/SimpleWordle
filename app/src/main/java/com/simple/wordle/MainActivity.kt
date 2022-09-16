/*
 * MIT License
 *
 * Copyright (c) 2022 Ethan Halsall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.simple.wordle

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.simple.wordle.data.WordViewModel
import com.simple.wordle.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var keyboard: Keyboard

    private var row = 1
    private var guess = ""
    private var word = ""
    lateinit var rowViews : ArrayList<CardView>
    private lateinit var viewModel : WordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[WordViewModel::class.java]


        // Setup game
        setup()

        viewModel.wordLiveData
            .observe(
                this
            ) { w ->
                word = w.uppercase()
                println(word)
            }

        viewModel.message
            .observe(
                this
            ) { message ->

                if (message == "back") {
                    if (guess.isNotEmpty()) {
                        val cardView = rowViews[guess.length - 1]
                        val textView = cardView.getChildAt(0) as TextView
                        guess = guess.dropLast(1)
                        textView.text = ""
                    }
                } else if (message == "Enter") {
                    if (guess.length == 5) {
                        viewModel.isWord(guess.lowercase())
                    }

                } else if (guess.length < 5)  {
                    val cardView = rowViews[guess.length ]
                    val textView = cardView.getChildAt(0) as TextView
                    textView.text = message
                    guess += message
                }

        }

        viewModel.isWord
            .observe(
                this
            ) { isWord ->

                if (isWord){
                    val isMatch = checkWord(word, guess, rowViews)
                    if (isMatch){
                        createDialog(true)
                    }
                    row += 1
                    if(row <= 6) {
                        rowViews = getRowViews(row)
                        guess = ""
                    } else {
                        createDialog(false)
                    }
                }

            }

    }

    private fun setup(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel.generateWord()

        row = 1
        guess = ""
        keyboard = findViewById<View>(R.id.keyboard) as Keyboard
        keyboard.setViewModel(viewModel)

        rowViews = getRowViews(row)
    }

    private fun createDialog(win: Boolean){
        val title: String
        val message: String

        if (win){
            title = "Well Done"
            message = "You won, do you want to play again?"
        } else {
            title = "Better Luck, next time"
            message = "You lost, do you want to play again?"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                setup()
            }
            .setNegativeButton(android.R.string.no){
                _, _ ->
                keyboard.disableKeyboard()
            }
            .show()
    }

    private fun checkWord(word: String, guess: String, rowViews: ArrayList<CardView>) : Boolean{
        val letterOccurrence: HashMap<Char, Int> = HashMap()
        var numMatches = 0
        val matchedPos = BooleanArray(word.length)

        for (i in word.indices){
            letterOccurrence[word[i]] = letterOccurrence.getOrDefault(word[i], 0) + 1
        }

        for (i in word.indices){
            if (guess[i] == word[i]){
                numMatches += 1
                matchedPos[i] = true
                letterOccurrence[word[i]] = letterOccurrence.getOrDefault(word[i], 0) - 1
                rowViews[i].background.setTint(resources.getColor(R.color.green))
            } else if (guess[i] !in word){
                matchedPos[i] = false
                keyboard.disableKey(guess[i])
            }
        }

        for (i in guess.indices){
            if (letterOccurrence.getOrDefault(guess[i], 0) > 0 && !matchedPos[i]){
                letterOccurrence[guess[i]] = letterOccurrence.getOrDefault(guess[i], 0) - 1
                rowViews[i].background.setTint(resources.getColor(R.color.yellow))
            }
        }

        if (numMatches == 5){
            return true
        }
        return false
    }

    private fun getRowViews(row: Int) : ArrayList<CardView> {
        val rowString = "row_$row"
        val rowId = resources.getIdentifier(rowString, "id", packageName)
        val rowLayout: LinearLayout = findViewById(rowId)
        var cardView: CardView
        val views: ArrayList<CardView> = ArrayList()

        for (i in 0 until rowLayout.childCount){
            cardView = rowLayout.getChildAt(i) as CardView
            views.add(cardView)
        }

        return views

    }

}