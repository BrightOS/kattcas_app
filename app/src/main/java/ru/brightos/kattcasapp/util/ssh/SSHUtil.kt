package ru.brightos.kattcasapp.util.ssh

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jcraft.jsch.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import ru.brightos.kattcasapp.data.PreferenceRepository
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*

class SSHUtil(val preferenceRepository: PreferenceRepository) {
    fun execute(vararg command: String): String {
        return runBlocking {
            return@runBlocking async {
                val jsch = JSch()
                var d = ""

                try {
                    val session: Session =
                        jsch.getSession(preferenceRepository.user, preferenceRepository.host, 22)
                    val config = Properties()

                    config["StrictHostKeyChecking"] = "no"
                    session.setConfig(config)
                    session.setPassword(preferenceRepository.password)
                    session.connect()

                    command.forEach {

                        val channel: Channel = session.openChannel("exec")
                        (channel as ChannelExec).setCommand(it)
                        channel.setInputStream(null)
                        channel.setErrStream(System.err)

                        val `in`: InputStream = channel.getInputStream()
                        channel.connect()

                        val tmp = ByteArray(1024)

                        while (true) {
                            while (`in`.available() > 0) {
                                val i: Int = `in`.read(tmp, 0, 1024)
                                if (i < 0) break
                                d += String(tmp, 0, i)
                            }

                            if (channel.isClosed()) {
                                break
                            }

                            try {
                                Thread.sleep(1000)
                            } catch (ee: Exception) {
                            }
                        }
                        channel.disconnect()
                    }
                    session.disconnect()
                } catch (e: Exception) {
                    println(e.message)
                }
                return@async d
            }.await()
        }
    }
}