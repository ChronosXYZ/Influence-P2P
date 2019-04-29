/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence.views

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import io.github.chronosx88.influence.R

object ViewUtils {
    fun setupEditTextDialog(context: Context, message: String): Pair<AlertDialog.Builder, EditText> {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(message)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.setSingleLine()
        input.layoutParams = lp

        alertDialog.setView(input)

        return Pair(alertDialog, input)
    }
}