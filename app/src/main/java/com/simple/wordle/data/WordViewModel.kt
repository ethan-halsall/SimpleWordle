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

package com.simple.wordle.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordViewModel(application: Application): AndroidViewModel(application) {

    private val repository: WordsRepository
    val wordLiveData: MutableLiveData<String>
    val message: MutableLiveData<String>
    val isWord: MutableLiveData<Boolean>
    val keyMessage: MutableLiveData<String>

    init {
        val wordDoa = WordDatabase.getDatabase(application).wordDao()
        repository = WordsRepository(wordDoa)
        wordLiveData = MutableLiveData()
        message = MutableLiveData()
        keyMessage = MutableLiveData()
        isWord = MutableLiveData()
        // set word value when view-model is created
        generateWord()
    }

    fun sendMessage(text: String) {
        message.value = text
    }

    fun generateWord() {
        viewModelScope.launch(Dispatchers.IO) {
            wordLiveData.postValue(repository.generateWord())
        }
    }

    fun disableKeyMessage(key: String){
        keyMessage.value = key
    }

    fun isWord(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.isWord(word) != null){
                isWord.postValue(true)
            } else {
                isWord.postValue(false)
            }
        }

    }

}