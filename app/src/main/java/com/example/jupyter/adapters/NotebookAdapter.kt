import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jupyter.R
import java.io.File

class NotebookAdapter(private val notebooks: List<File>, private val onNotebookClick: (File) -> Unit, private val onNotebookDelete: (File) -> Unit) : RecyclerView.Adapter<NotebookAdapter.NotebookViewHolder>() {

    inner class NotebookViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.notebook_item, parent, false)) {
        private val notebookName: TextView = itemView.findViewById(R.id.notebook_name)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_notebook_button)
        fun bind(notebook: File) {
            notebookName.text = notebook.name
            itemView.setOnClickListener { onNotebookClick(notebook) }

            deleteButton.setOnClickListener {
                onNotebookDelete(notebook)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotebookViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NotebookViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: NotebookViewHolder, position: Int) {
        val notebook = notebooks[position]
        holder.bind(notebook)
    }

    override fun getItemCount() = notebooks.size

    fun removeItem(position: Int) {
        onNotebookDelete(notebooks[position])
        notifyItemRemoved(position)
    }
}
