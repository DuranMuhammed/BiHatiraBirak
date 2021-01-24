package com.muhammedduran.bihatirabirak

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.muhammedduran.bihatirabirak.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.fragment_memory_add.*

class MainActivity : AppCompatActivity() {

    private lateinit var navigationController : NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.custem_toolbar)

        navigationController = Navigation.findNavController(this,R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this,navigationController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.fragment)
        return navController.navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Hangi menü gösterilecek
        //infaleter xmli aktivite içinde kullanmak için
        val menuInflater = menuInflater //getMenuInflater()
        menuInflater.inflate(R.menu.delete_memory, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //kullanıcı item seçerse ne olacak
        if(item.itemId == R.id.delete_memory_item){
            val alert = AlertDialog.Builder(this@MainActivity)
            alert.setTitle("Hatıra Silinsin Mi?")
            alert.setMessage("Hatıranızı silmek istediğinize emin misiniz? Bu işlemi onaylarsanız hatıra kalıcı olarak silinir.")
            alert.setPositiveButton("Sil") {dialog, which ->
                //Delete Memory
                var fragment = FragmentManager.findFragment(memory_image_view) as MemoryAddFragment
                val deletedMemoryControl = fragment.deleteMemory()
                if(deletedMemoryControl == true){
                    val intent = intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //Closes all activities before it
                    startActivity(intent)
                }

            }
            alert.setNegativeButton("İptal et"){dialog, which -> }//Return
            alert.show()
        }
        return super.onOptionsItemSelected(item)

    }


}

