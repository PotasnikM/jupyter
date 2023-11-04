package com.example.jupyter.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jupyter.R

class NotebookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notebook)

        // Get the file path from the intent
        val filePath = intent.getStringExtra("NOTEBOOK_FILE_PATH")
        if (filePath != null) {
            // Open and display the notebook file
        } else {
            // Handle the error
        }
    }
}
