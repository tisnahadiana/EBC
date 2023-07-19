package id.deeromptech.ebc.dialog

import android.app.Activity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.deeromptech.ebc.databinding.ResetPasswordDialogBinding

fun Activity.setupBottomSheetDialog(
    onSendClick: (String) -> Unit
){
    val dialog = BottomSheetDialog(this)
    val binding = ResetPasswordDialogBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    dialog.show()

    binding.btnSend.setOnClickListener {
        val email = binding.edEmail.text.toString().trim()
        onSendClick(email)
    }

    binding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }
}