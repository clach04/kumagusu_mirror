package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public final class EditorCompat {
    private EditorCompat() {
    }

    @SuppressLint({"NewApi"})
    public static void showDialogWithIme(final Dialog dialog, final EditText view, final DialogInterface.OnShowListener showListener, final View.OnFocusChangeListener focusChangeListener) {
        if (Build.VERSION.SDK_INT >= 8) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface d) {
                    EditorCompat.setImeVisibility(dialog.getContext(), dialog.getWindow(), true, view);
                    if (showListener != null) {
                        showListener.onShow(d);
                    }
                }
            });
            dialog.show();
            return;
        }
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(5);
                    if (focusChangeListener != null) {
                        focusChangeListener.onFocusChange(v, hasFocus);
                    }
                }
            }
        });
        dialog.show();
        view.requestFocus();
    }

    @SuppressLint({"NewApi"})
    public static void showIme4DialogEditText(final Dialog dialog, final EditText view, final DialogInterface.OnShowListener showListener, final View.OnFocusChangeListener focusChangeListener) {
        if (Build.VERSION.SDK_INT >= 8) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface d) {
                    EditorCompat.setImeVisibility(dialog.getContext(), dialog.getWindow(), true, view);
                    if (showListener != null) {
                        showListener.onShow(d);
                    }
                }
            });
            return;
        }
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(5);
                    if (focusChangeListener != null) {
                        focusChangeListener.onFocusChange(v, hasFocus);
                    }
                }
            }
        });
        view.requestFocus();
    }

    public static void setEditorInputType(EditText editText, boolean editable) {
        if (Build.VERSION.SDK_INT >= 8) {
            return;
        }
        if (editable) {
            editText.setInputType(655361);
        } else {
            editText.setRawInputType(0);
        }
    }

    public static void setImeVisibility(Context con, Window win, boolean editable, EditText editText) {
        if (editable) {
            InputMethodManager imm = (InputMethodManager) con.getSystemService("input_method");
            if (imm != null) {
                imm.showSoftInput(editText, 0);
                return;
            }
            return;
        }
        InputMethodManager imm2 = (InputMethodManager) con.getSystemService("input_method");
        if (imm2 != null) {
            imm2.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
}
