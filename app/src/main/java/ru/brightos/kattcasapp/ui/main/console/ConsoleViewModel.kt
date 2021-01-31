package ru.brightos.kattcasapp.ui.main.console

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jcraft.jsch.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.brightos.kattcasapp.data.PreferenceRepository
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*

class ConsoleViewModel : ViewModel() {
    lateinit var preferenceRepository: PreferenceRepository

    private val _status: MutableLiveData<String> = MutableLiveData()
    val status: LiveData<String>
        get() = _status

    fun init(preferenceRepository: PreferenceRepository, command: String?) {
        this.preferenceRepository = preferenceRepository
        runBlocking {
            GlobalScope.launch {
                connect()
                if (command != "screen -rd null") command?.let { execute(it) }
            }
        }
    }

    private lateinit var session: Session
    private lateinit var channel: Channel
    private lateinit var outputStream: PipedOutputStream

    fun clear() {
        _status.value = " "

        if (this::channel.isInitialized)
            channel.disconnect()
        if (this::session.isInitialized)
            session.disconnect()
    }

    fun connect() {
        try {
            println("Подготовка конфигурации для подключения к серверу...")
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            val jsch = JSch()
            session = jsch.getSession(preferenceRepository.user, preferenceRepository.host, 22)
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
            println("Соединение с каналом установлено.")

            Thread {
                runBlocking {
                    val tmp = ByteArray(32768)
                    val `in` = channel.inputStream

                    while (true) {
                        while (`in`.available() > 0) {
                            val i: Int = `in`.read(tmp, 0, 32768)
                            if (i < 0) break
                            _status.postValue(String(tmp, 0, i))
                        }

                        try {
                            Thread.sleep(200)
                        } catch (ee: Exception) {
                        }
                    }
                }
            }.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun execute(s: String) {
        outputStream.write("$s\n".toByteArray())
    }

    fun disconnect() {
        println("Закрытие соединения...")
        println("Соединение закрыто успешно.")
    }

    override fun onCleared() {
        super.onCleared()
        if (this::channel.isInitialized)
            channel.disconnect()
        if (this::session.isInitialized)
            session.disconnect()
    }
}