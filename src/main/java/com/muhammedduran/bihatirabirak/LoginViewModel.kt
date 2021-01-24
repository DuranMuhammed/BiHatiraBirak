package com.muhammedduran.bihatirabirak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class LoginViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm


    fun loginDataChanged(username: String ){
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        }  else {
            _loginForm.value = LoginFormState(isDataValid = true)

        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        if (username.length > 2) {
            val pattern = Pattern.compile("[^[a-zA-Z][ÇçĞğİıÖöÜü]]")

            val matcher = pattern.matcher(username)
            if (!matcher.find()) {
                return true

            } else {
                username.isNotBlank()
                return false
            }
        }else{
            return false
        }

    }
}