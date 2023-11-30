package com.example.jupyter.activities

import NotebookAdapter
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jupyter.R
import io.github.kbiakov.codeview.classifier.CodeProcessor
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var createButton: ImageButton
    private lateinit var notebookList: RecyclerView
    private lateinit var adapter: NotebookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CodeProcessor.init(this) // auto-detekcja jezyka programowania

        createButton = findViewById(R.id.addNotebookButton)
        notebookList = findViewById(R.id.notebook_list)

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

        adapter = NotebookAdapter(files.toList(),
            onNotebookClick = { file ->
                openNotebook(file)
            },
            onNotebookDelete = { file ->
                file.delete()
                listIpynbFiles()
            }
        )

        notebookList.layoutManager = LinearLayoutManager(this)
        notebookList.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeItem(viewHolder.adapterPosition)
            }
        })

        itemTouchHelper.attachToRecyclerView(notebookList)
    }

    private fun openNotebook(file: File) {
        val intent = Intent(this, NotebookActivity::class.java)
        intent.putExtra("NOTEBOOK_FILE_PATH", file.absolutePath)
        startActivity(intent)
    }
}
