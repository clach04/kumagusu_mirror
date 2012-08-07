package jp.gr.java_conf.kumagusu.compat;

import jp.gr.java_conf.kumagusu.commons.Utilities;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
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
     * @param con コンテキスト
     * @param dialog ダイアログ
     * @param view IME表示先View
     */
    @SuppressLint("NewApi")
    public static void showDialogWithIme(final Context con, final Dialog dialog, final EditText view)
    {
        // IME表示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) // 2.2以上
        {
            dialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface d)
                {
                    Utilities.setImeVisibility(con, true, view);
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
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
}
