package ru.brightos.kattcasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_auth.*
import ru.brightos.kattcasapp.App
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.ui.main.MainActivity

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        var value = intent.extras?.getBoolean("from_info")
        if (value == null)
            value = false

        login_button.setOnClickListener {
            (application as App).preferenceRepository.let {
                it.host = host.editText?.text.toString()
                it.user = user.editText?.text.toString()
                it.password = password.editText?.text.toString()

                println(it.host)
                println(it.user)
                println(it.password)

                goNext()
            }
        }

        if ((application as App).preferenceRepository.host != " " && !value)
            goNext()
    }

    private fun goNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}