package ru.brightos.kattcasapp.util.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.android.synthetic.main.layout_card.view.*
import kotlinx.coroutines.*
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.`object`.Directories
import ru.brightos.kattcasapp.`object`.Screens
import ru.brightos.kattcasapp.data.PreferenceRepository
import ru.brightos.kattcasapp.util.ssh.SSHUtil
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class InfoAdapter(
    private val context: Context,
    private val preferenceRepository: PreferenceRepository,
    private var dirs: ArrayList<Directories>,
    private val action: (id: Int) -> Unit
) : RecyclerView.Adapter<InfoAdapter.ViewHolder>() {

    init {
        dirs.forEach {
            Log.e("123", "${it.name} ${it.active} ${it.id}")
        }
    }

    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(mInflater.inflate(R.layout.layout_card, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.itemView.title.visibility = View.VISIBLE
        holder.itemView.status.visibility = View.VISIBLE

        holder.itemView.title.text = dirs[position].name
        holder.itemView.title.isSelected = true

        if (dirs[position].active)
            holder.itemView.circle.visibility = View.VISIBLE
        else
            holder.itemView.circle.visibility = View.GONE

        when {
            dirs[position].active -> {
                holder.itemView.status.text = "Каталог активен"
                holder.itemView.delete.visibility = View.GONE
                holder.itemView.start.visibility = View.GONE
                holder.itemView.console_join.visibility = View.VISIBLE
                holder.itemView.stop.visibility = View.VISIBLE
            }
            else -> {
                holder.itemView.status.text = "Каталог остановлен"
                holder.itemView.delete.visibility = View.VISIBLE
                holder.itemView.start.visibility = View.VISIBLE
                holder.itemView.console_join.visibility = View.GONE
                holder.itemView.stop.visibility = View.GONE
            }
        }

        holder.itemView.console_join.setOnClickListener {
            action.invoke(dirs[position].id)
        }

        holder.itemView.delete.setOnClickListener {
            runBlocking {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

                        TransitionManager.beginDelayedTransition(
                            holder.itemView as ViewGroup,
                            sharedAxis
                        )

                        holder.itemView.status.text = "Каталог удаляется"
                        holder.itemView.circle.visibility = View.GONE
                        holder.itemView.delete.visibility = View.GONE
                        holder.itemView.start.visibility = View.GONE
                    }

                    execute("rm -R ~/android_scripts/${dirs[position].name}")

                    withContext(Dispatchers.Main) {
                        holder.itemView.status.text = "Каталог удалён"
                        holder.itemView.delete.visibility = View.VISIBLE
                        holder.itemView.start.visibility = View.VISIBLE
                        holder.itemView.console_join.visibility = View.GONE
                        holder.itemView.stop.visibility = View.GONE

                        Toast.makeText(context, "Каталог удалён", Toast.LENGTH_SHORT).show()

                        deleteItemByPosition(position)
                    }
                }
            }
        }

        holder.itemView.start.setOnClickListener {
            runBlocking {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)

                        TransitionManager.beginDelayedTransition(
                            holder.itemView as ViewGroup,
                            sharedAxis
                        )

                        holder.itemView.wait.visibility = View.VISIBLE
                        holder.itemView.dir_constraint.visibility = View.INVISIBLE
                    }

                    execute(
                        "screen -S ${dirs[position].name}",
                        "cd ~/android_scripts/${dirs[position].name}",
                        "python3 launch.py",
                        "node launch.js",
                        "php launch.php"
                    )

                    val screens = getScreens()

                    dirs.forEach { dir ->
                        if (dir.name in screens.names) {
                            screens.names.forEachIndexed { i, name ->
                                if (name == dir.name) {
                                    dir.id = screens.ids[i]
                                }
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)

                        TransitionManager.beginDelayedTransition(
                            holder.itemView as ViewGroup,
                            sharedAxis
                        )

                        holder.itemView.wait.visibility = View.GONE
                        holder.itemView.dir_constraint.visibility = View.VISIBLE
                    }

                    withContext(Dispatchers.Main) {
                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

                        TransitionManager.beginDelayedTransition(
                            holder.itemView as ViewGroup,
                            sharedAxis
                        )

                        holder.itemView.status.text = "Каталог активен"
                        holder.itemView.delete.visibility = View.GONE
                        holder.itemView.start.visibility = View.GONE
                        holder.itemView.console_join.visibility = View.VISIBLE
                        holder.itemView.stop.visibility = View.VISIBLE
                        holder.itemView.circle.visibility = View.VISIBLE

                        Toast.makeText(context, "Каталог запущен", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        holder.itemView.stop.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    holder.itemView.status.text = "Каталог останавливается"
                    holder.itemView.circle.visibility = View.GONE
                }

                execute("screen -X -S ${dirs[position].id} quit")

                val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

                TransitionManager.beginDelayedTransition(
                    holder.itemView as ViewGroup,
                    sharedAxis
                )

                withContext(Dispatchers.Main) {
                    holder.itemView.status.text = "Каталог остановлен"
                    holder.itemView.delete.visibility = View.VISIBLE
                    holder.itemView.start.visibility = View.VISIBLE
                    holder.itemView.console_join.visibility = View.GONE
                    holder.itemView.stop.visibility = View.GONE

                    Toast.makeText(context, "Каталог остановлен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteItemByPosition(position: Int) {
        dirs.removeAt(position)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dirs.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun execute(vararg s: String) {
        lateinit var session: Session
        lateinit var channel: Channel
        lateinit var outputStream: PipedOutputStream
        try {
            println("Подготовка конфигурации для подключения к серверу...")
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            val jsch = JSch()
            session =
                jsch.getSession(preferenceRepository.user, preferenceRepository.host, 22)
            session.setPassword(preferenceRepository.password)
            session.setConfig(config)

            println("Подготовка конфигурации окончена.")
            println("Идёт подключение...")
            println("Начинается ожидание первичного ответа от сервера...")

            session.connect()
            println("Соединение с сервером установлено.")

            println("Идёт открытие канала...")
            channel = session.openChannel("shell")
            channel as ChannelShell

            val inputStream = PipedInputStream()
            outputStream = PipedOutputStream(inputStream)
            channel.inputStream = inputStream
            channel.connect()
            var k = 0

            s.forEach {
                runBlocking {
                    launch {
                        k++
                        delay(400)
                        outputStream.write("$it\n".toByteArray())
                        if (k == s.size) {
                            delay(1000)
                            channel.disconnect()
                            session.disconnect()
                        }
                    }
                }
            }

            Thread {
                runBlocking {
                    val tmp = ByteArray(32768)
                    val `in` = channel.inputStream

                    while (true) {
                        while (`in`.available() > 0) {
                            val i: Int = `in`.read(tmp, 0, 32768)
                            if (i < 0) break
                            print(String(tmp, 0, i))
                        }

                        try {
                            Thread.sleep(200)
                        } catch (ee: Exception) {
                        }
                    }
                }
            }.start()
            println("Соединение с каналом установлено.")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun getScreens(): Screens {
        val ids = ArrayList<Int>()
        val names = ArrayList<String>()

        val e =
            ArrayList(
                SSHUtil(preferenceRepository)
                    .execute("screen -list")
                    .split("\n")
            )

        print(e)

        if (e.size > 1) {
            // Remove unusual elements
            e.removeAt(0)
            e.removeAt(e.size - 1)
            e.removeAt(e.size - 1)

            e.forEach {
                it.let { s ->
                    var dot = 0
                    var dotFound = false
                    var pr = 0
                    var prFound = 0
                    s.forEachIndexed { i, c ->
                        if (c == '.' && !dotFound) {
                            dot = i
                            dotFound = true
                        }
                        if (c == '\t' && prFound < 2) {
                            pr = i
                            prFound++
                        }
                        if (dotFound && prFound == 2)
                            return@forEachIndexed
                    }
                    ids.add(
                        Integer.parseInt(
                            s.substring(0, dot).replace("\t", "")
                        )
                    )
                    names.add(
                        s.substring(dot + 1, pr).replace(" ", "")
                    )
                }
            }

            Log.e("IDS", ids.toString())
            Log.e("NAMES", names.toString())
        }
        return Screens(ids, names)
    }
}