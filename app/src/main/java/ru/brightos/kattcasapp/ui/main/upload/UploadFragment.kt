package ru.brightos.kattcasapp.ui.main.upload

import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_information.*
import kotlinx.android.synthetic.main.fragment_upload.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.brightos.kattcasapp.App
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.data.PreferenceRepository
import ru.brightos.kattcasapp.util.adapter.PathAdapter
import java.io.File

class UploadFragment : Fragment(R.layout.fragment_upload) {
    lateinit var model: UploadViewModel
    lateinit var preferenceRepository: PreferenceRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upload_recycler.layoutManager = LinearLayoutManager(context)
        upload_recycler.isNestedScrollingEnabled = false

        runBlocking {
            launch {
                val list = async { getPaths() }
                list.await().let {
                    if (it.size > 0)
                        upload_recycler.adapter =
                            PathAdapter(requireContext(), preferenceRepository, it)
                    else
                        no_folders.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getPaths(): ArrayList<String> {
        val result = ArrayList<String>()
        println(Environment.getExternalStorageDirectory().toString() + "/Scripts")
        val folder = File(
            Environment.getExternalStorageDirectory().toString() + "/Scripts"
        )

        if (!folder.exists())
            folder.createNewFile()

        if (folder.list() != null) {
            for (file in folder.listFiles())
                if (file.isDirectory)
                    result.add(file.name)
            println(result.toString())
        }

        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        preferenceRepository = (activity?.application as App).preferenceRepository
        model = ViewModelProvider(requireActivity()).get(UploadViewModel::class.java)
    }
}