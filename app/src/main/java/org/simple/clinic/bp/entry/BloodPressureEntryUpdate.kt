package org.simple.clinic.bp.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class BloodPressureEntryUpdate : Update<BloodPressureEntryModel, BloodPressureEntryEvent, BloodPressureEntryEffect> {
  override fun update(
      model: BloodPressureEntryModel,
      event: BloodPressureEntryEvent
  ): Next<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return when (event) {
      is SystolicChanged -> if (isSystolicValueComplete(event.systolic)) {
        next(model.withSystolic(event.systolic), HideBpErrorMessage, ChangeFocusToDiastolic)
      } else {
        next(model.withSystolic(event.systolic), HideBpErrorMessage as BloodPressureEntryEffect)
      }

      is DiastolicChanged -> next(model.withDiastolic(event.diastolic), HideBpErrorMessage)

      is DiastolicBackspaceClicked -> if (model.diastolic.isNotEmpty()) {
        next(model.deleteDiastolicLastDigit())
      } else {
        val updatedModel = model.deleteSystolicLastDigit()
        next(updatedModel, ChangeFocusToSystolic, SetSystolic(updatedModel.systolic))
      }

      else -> noChange()
    }
  }

  private fun isSystolicValueComplete(systolicText: String): Boolean {
    return (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
        || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
  }
}