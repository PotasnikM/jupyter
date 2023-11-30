package com.example.jupyter.activities

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.jupyter.R
import io.github.kbiakov.codeview.CodeView
import io.github.kbiakov.codeview.highlight.CodeHighlighter
import io.github.kbiakov.codeview.highlight.ColorTheme
import org.intellij.lang.annotations.Language


class CellsAdapter(
    private val cells: MutableList<NotebookActivity.Cell>,
    private val onCellContentChanged: (Int, String) -> Unit,
    private val onAddCellRequest: (Int, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_CODE_CELL = 0
        const val VIEW_TEXT_CELL = 1  // Zmieniono wartość
        const val VIEW_TYPE_ADD_BUTTONS = 2
    }


    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VIEW_TYPE_ADD_BUTTONS // Pierwszy wiersz to przyciski dodawania
            position % 2 == 0 -> {
                // Każdy parzysty indeks po pierwszym to przyciski dodawania
                VIEW_TYPE_ADD_BUTTONS
            }
            else -> {
                // Nieparzyste indeksy to komórki
                val cellIndex = (position - 1) / 2
                when (cells[cellIndex].type) {
                    "markdown" -> VIEW_TEXT_CELL
                    "code" -> VIEW_CODE_CELL
                    else -> throw IllegalArgumentException("Unknown cell type: ${cells[cellIndex].type}")
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TEXT_CELL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text_cell, parent, false)
                val type_cell = "markdown"
                CellViewHolder(view, type_cell, onCellContentChanged)
            }
            VIEW_CODE_CELL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_code_cell, parent, false)
                val type_cell = "code"
                CellViewHolder(view, type_cell, onCellContentChanged)
            }
            VIEW_TYPE_ADD_BUTTONS -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.add_cell_buttons, parent, false)
                AddButtonsViewHolder(view, onAddCellRequest)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CellViewHolder -> {
                // Dla komórek (nieparzyste indeksy poza pierwszym)
                val cellIndex = (position - 1) / 2
                val cell = cells[cellIndex]
                holder.bind(cell)
            }
            is AddButtonsViewHolder -> {
                // Dla przycisków dodawania (parzyste indeksy i pierwszy wiersz)
                val addCellPosition = if (position == 0) 0 else (position + 1) / 2
                holder.bind(addCellPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        // +1 dla wiersza z przyciskami dodawania na górze, pozostałe dla komórek i ich przycisków dodawania
        return 1 + cells.size * 2
    }

    class CellViewHolder(view: View, val typeCell: String, val onCellContentChanged: (Int, String) -> Unit) : RecyclerView.ViewHolder(view) {
        private val textInput: EditText? = if (typeCell == "markdown") {
            view.findViewById(R.id.textInput)
        } else {
            null
        }

        private val transparentEditText: EditText? = if (typeCell == "code") {
            view.findViewById(R.id.transparentEditText)
        } else {
            null
        }

        private val codeView: CodeView? = if (typeCell == "code") {
            view.findViewById(R.id.codeView)
        } else {
            null
        }

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            val content = when (typeCell) {
                "markdown" -> textInput?.text.toString()
                "code" -> transparentEditText?.text.toString() // Get content from EditText
                else -> ""
            }
            onCellContentChanged(adapterPosition, content)
        }

        init {
            textInput?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 1000) // Delay of 1 second
                }
            })

            transparentEditText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val codeString = s.toString()
                    val highlightedCode = CodeHighlighter.highlight("python", codeString, ColorTheme.DEFAULT.theme())
                    codeView?.setCode(highlightedCode)
                }
                override fun afterTextChanged(s: Editable?) {
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 1000) // Delay of 1 second
                }
            })


        }


        fun bind(cell: NotebookActivity.Cell) {
            when (typeCell) {
                "markdown" -> textInput?.setText(cell.content)
                "code" -> {
                    transparentEditText?.setText(cell.content)
                    codeView?.setCode(cell.content)
                }
            }
        }
    }


    class AddButtonsViewHolder(view: View, private val onAddCellRequest: (Int, String) -> Unit) : RecyclerView.ViewHolder(view) {
        private val addTextButton: View = view.findViewById(R.id.addTextCellButton)
        private val addCodeButton: View = view.findViewById(R.id.addCodeCellButton)

        fun bind(addCellPosition: Int) {
            addTextButton.setOnClickListener { onAddCellRequest(addCellPosition, "markdown") }
            addCodeButton.setOnClickListener { onAddCellRequest(addCellPosition, "code") }
        }

        fun bindTop() {
            // For the top buttons, the position is always 0
            addTextButton.setOnClickListener { onAddCellRequest(0, "markdown") }
            addCodeButton.setOnClickListener { onAddCellRequest(0, "code") }
        }
    }

}
