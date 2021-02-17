package com.yologger.simple_memo.presentation.screen.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.yologger.simple_memo.R
import com.yologger.simple_memo.presentation.screen.edit.EditActivity
import kotlinx.android.synthetic.main.activity_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : AppCompatActivity() {

    private val viewModel: DetailViewModel by viewModel()
    // private lateinit var binding: ActivityDetailBinding

    lateinit var toolbar: Toolbar
    lateinit var editTextTitle: EditText
    lateinit var editTextContent: EditText
    lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setup()
        setupBinding()
        setupToolbar()
        setupGesture()
    }

    private fun setup() {
        coordinatorLayout = findViewById(R.id.activity_detail_cl)
        editTextTitle = findViewById(R.id.activity_detail_et_title)
        editTextContent = findViewById(R.id.activity_detail_et_content)
        val memoId = intent.getIntExtra("memoId", 0)
        viewModel.fetchMemo(memoId)
    }

    private fun setupBinding() {
        viewModel.title.observe(this, Observer { editTextTitle.setText(it) })
        viewModel.content.observe(this, Observer { editTextContent.setText(it) })
        viewModel.routingEvent.observe(this, Observer {
            when(it) {
                DetailVMRoutingEvent.OPEN_EDIT -> {
                    val nextIntent = Intent(this, EditActivity::class.java)
                    startActivity(nextIntent)
                }
                DetailVMRoutingEvent.SHOW_TOAST -> {
                    Toast.makeText(this, "Tap twice to edit.", Toast.LENGTH_SHORT).show()
                }
                DetailVMRoutingEvent.SHOW_DELETE_DIALOG -> {
                    val builder = AlertDialog.Builder(coordinatorLayout.context)
                    builder.setMessage("Want to delete this post?")
                    builder.setPositiveButton("OK") { dialog, id ->
                        finish()
                    }
                    builder.setNegativeButton("CANCEL") { dialog, id ->

                    }
                    builder.show()
                }
                DetailVMRoutingEvent.DELETE_AND_CLOSE -> {
                    Snackbar.make(coordinatorLayout, "Successfully Deleted", Snackbar.LENGTH_LONG).show()
                    finish()
                }
            }
        })
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.activity_detail_toolbar)
        toolbar.setNavigationIcon(R.drawable.icon_close_filled_black_24)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_activity_detail_toolbar)
        toolbar.setOnMenuItemClickListener {
            when(it?.itemId) {
                R.id.menu_activity_detail_toolbar_delete -> { viewModel.showDeleteDialog() }
                R.id.menu_activity_detail_toolbar_edit -> { viewModel.openEdit() }
            }
            true
        }
    }

    private fun setupGesture() {
        val gestureDetector = GestureDetector(this, object: GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                viewModel.showToast()
                return super.onSingleTapConfirmed(e)
            }
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                viewModel.openEdit()
                return super.onDoubleTap(e)
            }
        })

        editTextTitle.setOnTouchListener { view, event ->
            gestureDetector.onTouchEvent(event)
        }

        editTextContent.setOnTouchListener { view, event ->
            gestureDetector.onTouchEvent(event)
        }
    }
}