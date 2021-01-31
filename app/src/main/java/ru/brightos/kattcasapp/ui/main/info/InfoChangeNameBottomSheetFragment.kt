package ru.brightos.kattcasapp.ui.main.info

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.layout_bottom_change_name.*
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.data.PreferenceRepository

class InfoChangeNameBottomSheetFragment(
    private val preferenceRepository: PreferenceRepository,
    private val model: InfoViewModel
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface: DialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(object :
            BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet)
                ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet)
                ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable)
            }
        })
        return dialog
    }

    private fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable {
        val shapeAppearanceModel =
            ShapeAppearanceModel.builder(context, 0, R.style.CustomShapeAppearanceBottomSheetDialog)
                .build()
        val currentMaterialShapeDrawable = bottomSheet.background as MaterialShapeDrawable
        val newMaterialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        newMaterialShapeDrawable.initializeElevationOverlay(requireContext())
        newMaterialShapeDrawable.fillColor = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimaryDark
            )
        )
        newMaterialShapeDrawable.tintList = currentMaterialShapeDrawable.tintList
        newMaterialShapeDrawable.elevation = currentMaterialShapeDrawable.elevation
        newMaterialShapeDrawable.strokeWidth = currentMaterialShapeDrawable.strokeWidth
        newMaterialShapeDrawable.strokeColor = currentMaterialShapeDrawable.strokeColor
        return newMaterialShapeDrawable
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_bottom_change_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nickname.hint = preferenceRepository.localNickname

        nickname.imeOptions = EditorInfo.IME_ACTION_DONE

        save_nickname.setOnClickListener {
            model.setLocalNickname(nickname.text.toString())
            this.dismiss()
        }

        nickname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                model.setLocalNickname(nickname.text.toString())
                true
            } else {
                false
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}