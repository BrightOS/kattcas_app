package ru.brightos.kattcasapp.ui.main.info

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_information.*
import kotlinx.android.synthetic.main.layout_card.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.brightos.kattcasapp.App
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.data.PreferenceRepository
import ru.brightos.kattcasapp.ui.auth.AuthActivity
import ru.brightos.kattcasapp.ui.main.MainActivity
import ru.brightos.kattcasapp.util.adapter.InfoAdapter


class InfoFragment : Fragment(R.layout.fragment_information) {
    lateinit var model: InfoViewModel
    lateinit var preferenceRepository: PreferenceRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var uptimeUpdating = false
        var dirsUpdating = false

        info_main_title.text = "Добро пожаловать, ${preferenceRepository.localNickname}!"

        model.localNickname.observe(viewLifecycleOwner) {
            info_main_title.text = "Добро пожаловать, ${it}!"
        }

        model.uptime.observe(viewLifecycleOwner) {
            uptime_title.text = it
            uptimeUpdating = false
            if (!dirsUpdating)
                info_refresh.isRefreshing = false
        }

        info_refresh.setProgressBackgroundColorSchemeColor(
            resources.getColor(
                R.color.refresh_background,
                context?.theme
            )
        )

        info_refresh.setColorSchemeColors(
            resources.getColor(
                R.color.refresh_foreground,
                context?.theme
            )
        )

        info_refresh.setOnRefreshListener {
            runBlocking {
                uptimeUpdating = true
                dirsUpdating = true
                model.updateUptime()
                model.updateDirectories()
            }
        }

        info_main_title.setOnClickListener {
            changeNickname()
        }

        smile.setOnClickListener {
            changeNickname()
        }

        host_subtitle.text = preferenceRepository.host
        user_subtitle.text = preferenceRepository.user

        info_recycler.layoutManager = LinearLayoutManager(context)
        info_recycler.isNestedScrollingEnabled = false

        model.directories.observe(viewLifecycleOwner) {
            runBlocking {
                launch {
                    if (it.size > 0) {
                        info_recycler.adapter =
                            InfoAdapter(
                                requireContext(),
                                preferenceRepository,
                                it
                            ) { id ->
                                activity?.bottom_navigation!!.selectedItemId = R.id.console
                                (activity as MainActivity).openScreenID = id
                            }

                        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

                        TransitionManager.beginDelayedTransition(
                            info_constraint,
                            sharedAxis
                        )

                        info_title.text = it.size.toString()

                        recycler_warn.visibility = View.GONE
                        info_recycler.visibility = View.VISIBLE
                    } else {
                        recycler_warn.setText("Данные для авторизации неверны. Для того, чтобы продолжить пользоваться приложением, измените данные для подключения, нажав на плитку \"Данные для подключения\"")
                        recycler_warn.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorBad
                            )
                        )
                    }
                }
            }
            dirsUpdating = false
            if (!uptimeUpdating)
                info_refresh.isRefreshing = false
        }

        conn_info_card.setOnClickListener {
            val intent = Intent(activity, AuthActivity::class.java)
            val bundle = Bundle()
            bundle.putBoolean("from_info", true)
            intent.putExtras(bundle)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun changeNickname() =
        InfoChangeNameBottomSheetFragment(preferenceRepository, model).show(
            parentFragmentManager,
            "change_nickname"
        )

    override fun onResume() {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

        TransitionManager.beginDelayedTransition(
            info_constraint,
            sharedAxis
        )

        model.updateUptime()
        model.updateDirectories()

        info_recycler.adapter = null
        recycler_warn.visibility = View.VISIBLE
        info_recycler.visibility = View.GONE

        super.onResume()
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
        model = ViewModelProvider(requireActivity()).get(InfoViewModel::class.java)
        runBlocking { model.init(preferenceRepository) }
    }
}
