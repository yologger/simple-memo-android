package com.yologger.simple_memo.presentation.screen.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.yologger.simple_memo.R
import com.yologger.simple_memo.presentation.model.Memo
import com.yologger.simple_memo.presentation.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : BaseFragment() {

    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: HomeViewModel by viewModel()

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var speedDialView: SpeedDialView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        searchView = rootView.findViewById(R.id.fragment_home_sv)
        recyclerView = rootView.findViewById(R.id.fragment_home_rv)
        speedDialView = rootView.findViewById(R.id.fragment_home_sd)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBinding()
        setupSearchView()
        setupRecyclerView()
        setupSpeedDial()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchAllMemos()
    }

    private fun setupBinding() {
        viewModel.memosLiveData.observe(viewLifecycleOwner, Observer { memos ->
            recyclerViewAdapter.update(memos)
        })

        viewModel.routingEvent.observe(viewLifecycleOwner, Observer { event ->
            when(event) {
                HomeVMRoutingEvent.OPEN_NEW_POST -> { }
                HomeVMRoutingEvent.OPEN_EDIT -> { }
                HomeVMRoutingEvent.OPEN_DETAIL -> { }
                HomeVMRoutingEvent.DELETE_SUCCESS -> {
                    Toast.makeText(requireActivity(), getString(R.string.fragment_home_message_delete_success), Toast.LENGTH_SHORT).show()
                }
                HomeVMRoutingEvent.UNKNOWN_ERROR -> { }
            }
        })
    }

    private fun setupRecyclerView() {
        recyclerViewAdapter = RecyclerViewAdapter()
        recyclerView.adapter = recyclerViewAdapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }

    private fun setupSearchView() {  }

    private fun setupSpeedDial() {
        speedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fragment_home_sd_add, R.drawable.icon_create_filled_black_24).setLabel(getString(R.string.fragment_home_btn_new_post)).create())
        speedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fragment_home_sd_edit, R.drawable.icon_reorder_filled_black_24).setLabel(getString(R.string.fragment_home_btn_reorder)).create())
        speedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fragment_home_sd_delete, R.drawable.icon_delete_filed_black_24).setLabel(getString(R.string.fragment_home_btn_delete)).create())
        speedDialView.setOnActionSelectedListener {
            when(it.id) {
                R.id.fragment_home_sd_add -> {
                    router.openCreate()
                    speedDialView.close()
                    true
                }
                R.id.fragment_home_sd_edit -> {
                    router.openEdit()
                    speedDialView.close()
                    true
                }
                R.id.fragment_home_sd_delete -> {
                    router.openDelete()
                    speedDialView.close()
                    true
                }
            }
            false
        }
    }

    inner class RecyclerViewAdapter
    constructor(
        private var memos: MutableList<Memo> = mutableListOf()
    ) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fragment_home_memo, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = memos.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) = (holder as ViewHolder).bind(memos[position])

        fun update(memos: List<Memo>) {
            this.memos = memos.toMutableList()
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val textViewTitle: TextView = itemView.findViewById(R.id.item_fragment_home_memo_tv_title)

            fun bind(memo: Memo) {
                textViewTitle.text = memo.title

                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val memoId = memos[position].id
                        val memoPosition = memos[position].position
                        if (memoId != null) { router.openDetail(memoId, memoPosition) }
                    }
                }

                itemView.setOnLongClickListener {
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage(getString(R.string.fragment_home_message_delete))
                    builder.setPositiveButton(getString(R.string.fragment_home_message_alert_ok)) { _, _ ->
                        val position = adapterPosition
                        viewModel.deleteMemo(position)
                    }
                    builder.setNegativeButton(getString(R.string.fragment_home_message_alert_cancel)) { _, _ ->
                    }
                    builder.show()
                    true
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}