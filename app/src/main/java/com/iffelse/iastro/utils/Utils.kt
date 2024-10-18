package com.iffelse.iastro.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.core.content.ContextCompat.getSystemService
import com.iffelse.iastro.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        dateFormat: String = "dd/MM/yyyy"
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
                dateFormatResult.onDateSelected("$formattedDay/$formattedMonth/$year")
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
        val inputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
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

    interface DateFormatResult {
        fun onDateSelected(date: String)
    }


}