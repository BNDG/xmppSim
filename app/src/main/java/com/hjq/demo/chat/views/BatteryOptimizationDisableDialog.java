package com.hjq.demo.chat.views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hjq.demo.R;
import com.hjq.demo.chat.utils.BatteryHelper;

public class BatteryOptimizationDisableDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static DialogFragment newInstance() {
        return new BatteryOptimizationDisableDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.battery_optimization_disable_dialog_title)
                .setMessage(R.string.battery_optimization_disable_dialog_message)
                .setPositiveButton(android.R.string.ok, this)
                .setCancelable(false)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        BatteryHelper.sendIgnoreButteryOptimizationIntent(getActivity());
    }
}