package com.example.jupyter.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.jupyter.R
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setContentView(R.layout.activity_notebook)
        }
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

    private fun listIpynbFiles(listView: ListView) {
        val files = filesDir.listFiles { _, name -> name.endsWith(".ipynb") }?.map { it.name } ?: listOf()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, files)
        listView.adapter = adapter
    }
}