package ru.brightos.kattcasapp.util.adapter

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.layout_card.view.*
import kotlinx.android.synthetic.main.layout_catalog.view.*
import kotlinx.android.synthetic.main.layout_catalog.view.status
import kotlinx.android.synthetic.main.layout_catalog.view.title
import kotlinx.coroutines.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.data.PreferenceRepository
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class PathAdapter(
    private val context: Context,
    private val preferenceRepository: PreferenceRepository,
    private val paths: ArrayList<String>
) : RecyclerView.Adapter<PathAdapter.ViewHolder>() {

    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(mInflater.inflate(R.layout.layout_catalog, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.title.setText(paths[position])

        holder.itemView.title.isSelected = true
        holder.itemView.status.isSelected = true

        if (!paths[position].matches("^[a-zA-Z0-9]+$".toRegex())) {
            holder.itemView.status.setText("В названии папки есть недопустимые символы")
            holder.itemView.status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorBad
                )
            )
            holder.itemView.upload.visibility = View.GONE
            holder.itemView.warning.visibility = View.VISIBLE
        } else if (!File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/Scripts/" + paths[position] + "/launch.py"
            ).exists()
        ) {
            holder.itemView.status.setText("Файл launch.py не найден")
            holder.itemView.status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorBad
                )
            )
            holder.itemView.upload.visibility = View.GONE
            holder.itemView.warning.visibility = View.VISIBLE
        } else {
            holder.itemView.status.setText("Каталог готов к отправке")
            holder.itemView.status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                )
            )
            holder.itemView.upload.visibility = View.VISIBLE
            holder.itemView.warning.visibility = View.GONE
        }

        holder.itemView.upload.setOnClickListener {
            runBlocking {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        holder.itemView.status.setText("Начинается отправка")
                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

                        TransitionManager.beginDelayedTransition(
                            holder.itemView as ViewGroup,
                            sharedAxis
                        )

                        holder.itemView.upload.visibility = View.GONE
                    }

                    send(position)

                    withContext(Dispatchers.Main) {
                        holder.itemView.status.setText("Каталог отправлен успешно")
                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

                        TransitionManager.beginDelayedTransition(
                            holder.itemView as ViewGroup,
                            sharedAxis
                        )

                        holder.itemView.upload.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = paths.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun send(pos: Int) {
        try {
            val ftpClient = FTPClient()
            try {
                val path = paths.get(pos)
                ftpClient.connect(preferenceRepository.host)
                ftpClient.soTimeout = 10000
                ftpClient.enterLocalPassiveMode()
                if (ftpClient.login(preferenceRepository.user, preferenceRepository.password)) {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                    ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)
                    ftpClient.makeDirectory("/android_scripts/$path")

                    fun listFilesForFolder(folder: File, currentPath: String) {
                        for (fileEntry in folder.listFiles()) {
                            if (fileEntry.isDirectory) {
                                listFilesForFolder(fileEntry, "$currentPath${fileEntry.name}/")
                            } else {
                                val fs = FileInputStream(fileEntry)
                                val fileName =
                                    "/android_scripts/$path/$currentPath${fileEntry.name}"
                                val result = ftpClient.storeFile(fileName, fs)
                                fs.close()
                            }
                        }
                    }

                    listFilesForFolder(
                        File(
                            Environment.getExternalStorageDirectory()
                                .toString() + "/Scripts/" + path
                        ),
                        ""
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}