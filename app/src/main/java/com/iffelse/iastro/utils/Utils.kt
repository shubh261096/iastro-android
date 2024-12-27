package com.iffelse.iastro.utils

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


object Utils {

    fun getSunSign(dob: Date): String {
        val calendar = Calendar.getInstance().apply { time = dob }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based in Calendar

        return when {
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Aquarius"
            (month == 2 && day >= 19) || (month == 3 && day <= 20) -> "Pisces"
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Aries"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Taurus"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Gemini"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Cancer"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Leo"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Virgo"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Libra"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Scorpio"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Sagittarius"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Capricorn"
            else -> "Unknown"
        }
    }

    fun closeKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // Find the current focused view
        view.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun openTimePicker(
        context: Context,
        dateFormatResult: DateFormatResult
    ) {
        // Get the current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog with 12-hour format
        val timePickerDialog = TimePickerDialog(
            context, // Or use 'context' if inside a Fragment
            { _, selectedHour, selectedMinute ->
                // Convert 24-hour time to 12-hour format with AM/PM
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val hourIn12Format =
                    if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
                val formattedMinute =
                    if (selectedMinute < 10) "0$selectedMinute" else "$selectedMinute"

                dateFormatResult.onDateSelected("$hourIn12Format:$formattedMinute $amPm")
                // Update the EditText with selected time in HH:MM AM/PM format
            }, hour, minute, false // 'false' makes it a 12-hour clock format
        )

        // Show the TimePickerDialog
        timePickerDialog.show()
    }

    fun openDatePicker(
        minDateString: String?,
        maxDateString: String?,
        initialDateString: String?,
        context: Context,
        dateFormatResult: DateFormatResult,
        dateFormat: String = "yyyy/MM/dd"
    ) {

        val dateForm = SimpleDateFormat(dateFormat, Locale.getDefault())
        val c = Calendar.getInstance()

        // Parse the initial date
        val initialDate = parseDate(initialDateString, dateForm)
        initialDate?.let {
            c.time = it
        }
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Format day and month with leading zero if needed
                val formattedDay = String.format("%02d", dayOfMonth)
                val formattedMonth =
                    String.format("%02d", monthOfYear + 1) // +1 since month starts from 0
                dateFormatResult.onDateSelected("$year/$formattedMonth/$formattedDay")
            }, year, month, day
        )

        // Set the minimum and maximum dates
        val minDate = parseDate(minDateString, dateForm)
        val maxDate = parseDate(maxDateString, dateForm)

        minDate?.let {
            datePickerDialog.datePicker.minDate = it.time
        }
        maxDate?.let {
            datePickerDialog.datePicker.maxDate = it.time
        }

        datePickerDialog.show()
    }

    private fun parseDate(dateString: String?, dateFormat: SimpleDateFormat): Date? {
        return try {
            dateString?.let { dateFormat.parse(it) }
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun convertTimestamp(input: String): String {
        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())

        // Parse the input date string
        val date = inputFormat.parse(input)

        // Return the formatted date string
        return if (date != null) {
            outputFormat.format(date)
        } else {
            "Invalid date"
        }
    }

    fun convertTo24HourFormat(timeIn12HourFormat: String): String {
        val inputFormat =
            SimpleDateFormat("hh:mm a", Locale.getDefault()) // Input format: 12-hour with AM/PM
        val outputFormat =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // Output format: 24-hour with seconds

        return try {
            // Parse the input time string into a Date object
            val parsedDate = inputFormat.parse(timeIn12HourFormat)

            // Format the Date object into the 24-hour format
            outputFormat.format(parsedDate ?: Date()) // In case of null, return current time
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    fun convertTo12HourFormat(time: String): String {
        // Create a SimpleDateFormat for 24-hour format input
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        // Create a SimpleDateFormat for 12-hour format output
        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Parse the 24-hour time string and format it into 12-hour format
        val date = inputFormat.parse(time)
        return outputFormat.format(date!!)
    }

    fun convertTo12HourFormatWithoutSeconds(time: String): String {
        // Create a SimpleDateFormat for 24-hour format input
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Create a SimpleDateFormat for 12-hour format output
        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Parse the 24-hour time string and format it into 12-hour format
        val date = inputFormat.parse(time)
        return outputFormat.format(date!!)
    }

    fun encodeToBase64(input: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Use java.util.Base64 for API 26 and above
            java.util.Base64.getEncoder().encodeToString(input.toByteArray())
        } else {
            // Use android.util.Base64 for API levels below 26
            android.util.Base64.encodeToString(input.toByteArray(), android.util.Base64.DEFAULT)
        }
    }

    fun generateUniquePaymentId(): String {
        // Generate a random UUID and replace hyphens with underscores
        val uuid = UUID.randomUUID().toString().replace("-", "_")

        // Get current timestamp
        val timestamp = System.currentTimeMillis().toString()

        // Combine both and limit the total length to 50 characters
        val combinedId = "order_${uuid}_$timestamp"

        // If the combinedId exceeds 50 characters, truncate it
        return if (combinedId.length > 50) combinedId.take(50) else combinedId
    }

    private var progressDialog: ProgressDialog? = null

    fun showProgress(context: Context?, message: String?) {
        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage(message)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    fun hideProgress() {
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }

    fun parseDate(dateStr: String): Date? {
        val patterns = arrayOf("yyyy-MM-dd", "yyyy/MM/dd")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                return sdf.parse(dateStr)
            } catch (e: ParseException) {
                // Ignore and try the next pattern
            }
        }
        return null // Return null if no pattern matches
    }


    fun getConnectivityStatus(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.state == NetworkInfo.State.CONNECTED
    }

    fun openWhatsApp(context: Context, phoneNumber: String) {
        val whatsappUri = "https://api.whatsapp.com/send?phone=$phoneNumber"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(whatsappUri)
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUri))
            context.startActivity(browserIntent)
        }
    }

    // Generic function to open app or browser link
     fun openLink(context: Context, appUri: String, webUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUri))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(browserIntent)
        }
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun convertTimeToMilliseconds(time: String): Long {
        val parts = time.split(":").map { it.toInt() }
        val hours = parts[0]
        val minutes = parts[1]
        val seconds = parts[2]
        return (hours * 3600 + minutes * 60 + seconds) * 1000L
    }



    interface DateFormatResult {
        fun onDateSelected(date: String)
    }


}