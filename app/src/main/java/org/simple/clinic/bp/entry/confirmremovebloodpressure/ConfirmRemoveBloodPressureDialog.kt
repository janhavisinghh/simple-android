package org.simple.clinic.bp.entry.confirmremovebloodpressure

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class ConfirmRemoveBloodPressureDialog : AppCompatDialogFragment() {
  companion object {
    private const val KEY_BP_UUID = "bloodPressureMeasurementUuid"

    fun show(bloodPressureMeasurementUuid: UUID, fragmentManager: FragmentManager) {
      val fragmentTag = "fragment_confirm_remove_blood_pressure"

      val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val args = Bundle()
      args.putSerializable(KEY_BP_UUID, bloodPressureMeasurementUuid)

      val fragment = ConfirmRemoveBloodPressureDialog()
      fragment.arguments = args

      fragmentManager
          .beginTransaction()
          .add(fragment, fragmentTag)
          .commitNowAllowingStateLoss()
    }
  }

  @Inject
  lateinit var controller: ConfirmRemoveBloodPressureDialogController

  private var removeBloodPressureListener: RemoveBloodPressureListener? = null

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()
  private val onStarts = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.bloodpressureentry_remove_bp_title)
        .setMessage(R.string.bloodpressureentry_remove_bp_message)
        .setPositiveButton(R.string.bloodpressureentry_remove_bp_confirm, null)
        .setNegativeButton(R.string.bloodpressureentry_remove_bp_cancel, null)
        .create()

    onStarts
        .take(1)
        .subscribe { setupDialog() }

    return dialog
  }

  override fun onStart() {
    super.onStart()
    onStarts.onNext(Any())
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    removeBloodPressureListener = context as? RemoveBloodPressureListener
    if (removeBloodPressureListener == null) {
      throw ClassCastException("$context must implement RemoveBloodPressureListener")
    }
  }

  private fun setupDialog() {
    bindUiToController(
        ui = this,
        events = Observable.merge(dialogCreates(), removeClicks()),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun removeClicks(): Observable<UiEvent> {
    val button = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    return RxView.clicks(button)
        .doOnNext { removeBloodPressureListener?.onBloodPressureRemoved() }
        .map { ConfirmRemoveBloodPressureDialogRemoveClicked }
  }

  private fun dialogCreates(): Observable<UiEvent> {
    val bloodPressureMeasurementUuid = arguments!!.getSerializable(KEY_BP_UUID) as UUID
    return Observable.just(ConfirmRemoveBloodPressureDialogCreated(bloodPressureMeasurementUuid))
  }

  interface RemoveBloodPressureListener {
    fun onBloodPressureRemoved()
  }
}
