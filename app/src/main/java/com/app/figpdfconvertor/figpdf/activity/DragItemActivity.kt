package com.app.figpdfconvertor.figpdf.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.activity.BaseActivity
import com.app.figpdfconvertor.figpdf.databinding.ActivityDragItemBinding
import com.app.figpdfconvertor.figpdf.adapter.EditedImagesAdapter
import com.app.figpdfconvertor.figpdf.utils.MyUtils

class DragItemActivity : BaseActivity() {
    private lateinit var binding: ActivityDragItemBinding
    private lateinit var images: MutableList<String>
    private lateinit var adapter: EditedImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityDragItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        images = intent.getStringArrayListExtra("editedUris")?.toMutableList() ?: mutableListOf()

        adapter = EditedImagesAdapter(
            this,
            images,
            onItemClick = { pos ->

            },
            onSelectionChanged = { count -> }
        )

        val layoutManager = GridLayoutManager(this, 2)
        binding.rvImageList.layoutManager = layoutManager
        binding.rvImageList.adapter = adapter
        binding.backButtonToProConverterTools.setOnClickListener {
            finish()
        }
        // --- Add drag & drop support ---
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0 // no swipe
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition

                // Swap items in your data list
                val temp = images[fromPos]
                images[fromPos] = images[toPos]
                images[toPos] = temp

                // Notify adapter
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not needed
            }

            override fun isLongPressDragEnabled(): Boolean = true
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvImageList)
    }
}