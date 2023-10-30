package com.example.jupyter.activities
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jupyter.editor.models.TextCell
import com.example.jupyter.R
import java.io.File

class TextCellActivity : AppCompatActivity() {

    private lateinit var textInput: EditText
    private var currentTextModel: TextCell? = null
    private val fileName = "savedText.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_text_cell)

        textInput = findViewById(R.id.textInput)

        textInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveText()
            }
        }

        loadText()
    }

    private fun saveText() {
        val text = textInput.text.toString()
        currentTextModel = TextCell(text)

        val file = File(filesDir, fileName)
        file.writeText(text)

        Toast.makeText(this, "Tekst zapisany!", Toast.LENGTH_SHORT).show()
    }

    private fun loadText() {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            val text = file.readText()
            currentTextModel = TextCell(text)
            textInput.setText(text)
        }
    }

    override fun onPause() {
        super.onPause()
        saveText()
    }
}