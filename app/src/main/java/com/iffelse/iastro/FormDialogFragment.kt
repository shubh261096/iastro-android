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
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import org.json.JSONObject

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
        val jsonObject = JSONObject()

        firebaseHelper.checkIfNameExists(KeyStorePref.getString("userId")!!) { hasName, dataSnapShot ->
            if (hasName) {
                val name = dataSnapShot!!.child("name").getValue(String::class.java)
                val dob = dataSnapShot.child("dob").getValue(String::class.java)
                val gender = dataSnapShot.child("gender").getValue(String::class.java)
                val placeOfBirth = dataSnapShot.child("placeOfBirth").getValue(String::class.java)
                val timeOfBirth = dataSnapShot.child("time").getValue(String::class.java)

                jsonObject.put("dob", dob)
                jsonObject.put("gender", gender)
                jsonObject.put("placeOfBirth", placeOfBirth)
                jsonObject.put("time", timeOfBirth)

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

            val url = "https://www.apsdeoria.com/apszone/api/v2/qa/test/vendor/sendEmail"

            // Adding body
            jsonObject.put("name", dialogFormBinding.etName.text.toString().trim())
            jsonObject.put("phoneNumber", KeyStorePref.getString("userId"))
            jsonObject.put("astrologerName", astrologer.name)
            jsonObject.put("message", dialogFormBinding.etMessage.text.toString().trim())
            OkHttpNetworkProvider.post(
                url,
                jsonObject,
                mutableMapOf(),
                null,
                null,
                responseType = JSONObject::class.java,
                object : OkHttpNetworkProvider.NetworkListener<JSONObject> {
                    override fun onResponse(response: JSONObject?) {
                    }

                    override fun onError(error: BaseErrorModel?) {

                    }
                }
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
