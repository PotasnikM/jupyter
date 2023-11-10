package com.example.jupyter.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jupyter.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class NotebookActivity : AppCompatActivity() {
    private lateinit var filePath: String
    private lateinit var recyclerView: RecyclerView
    private val cells: MutableList<Cell> = mutableListOf()
    private lateinit var adapter: CellsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notebook)

        filePath = intent.getStringExtra("NOTEBOOK_FILE_PATH") ?: ""
        if (filePath.isNotEmpty()) {
            recyclerView = findViewById(R.id.notebook_container)
            recyclerView.layoutManager = LinearLayoutManager(this)

            loadCellsFromFile()

            adapter = CellsAdapter(cells, ::onCellContentChanged, ::onAddCellRequest)
            recyclerView.adapter = adapter
            setupSwipeToDelete()

            val notebookTitle = findViewById<TextView>(R.id.notebookTitle)
            notebookTitle.text = File(filePath).nameWithoutExtension
        } else {
            Toast.makeText(this, "No file path provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun onCellContentChanged(position: Int, content: String) {
        // Adjust for the additional button row
        cells[position / 2].content = content
        saveNotebookToFile()
    }

    private fun onAddCellRequest(positionInCellsList: Int, type: String) {
        // Calculate the index in the cells list where the new cell will be added
        addNewCell(type, "", positionInCellsList)
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // Nie obsługujemy przenoszenia.
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                // Zablokuj przesuwanie dla wiersza z przyciskami
                if (viewHolder.itemViewType == CellsAdapter.VIEW_TYPE_ADD_BUTTONS) {
                    return 0
                }
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Nie usuwamy wiersza z przyciskami, tylko komórki
                if (viewHolder.itemViewType == CellsAdapter.VIEW_TEXT_CELL || viewHolder.itemViewType == CellsAdapter.VIEW_CODE_CELL) {
                    // Skoryguj pozycję, biorąc pod uwagę wiersz z przyciskami na górze
                    val adjustedPosition = (position - 1) / 2
                    cells.removeAt(adjustedPosition)
                    adapter.notifyItemRangeRemoved(position, 2) // Usuwamy komórkę i przyciski
                    saveNotebookToFile()
                } else {
                    adapter.notifyItemChanged(position) // Przywróć pozycję, jeśli to przyciski
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
        adapter.notifyDataSetChanged() // Odświeżenie adaptera po usunięciu elementu
    }


    private fun addNewCell(type: String, content: String, positionInCellsList: Int) {
        // Tworzenie nowej komórki
        val newCell = Cell(type, content, positionInCellsList)

        // Dodawanie komórki do listy
        cells.add(positionInCellsList, newCell)

        // Powiadomienie adaptera o wstawieniu komórki i przycisków dodawania
        val adapterPositionToAdd = positionInCellsList * 2 - 1
        adapter.notifyItemRangeInserted(adapterPositionToAdd, 2)

        // Zaktualizowanie widoków poniżej dodanej komórki
        adapter.notifyItemRangeChanged(adapterPositionToAdd + 2, adapter.itemCount - (adapterPositionToAdd + 2))

        // Zapis do pliku
        saveNotebookToFile()
    }




    private fun loadCellsFromFile() {
        val fileContent = File(filePath).readText()
        val json = JSONObject(fileContent)
        val jsonCells = json.getJSONArray("cells")
        for (i in 0 until jsonCells.length()) {
            val cellJson = jsonCells.getJSONObject(i)
            val type = cellJson.getString("cell_type")
            val content = cellJson.getJSONArray("source").join("\n").replace("\"", "")
            cells.add(Cell(type, content, i))
        }
    }

    private fun saveNotebookToFile() {
        val json = JSONObject()
        val cellsJsonArray = JSONArray()

        for (cell in cells) {
            if (cell.type != "add_buttons") { // Pomiń komórkę z przyciskami dodawania
                val contentArray = JSONArray()
                cell.content.split("\n").forEach { line ->
                    contentArray.put(line)
                }

                val cellJson = JSONObject().apply {
                    put("cell_type", cell.type)
                    put("source", contentArray)
                }
                cellsJsonArray.put(cellJson)
            }
        }

        json.put("cells", cellsJsonArray)
        File(filePath).writeText(json.toString(2)) // Formatowanie JSON dla lepszej czytelności
    }

    override fun onPause() {
        super.onPause()
        saveNotebookToFile()
    }

    data class Cell(var type: String, var content: String, var index: Int)
}
