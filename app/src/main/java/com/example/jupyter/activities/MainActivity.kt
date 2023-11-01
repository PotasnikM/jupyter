package com.example.jupyter.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.jupyter.R
import java.io.File

class MainActivity : ComponentActivity() {
//    private val createButton: ImageButton? = findViewById(R.id.addNotebookButton)
//    private val listView: LinearLayout? = findViewById(R.id.notebook_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setContentView(R.layout.activity_main)
        }
//        createButton?.setOnClickListener {
//            createIpynbFile()
//            if (listView != null) {
//                listIpynbFiles(listView)
//            }
//        }
//
//        if (listView != null) {
//            listIpynbFiles(listView)
//        }
    }
    private fun createIpynbFile() {
        val fileName = "notebook_${System.currentTimeMillis()}.ipynb"
        val file = File(filesDir, fileName)

        file.writeText("""
            {
                "cells": [],
                "metadata": {},
                "nbformat": 4,
                "nbformat_minor": 5
            }
        """.trimIndent())
    }

    private fun listIpynbFiles(linearLayout: LinearLayout) {
        val files = filesDir.listFiles { _, name -> name.endsWith(".ipynb") } ?: return

        linearLayout.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (file in files) {

            val notebookView = inflater.inflate(R.layout.notebook_item, linearLayout, false)

            val textView = notebookView.findViewById<TextView>(R.id.notebook_name)
            textView.text = file.name

            linearLayout.addView(notebookView)
        }
    }
}
