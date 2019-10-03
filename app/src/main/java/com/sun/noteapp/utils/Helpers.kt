package com.sun.noteapp.utils

import android.app.Activity
import android.content.res.Resources
import android.view.View
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels

fun Activity.showToast(string: CharSequence, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, string, length).show()
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun getListColor(): MutableList<Pair<Int?, String>> {
    val colors = mutableListOf<Pair<Int?, String>>()
    for (i in 0..9) {
        colors.add(ColorPicker.getColorWithName(i))
    }
    return colors
}

fun getCurrentTime() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

fun String.allIndexOf(word: String): List<Int> {
    val results = mutableListOf<Int>()
    var position = 0
    while (position < this.length - word.length) {
        val check = this.indexOf(word, position, true)
        if (check != -1) {
            results.add(check)
            position = check + word.length
        } else
            position++
    }
    return results
}

fun getLabelsFromLabelDataString(labelDataStrings: List<String>): List<String> {
    val results = mutableListOf<String>()
    labelDataStrings.forEach {
        val labels = it.split("_")
        results.addAll(labels)
    }
    Collections.sort(results)
    return results.distinct()
}