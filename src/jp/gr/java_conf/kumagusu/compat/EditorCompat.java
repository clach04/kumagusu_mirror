package jp.gr.java_conf.kumagusu.compat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * エディタ関連の互換性吸収処理.
 *
 * @author tarshi
 *
 */
public final class EditorCompat
{
    /**
     * インスタンス化させない.
     */
    private EditorCompat()
    {
    }

    /**
     * ダイアログで入力メソッドを表示する.
     *
     * @param dialog ダイアログ
     * @param view IME表示先View
     */
    @SuppressLint("NewApi")
    public static void showDialogWithIme(final Dialog dialog, final EditText view,
            final DialogInterface.OnShowListener showListener, final View.OnFocusChangeListener focusChangeListener)
    {
        // IME表示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) // 2.2以上
        {
            dialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface d)
                {
                    EditorCompat.setImeVisibility(dialog.getContext(), dialog.getWindow(), true, view);

                    if (showListener != null)
                    {
                        showListener.onShow(d);
                    }
                }
            });

            // ダイアログ表示
            dialog.show();
        }
        else
        {
            view.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (hasFocus)
                    {
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e)
                        {
                            Log.w("EditorCompat", "Sleep Exception", e);
                        }

                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                        if (focusChangeListener != null)
                        {
                            focusChangeListener.onFocusChange(v, hasFocus);
                        }
                    }
                }
            });

            // ダイアログ表示
            dialog.show();
            view.requestFocus();
        }
    }

    /**
     * エディタのInputTypeを設定する.
     *
     * @param editText 設定先View
     * @param editable 編集可のときtrue
     */
    public static void setEditorInputType(EditText editText, boolean editable)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) // 2.2未満
        {
            // InputType設定
            if (editable)
            {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            else
            {
                editText.setRawInputType(InputType.TYPE_NULL);
            }
        }
    }

    /**
     * IME表示状態を変更する.
     *
     * @param con コンテキスト
     * @param win ウィンドウ
     * @param editable 表示するときtrue
     * @param editText view
     */
    public static void setImeVisibility(Context con, Window win, boolean editable, EditText editText)
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            Log.w("EditorCompat", "Sleep Exception", e);
        }

        if (editable)
        {
            // IME表示
            InputMethodManager imm = (InputMethodManager) con.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
            {
                imm.showSoftInput(editText, 0);
            }
        }
        else
        {
            // IME非表示
            InputMethodManager imm = (InputMethodManager) con.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
            {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }
}
