package com.muhammedduran.bihatirabirak

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_memory_add.*
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MemoryAddFragment : Fragment() {

    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null
    lateinit var userDatabase : SQLiteDatabase
    var selectedId : Int = 0
    lateinit var info : String
    var currentDate : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.delete_memory_item).setVisible(true)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memory_add, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        memory_image_view.visibility = View.INVISIBLE
        //save_button.isEnabled = false

        add_image_button.setOnClickListener { addImage(view) }

        arguments?.let {
            info = MemoryAddFragmentArgs.fromBundle(it).info
            if(info.equals("new")){
                //New Memory
                memory_image_view.visibility = View.INVISIBLE
                add_image_button.visibility = View.VISIBLE
                textView_inImage.visibility = View.VISIBLE
                memory_title_view.setText("")
                memory_content_view.setText("")
                save_button.setText("KAYDET")
                save_button.setOnClickListener { saveMemory(view) }
            }else{
                //Old Memory

                save_button.setText("GÜNCELLE")

                selectedId = MemoryAddFragmentArgs.fromBundle(it).id
                val database = context?.openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)

                val cursor = database!!.rawQuery("SELECT * FROM memories WHERE id = ?", arrayOf(selectedId.toString()))
                val titleIx = cursor.getColumnIndex("memorytitle")
                val contentIx = cursor.getColumnIndex("memorycontent")
                val imageIx = cursor.getColumnIndex("image")

                while (cursor.moveToNext()) {
                    memory_title_view.setText(cursor.getString(titleIx))
                    memory_content_view.setText(cursor.getString(contentIx))

                    val byteArray = cursor.getBlob(imageIx)
                    if(byteArray != null){
                        memory_image_view.visibility = View.VISIBLE
                        add_image_button.visibility = View.INVISIBLE
                        textView_inImage.visibility = View.INVISIBLE
                        selectedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        memory_image_view.setImageBitmap(selectedBitmap)
                    }else{
                        memory_image_view.visibility = View.INVISIBLE
                        add_image_button.visibility = View.VISIBLE
                        textView_inImage.visibility = View.VISIBLE

                    }
                }
                cursor.close()
                save_button.setOnClickListener { updateMemory(view) }
            }
        }

    }

    fun addImage(view: View){
        if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){ // izin daha önce verilmediyse, SDK >=23
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else{
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentToGallery, 2)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 1){
            if(grantResults.size > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentToGallery, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            //Kullanıcının galeriden sonraki aktivite kontrolü
            selectedPicture = data.data

            try {
                if(selectedPicture != null){
                    if(Build.VERSION.SDK_INT >= 28){
                        val source = ImageDecoder.createSource(activity?.contentResolver!!, selectedPicture!!)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                        memory_image_view.visibility = View.VISIBLE
                        //save_button.isEnabled = true
                        add_image_button.visibility = View.INVISIBLE
                        textView_inImage.visibility = View.INVISIBLE
                        memory_image_view?.setImageBitmap(selectedBitmap)
                    }
                    else{ //Eski APIlerde bu kullanılır
                        selectedBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver!!, selectedPicture)
                        memory_image_view.visibility = View.VISIBLE
                        //save_button.isEnabled = true
                        add_image_button.visibility = View.INVISIBLE
                        textView_inImage.visibility = View.INVISIBLE
                        memory_image_view?.setImageBitmap(selectedBitmap)
                    }
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun saveMemory(view: View){
        val memoryTitle = memory_title_view.text.toString()
        val memoryContent = memory_content_view.text.toString()

         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             val currentDateTime =  LocalDateTime.now()
             currentDate = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
             println("Current Date is: $currentDate")
         }

        if(memoryTitle != "" && memoryContent != "") {
            if (selectedBitmap != null){
                val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 350)
                val outputStream = ByteArrayOutputStream()
                smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                //Resmi veritabanına kaydetmek için bitmapten format değiştirir.
                val byteArray = outputStream.toByteArray()
                //Resimleri byte dizisine çevirerek kaydedeceğiz.
                 try {
                     userDatabase = requireActivity().openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)
                     userDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY, memorytitle VARCHAR, memorycontent VARCHAR, image BLOB, currentDate VARCHAR)")
                     val sqlString = "INSERT INTO memories (memorytitle, memorycontent, image, currentDate) VALUES (?, ?, ?, ?)"
                     val sqLiteStatement = userDatabase.compileStatement(sqlString)

                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                         sqLiteStatement.bindString(1, memoryTitle)
                         sqLiteStatement.bindString(2, memoryContent)
                         sqLiteStatement.bindBlob(3, byteArray)
                         sqLiteStatement.bindString(4,currentDate)
                         sqLiteStatement.execute()
                     }
                     else{
                         sqLiteStatement.bindString(1, memoryTitle)
                         sqLiteStatement.bindString(2, memoryContent)
                         sqLiteStatement.bindBlob(3, byteArray)
                         sqLiteStatement.execute()
                     }
                 } catch (e: Exception) {
                        e.printStackTrace()
                 }
                val action = MemoryAddFragmentDirections.actionMemoryAddFragmentToMemoryListFragment()
                Navigation.findNavController(view).navigate(action)
            }else{
                try {
                    userDatabase = requireActivity().openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)
                    userDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY, memorytitle VARCHAR, memorycontent VARCHAR, image BLOB, currentDate VARCHAR)")
                    val sqlString = "INSERT INTO memories (memorytitle, memorycontent, image, currentDate) VALUES (?, ?, ?, ?)"
                    val sqLiteStatement = userDatabase.compileStatement(sqlString)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        sqLiteStatement.bindString(1, memoryTitle)
                        sqLiteStatement.bindString(2, memoryContent)
                        sqLiteStatement.bindString(4,currentDate)
                        sqLiteStatement.execute()
                    }
                    else{
                        sqLiteStatement.bindString(1, memoryTitle)
                        sqLiteStatement.bindString(2, memoryContent)
                        sqLiteStatement.execute()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val action = MemoryAddFragmentDirections.actionMemoryAddFragmentToMemoryListFragment()
                Navigation.findNavController(view).navigate(action)
            }
        }
        else{
            Toast.makeText(requireActivity().baseContext, "Lütfen boş alanları doldurunuz", Toast.LENGTH_LONG).show()
        }
    }

    fun updateMemory(view: View){
        val memoryTitle = memory_title_view.text.toString()
        val memoryContent = memory_content_view.text.toString()

        if(memoryTitle != "" && memoryContent != ""){
            if (selectedBitmap != null){
                val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
                val outputStream = ByteArrayOutputStream()
                smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                //Resmi veritabanına kaydetmek için bitmapten format değiştirir.
                val byteArray = outputStream.toByteArray()
                //Resimleri byte dizisine çevirerek kaydedeceğiz.
                try {
                    userDatabase = requireActivity().openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)
                    userDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY, memorytitle VARCHAR, memorycontent VARCHAR, image BLOB, currentDate VARCHAR)")

                    //"UPDATE memories SET memorytitle = ?, memorycontent = ?, image = ? WHERE id = ?"
                    val sqlString = "UPDATE memories SET memorytitle = ?, memorycontent = ?, image = ? WHERE id = ?"
                    val sqLiteStatement = userDatabase.compileStatement(sqlString)
                    sqLiteStatement.bindString(1, memoryTitle)
                    sqLiteStatement.bindString(2, memoryContent)
                    sqLiteStatement.bindBlob(3, byteArray)
                    sqLiteStatement.bindString(4, selectedId.toString())
                    sqLiteStatement.execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val action = MemoryAddFragmentDirections.actionMemoryAddFragmentToMemoryListFragment()
                Navigation.findNavController(view).navigate(action)
            }else{
                try {
                    userDatabase = requireActivity().openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)
                    userDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY, memorytitle VARCHAR, memorycontent VARCHAR, image BLOB, currentDate VARCHAR)")
                    //"UPDATE memories SET memorytitle = ?, memorycontent = ?, image = ? WHERE id = ?"
                    val sqlString = "UPDATE memories SET memorytitle = ?, memorycontent = ? WHERE id = ?"
                    val sqLiteStatement = userDatabase.compileStatement(sqlString)
                    sqLiteStatement.bindString(1, memoryTitle)
                    sqLiteStatement.bindString(2, memoryContent)
                    sqLiteStatement.bindString(3, selectedId.toString())
                    sqLiteStatement.execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val action = MemoryAddFragmentDirections.actionMemoryAddFragmentToMemoryListFragment()
                Navigation.findNavController(view).navigate(action)
            }
        }
        else{
            Toast.makeText(requireActivity().baseContext, "Lütfen boş alanları doldurunuz", Toast.LENGTH_LONG).show()
        }

    }

    fun deleteMemory() : Boolean{

        //println("DELETE TEST")
        userDatabase = requireActivity().openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)
        userDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY, memorytitle VARCHAR, memorycontent VARCHAR, image BLOB, currentDate VARCHAR)")

        if(info.equals("old")) {
            try{
                //myDatabase.execSQL("DELETE FROM musicians WHERE id = 2 ")
                val sqlString = "DELETE FROM memories WHERE id = ?"
                val sqLiteStatement = userDatabase.compileStatement(sqlString)
                sqLiteStatement.bindString(1, selectedId.toString())
                sqLiteStatement.execute()
                //println("SİLİNDİ")
                return true
            }
            catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        else{
            Toast.makeText(requireActivity().baseContext, "Hatıra seçilmedi!", Toast.LENGTH_LONG).show()
            return false
        }

    }


    fun makeSmallerBitmap(image: Bitmap, maximumSize: Int) : Bitmap{
        var width =  maximumSize
        var height = 140


        /*val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1){
            width = maximumSize
            height = (width / bitmapRatio).toInt()
        }else{
            height = maximumSize
            width = (height * bitmapRatio).toInt()
        }*/
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

}



