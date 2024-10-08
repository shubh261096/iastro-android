package com.iffelse.iastro.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import java.util.Calendar
import java.util.Date

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


}