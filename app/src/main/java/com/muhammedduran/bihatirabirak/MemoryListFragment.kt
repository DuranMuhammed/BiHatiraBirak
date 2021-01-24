package com.muhammedduran.bihatirabirak

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_memory_list.*


class MemoryListFragment : Fragment() {

    val memoryTitleList = ArrayList<String>()
    val memoryContentList = ArrayList<String>()
    val memoryImageList = ArrayList<ByteArray?>()
    val memoryIdList = ArrayList<Int>()
    val memoryDateList = ArrayList<String?>()
    private lateinit var memoryAdapter : MemoryListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.delete_memory_item).setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memory_list, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memoryAdapter = MemoryListAdapter(memoryTitleList, memoryContentList, memoryImageList, memoryIdList, memoryDateList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = memoryAdapter

        getDataFromSQLite()

        add_memory_button.setOnClickListener {
            val action = MemoryListFragmentDirections.actionMemoryListFragmentToMemoryAddFragment(-1, "new")
            Navigation.findNavController(view).navigate(action)

        }
    }

    fun getDataFromSQLite(){
        try{
            if(activity!= null){
                val database = requireActivity().openOrCreateDatabase("Memories", Context.MODE_PRIVATE,null)

                val cursor = database.rawQuery("SELECT * FROM memories ORDER BY id DESC",null)
                val titleIx = cursor.getColumnIndex("memorytitle")
                val idIx = cursor.getColumnIndex("id")
                val contentIx = cursor.getColumnIndex("memorycontent")
                val imageIx = cursor.getColumnIndex("image")
                val dateIx = cursor.getColumnIndex("currentDate")

                memoryIdList.clear() //To prevent review of views in list
                memoryTitleList.clear()  //To prevent review of views in list
                memoryContentList.clear()  //To prevent review of views in list
                memoryImageList.clear()  //To prevent review of views in list
                memoryDateList.clear() //To prevent review of views in list

                while (cursor.moveToNext()) {
                    memoryTitleList.add(cursor.getString(titleIx))
                    memoryContentList.add(cursor.getString(contentIx))
                    memoryImageList.add(cursor.getBlob(imageIx))
                    memoryIdList.add(cursor.getInt(idIx))
                    memoryDateList.add(cursor.getString(dateIx))

                }
                memoryAdapter.notifyDataSetChanged()
                cursor.close()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}