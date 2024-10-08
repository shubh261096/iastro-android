package com.iffelse.iastro

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.iffelse.iastro.databinding.DialogFormBinding
import com.iffelse.iastro.model.Astrologer
import com.iffelse.iastro.utils.Utils

class FormDialogFragment(private val context: Context, private val astrologer: Astrologer) :
    DialogFragment() {

    private lateinit var dialogFormBinding: DialogFormBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate the form layout
        dialogFormBinding = DialogFormBinding.inflate(inflater)
        builder.setView(dialogFormBinding.root)

        // Get the references to form elements

        val firebaseHelper = FirebaseHelper()

        firebaseHelper.checkIfNameExists(KeyStorePref.getString("userId")!!) { hasName, dataSnapShot ->
            if (hasName) {
                val name = dataSnapShot!!.child("name").getValue(String::class.java)
                dialogFormBinding.etName.setText(name)
            }
        }
        // Submit button logic
        dialogFormBinding.btnSubmit.setOnClickListener {

            Utils.closeKeyboard(context, it)

            // Form submission data
            val formData = FormData(
                name = dialogFormBinding.etName.text.toString().trim(),
                astrologerName = astrologer.name,
                message = dialogFormBinding.etMessage.text.toString().trim()
            )

            // Save the form data
            firebaseHelper.saveFormSubmission(KeyStorePref.getString("userId")!!, formData)
            showSuccessMessage()
        }

        return builder.create()
    }

    private fun showSuccessMessage() {


        dialogFormBinding.clSuccess.visibility = View.VISIBLE
        dialogFormBinding.clForm.visibility = View.GONE

        // Auto-dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dismiss() // Dismiss the form dialog
        }, 3000)
    }
}
