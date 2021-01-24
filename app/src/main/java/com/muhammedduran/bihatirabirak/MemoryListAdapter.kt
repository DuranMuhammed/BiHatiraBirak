package com.muhammedduran.bihatirabirak

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class MemoryListAdapter(val memoryTitleList: ArrayList<String>, val memoryContentList: ArrayList<String>,
                        val memoryImageList: ArrayList<ByteArray?>, val memoryIdList: ArrayList<Int>,
                        val memoryDateList: ArrayList<String?>)
    : RecyclerView.Adapter<MemoryListAdapter.MemoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryHolder {
        //Binding will be done here
        val inflater = LayoutInflater.from(parent.context) //parent reference to MainActivity
        val view = inflater.inflate(R.layout.memory_item,parent,false) // false -> don't bind to root(parent)
        return MemoryHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryHolder, position: Int) {
        holder.memoryTitleText?.text = memoryTitleList[position]
        holder.memoryContentText?.text = memoryContentList[position]
        if(memoryImageList[position] != null)
        {val byteArray = memoryImageList[position]
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
            holder.memoryImageView?.setImageBitmap(bitmap)
            //println("Resim eklendi" + memoryImageList.size)
        }
        else if(memoryImageList[position] == null){
            holder.memoryImageView?.visibility = View.GONE
            println("resim eklenmedi" + memoryImageList.size)
        }
        if(memoryDateList[position] != null){
            holder.memoryDateText?.text = memoryDateList[position]
        }
        else if(memoryDateList[position] == null){
            holder.memoryDateText?.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            val action = MemoryListFragmentDirections.actionMemoryListFragmentToMemoryAddFragment(memoryIdList[position], "old")
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        // How many recylerView is shown
        return memoryTitleList.size
    }


    class MemoryHolder(view: View) : RecyclerView.ViewHolder(view) {
        //View Holder class --- Views are declared in here
        var memoryTitleText : TextView? = null
        var memoryContentText : TextView? = null
        var memoryImageView : ImageView? = null
        var memoryDateText : TextView? = null

        init {
            memoryTitleText = view.findViewById(R.id.memory_item_title)
            memoryContentText = view.findViewById(R.id.memory_item_content)
            memoryImageView = view.findViewById(R.id.memory_item_image)
            memoryDateText = view.findViewById(R.id.memory_item_date)
        }


    }
}