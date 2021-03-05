package ru.brightos.kattcasapp.ui.main.info

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.fragment_information.*
import kotlinx.android.synthetic.main.layout_bottom_change_name.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import ru.brightos.kattcasapp.`object`.Directories
import ru.brightos.kattcasapp.`object`.Screens
import ru.brightos.kattcasapp.data.PreferenceRepository
import ru.brightos.kattcasapp.util.ssh.SSHUtil

class InfoViewModel : ViewModel() {
    lateinit var preferenceRepository: PreferenceRepository

    fun init(preferenceRepository: PreferenceRepository) {
        this.preferenceRepository = preferenceRepository
    }

    private val _uptime: MutableLiveData<String> = MutableLiveData()
    val uptime: LiveData<String>
        get() = _uptime

    private val _localNickname: MutableLiveData<String> = MutableLiveData()
    val localNickname: LiveData<String>
        get() = _localNickname

    private val _directories: MutableLiveData<ArrayList<Directories>> = MutableLiveData()
    val directories: LiveData<ArrayList<Directories>>
        get() = _directories

    fun setLocalNickname(nickname: String) {
        _localNickname.postValue(nickname)
        preferenceRepository.localNickname = nickname
    }

    fun updateDirectories() {
        Thread {
            run {
                _directories.postValue(
                    runBlocking {
                        async {
                            val dirs = ArrayList<Directories>()
                            val screens = getScreens()

                            getDirectories().let { it ->
                                if (it.size > 0)
                                    it.forEach {
                                        (it in screens.names).let { active ->
                                            var pos = 0
                                            screens.names.forEachIndexed { index, name ->
                                                if (it == name) {
                                                    pos = index
                                                    return@forEachIndexed
                                                }
                                            }
                                            if (!active)
                                                dirs.add(Directories(0, it, false))
                                            else
                                                dirs.add(Directories(screens.ids[pos], it, true))
                                        }
                                    }
                            }

                            return@async dirs
                        }.await()
                    }
                )
            }
        }.start()
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

    private fun getDirectories(): ArrayList<String> {
        val list = ArrayList(
            SSHUtil(preferenceRepository)
                .execute("ls -d ~/android_scripts/*/")
                .split("\n")
        )

        val finalList = ArrayList<String>()

        if (list.size > 0) {
            list.removeLast()
            list.forEach {
                var pos = 0
                val s = it.substring(0, it.length - 1)
                s.forEachIndexed { i, c ->
                    if (c == '/')
                        pos = i
                }
                finalList.add(s.substring(pos + 1, s.length))
            }
        }

        return finalList
    }

    fun updateUptime() {
        Thread {
            run {
                val d =
                    SSHUtil(preferenceRepository)
                        .execute("uptime -p")
                        .split(" ")

                if (d.size > 1) {
                    println(d)
                    when {
                        d[2].contains("week") -> {
                            _uptime.postValue(
                                (Integer.parseInt(d[1]) * 7 + Integer.parseInt(d[3])).let {
                                    if (it in 11..19) {
                                        return@let "$it дней"
                                    }
                                    when (it % 10) {
                                        1 -> {
                                            return@let "$it день"
                                        }
                                        2, 3, 4 -> {
                                            return@let "$it дня"
                                        }
                                        else -> {
                                            return@let "$it дней"
                                        }
                                    }
                                }
                            )
                        }
                        d[2].contains("day") -> {
                            _uptime.postValue(
                                Integer.parseInt(d[1]).let {
                                    if (it in 11..19) {
                                        return@let "$it дней"
                                    }
                                    when (it % 10) {
                                        1 -> {
                                            return@let "$it день"
                                        }
                                        2, 3, 4 -> {
                                            return@let "$it дня"
                                        }
                                        else -> {
                                            return@let "$it дней"
                                        }
                                    }
                                }
                            )
                        }
                        d[2].contains("hour") -> {
                            _uptime.postValue(
                                Integer.parseInt(d[1]).let {
                                    if (it in 11..19) {
                                        return@let "$it часов"
                                    }
                                    when (it % 10) {
                                        1 -> {
                                            return@let "$it час"
                                        }
                                        2, 3, 4 -> {
                                            return@let "$it часа"
                                        }
                                        else -> {
                                            return@let "$it часов"
                                        }
                                    }
                                }
                            )
                        }
                        d[2].contains("min") -> {
                            _uptime.postValue(
                                Integer.parseInt(d[1]).let {
                                    if (it in 11..19) {
                                        return@let "$it минут"
                                    }
                                    when (it % 10) {
                                        1 -> {
                                            return@let "$it минута"
                                        }
                                        2, 3, 4 -> {
                                            return@let "$it минуты"
                                        }
                                        else -> {
                                            return@let "$it минут"
                                        }
                                    }
                                }
                            )
                        }
                        d[2].contains("sec") -> {
                            _uptime.postValue(
                                Integer.parseInt(d[1]).let {
                                    if (it in 11..19) {
                                        return@let "$it секунд"
                                    }
                                    when (it % 10) {
                                        1 -> {
                                            return@let "$it секунда"
                                        }
                                        2, 3, 4 -> {
                                            return@let "$it секунды"
                                        }
                                        else -> {
                                            return@let "$it секунд"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }.start()
    }
}