/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package com.bouygues.bysafe.util;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.bouygues.bysafe.R;

public class DialogUtil {

	public static void showConfirmDialog(Context context, @StringRes int title,
			DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog d = new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(R.string.dialog_confirm_message)
				.setPositiveButton(R.string.dialog_confirm_positive_button, (dialog, which) -> {
					dialog.dismiss();
					positiveClickListener.onClick(dialog, which);
				})
				.setNegativeButton(R.string.dialog_confirm_negative_button, (dialog, which) -> dialog.dismiss())
				.show();
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
	}

	public static void showMessageDialog(Context context, String title, String msg) {
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton(R.string.dialog_button_ok, (dialog, which) -> dialog.dismiss())
				.show();
	}

}
