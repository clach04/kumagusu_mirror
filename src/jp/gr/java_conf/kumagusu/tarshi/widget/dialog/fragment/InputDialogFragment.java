package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.SingleLineTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.tarshi.widget.dialog.util.EditorCompat;

public class InputDialogFragment extends DialogFragment {
    /* access modifiers changed from: private */
    public EditText editText = null;
    private TextView messageTextView = null;
    private Button userButton = null;

    public interface OnClickInputDialogListener {
        void onClick(String str);
    }

    public static InputDialogFragment newInstance(int listenerId, int iconId, int titleId, int inputType, int messageId) {
        return newInstance(listenerId, iconId, titleId, inputType, messageId, (String) null);
    }

    public static InputDialogFragment newInstance(int listenerId, int iconId, int titleId, int inputType, int messageId, String initText) {
        InputDialogFragment frag = new InputDialogFragment();
        Bundle args = new Bundle();
        args.putInt("listenerId", listenerId);
        args.putInt("titleId", titleId);
        args.putInt("iconId", iconId);
        args.putInt("inputType", inputType);
        args.putInt("messageId", messageId);
        args.putString("initText", initText);
        frag.setArguments(args);
        return frag;
    }

    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        OnClickInputDialogListener okListener;
        final OnClickInputDialogListener cancelListener;
        View.OnClickListener userButtonOnClickListener;
        String userButtonText;
        int listenerId = getArguments().getInt("listenerId");
        int titleId = getArguments().getInt("titleId");
        int iconId = getArguments().getInt("iconId");
        int inputType = getArguments().getInt("inputType");
        int messageId = getArguments().getInt("messageId");
        String initText = getArguments().getString("initText");
        FragmentActivity activity = getActivity();
        InputDialogListeners listeners = null;
        if (activity instanceof InputDialogListenerFolder) {
            listeners = ((InputDialogListenerFolder) activity).getInputDialogListeners(listenerId);
        }
        if (listeners != null) {
            okListener = listeners.getOkOnClickListener();
            cancelListener = listeners.getCancelOnClickListener();
            userButtonOnClickListener = listeners.getUserButtonClickListener();
            userButtonText = listeners.getUserButtonText();
        } else {
            okListener = null;
            cancelListener = null;
            userButtonOnClickListener = null;
            userButtonText = null;
        }
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.input_dialog, (ViewGroup) null);
        if (messageId > 0) {
            this.messageTextView = (TextView) view.findViewById(messageId);
            this.messageTextView.setVisibility(0);
        }
        this.editText = (EditText) view.findViewById(R.id.input_dialog_edit_text);
        this.editText.setTransformationMethod(SingleLineTransformationMethod.getInstance());
        if (inputType != 0) {
            this.editText.setInputType(inputType);
        }
        if (initText != null) {
            this.editText.setText(initText);
            this.editText.setSelection(initText.length());
        }
        this.userButton = (Button) view.findViewById(R.id.input_dialog_add_button);
        if (userButtonOnClickListener != null) {
            this.userButton.setVisibility(0);
            this.userButton.setOnClickListener(userButtonOnClickListener);
            this.userButton.setText(userButtonText);
        } else {
            this.userButton.setVisibility(8);
        }
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        if (iconId > 0) {
            db.setIcon(iconId);
        }
        db.setTitle(titleId);
        final OnClickInputDialogListener onClickInputDialogListener = okListener;
        db.setPositiveButton(R.string.ui_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (onClickInputDialogListener != null) {
                    onClickInputDialogListener.onClick(InputDialogFragment.this.editText.getText().toString());
                }
            }
        });
        db.setNegativeButton(R.string.ui_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (cancelListener != null) {
                    cancelListener.onClick(InputDialogFragment.this.editText.getText().toString());
                }
            }
        });
        db.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface d) {
                if (cancelListener != null) {
                    cancelListener.onClick(InputDialogFragment.this.editText.getText().toString());
                }
            }
        });
        AlertDialog alertDialog = db.create();
        alertDialog.setView(view, 0, 0, 0, 0);
        EditorCompat.showIme4DialogEditText(alertDialog, this.editText, (DialogInterface.OnShowListener) null, (View.OnFocusChangeListener) null);
        return alertDialog;
    }
}
