package ru.brightos.kattcasapp.ui.main.console

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_console.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.brightos.kattcasapp.App
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.ui.main.MainActivity

class ConsoleFragment : Fragment(R.layout.fragment_console) {
    private lateinit var model: ConsoleViewModel

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reader.isFocusableInTouchMode = true
        reader.requestFocus()

        OverScrollDecoratorHelper.setUpOverScroll(console_scroll)
        runBlocking { model.init((activity?.application as App).preferenceRepository, "screen -rd ${(activity as MainActivity).openScreenID}") }
        (activity as MainActivity).openScreenID = null

        model.status.observe(viewLifecycleOwner) { text ->
            runBlocking {
                launch {
                    console.text = "${console.text} $text"
                    console.text =
                        console.text.toString().replace("\u001B\\[[;\\d]*m".toRegex(), "")
                    console_scroll.post { console_scroll.fullScroll(ScrollView.FOCUS_DOWN) }
                }
            }
            print(text)
        }

        send.setOnClickListener {
            model.execute(reader_inside.text.toString())
            reader_inside.setText("")
        }
    }

    override fun onDestroy() {
        model.clear()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        model = ViewModelProvider(this).get(ConsoleViewModel::class.java)
    }
}