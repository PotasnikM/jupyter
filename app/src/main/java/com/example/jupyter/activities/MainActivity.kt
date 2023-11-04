package com.example.jupyter.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import com.example.jupyter.R
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var createButton: ImageButton
    private lateinit var listView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createButton = findViewById(R.id.addNotebookButton)
        listView = findViewById(R.id.notebook_list)

        createButton.setOnClickListener {
            promptForNotebookNameAndCreateFile()
        }

        listIpynbFiles()
    }

    private fun promptForNotebookNameAndCreateFile() {
        val input = EditText(this)
        AlertDialog.Builder(this).apply {
            setTitle("Enter Notebook Name")
            setView(input)
            setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val success = createIpynbFile(name)
                    if (success) {
                        Toast.makeText(this@MainActivity, "Notebook '$name.ipynb' created", Toast.LENGTH_SHORT).show()
                        listIpynbFiles()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to create notebook. Does the file already exist?", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Notebook name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun createIpynbFile(notebookName: String): Boolean {
        val safeName = notebookName.replace("\\s+".toRegex(), "_")
        val fileName = "${safeName}.ipynb"
        val file = File(filesDir, fileName)
        return if (file.exists()) {
            false
        } else {
            file.writeText(
                """
                {
                    "cells": [],
                    "metadata": {},
                    "nbformat": 4,
                    "nbformat_minor": 5
                }
                """.trimIndent()
            )
            true
        }
    }

    private fun listIpynbFiles() {
        val files = filesDir.listFiles { _, name -> name.endsWith(".ipynb") } ?: return

        listView.removeAllViews()

        val inflater = LayoutInflater.from(this)
        files.forEach { file ->
            val notebookView = inflater.inflate(R.layout.notebook_item, listView, false).apply {
                findViewById<TextView>(R.id.notebook_name).text = file.name

                // Set an OnClickListener on the entire notebook view or a specific button/view within it
                setOnClickListener {
                    // Create an intent to start NotebookActivity
                    val intent = Intent(this@MainActivity, NotebookActivity::class.java)
                    // Optionally pass data to NotebookActivity, such as the file name or path
                    intent.putExtra("NOTEBOOK_FILE_PATH", file.absolutePath)
                    startActivity(intent)
                }

                val deleteButton = findViewById<ImageButton>(R.id.delete_notebook_button)
                deleteButton.setOnClickListener {
                    file.delete()
                    listIpynbFiles() // Refresh the list after deletion
                }
            }
            listView.addView(notebookView)
        }
    }

}
