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

class FormDialogFragment(private val context: Context) : DialogFragment() {

    private lateinit var dialogFormBinding: DialogFormBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate the form layout
        dialogFormBinding = DialogFormBinding.inflate(inflater)
        builder.setView(dialogFormBinding.root)

        // Get the references to form elements
//        val submitButton = view.findViewById<Button>(R.id.btnSubmit)

        // Submit button logic
        dialogFormBinding.btnSubmit.setOnClickListener {

            val firebaseHelper = FirebaseHelper()
            // Form submission data
            val formData = FormData(
                name = dialogFormBinding.etName.text.toString().trim(),
                email = dialogFormBinding.etEmail.text.toString().trim(),
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
