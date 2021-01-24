package com.muhammedduran.bihatirabirak

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.muhammedduran.bihatirabirak.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.view.*
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var usernameData : String? = null
    private lateinit var loginViewModel: LoginViewModel
    lateinit var sharedPreferences : SharedPreferences //Lately initialized
    var currentDate : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val username = findViewById<EditText>(R.id.username_TextView)
        val login = findViewById<Button>(R.id.login_Button)
        sharedPreferences =this.getSharedPreferences("com.muhammedduran.bihatirabirak", MODE_PRIVATE)
        if(sharedPreferences.getString("Name", "null") != "null"){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)


        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(username.text.toString())
        }

    }

    fun login(view: View){
        usernameData = binding.usernameTextView.text.toString()
        sharedPreferences.edit().putString("Name", usernameData).apply()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.istanbul)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        //Resmi veritabanına kaydetmek için bitmapten format değiştirir.
        val byteArray = outputStream.toByteArray()
        //Resimleri byte dizisine çevirerek kaydedeceğiz

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val currentDateTime =  LocalDateTime.now()
            currentDate = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
            println("Current Date is: $currentDate")
        }

        try{
            val userDatabase = this.openOrCreateDatabase("Memories", Context.MODE_PRIVATE, null)
            userDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY, memorytitle VARCHAR, memorycontent VARCHAR, image BLOB, currentDate VARCHAR)")
            val sqlString = "INSERT INTO memories (memorytitle, memorycontent, image, currentDate) VALUES (?, ?, ?, ?)"
            val sqLiteStatement = userDatabase.compileStatement(sqlString)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                sqLiteStatement.bindString(1,"İlk Hatıram")
                sqLiteStatement.bindString(2, "Hoş geldiniz ${usernameData}" + "\n" + "Bu güzel günü ileride hatırlamak isteyebileceğinizi düşündük ve sizin için ilk hatıranızı oluşturduk. " +
                        "\"" + "Hatıraları olanlar hatırlarlar." + "\"" + " der İhsan Fazlıoğlu ve ekler " + "\"" + "İnsanlar nezdinde de hatırladığınız ve hatırlandığınız oranda " +"\'" + "hatır" + "\'" +
                        "ınız yani itibârınız olur." + "\"" + " Hatıranız ve hatrınız bol olsun!")
                sqLiteStatement.bindBlob(3, byteArray)
                sqLiteStatement.bindString(4,currentDate)
                sqLiteStatement.execute()
            } 
            else {
                sqLiteStatement.bindString(1,"İlk Hatıram")
                sqLiteStatement.bindString(2, "Hoş geldiniz ${usernameData}" + "\n" + "Bu güzel günü ileride hatırlamak isteyebileceğinizi düşündük ve sizin için ilk hatıranızı oluşturduk. " +
                        "\"" + "Hatıraları olanlar hatırlarlar." + "\"" + " der İhsan Fazlıoğlu ve ekler " + "\"" + "İnsanlar nezdinde de hatırladığınız ve hatırlandığınız oranda " +"\'" + "hatır" + "\'" +
                        "ınız yani itibârınız olur." + "\"" + " Hatıranız ve hatrınız bol olsun!")
                sqLiteStatement.bindBlob(3, byteArray)
                sqLiteStatement.execute()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }
}


/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}