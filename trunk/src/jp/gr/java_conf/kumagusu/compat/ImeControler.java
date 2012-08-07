package jp.gr.java_conf.kumagusu.compat;

import jp.gr.java_conf.kumagusu.commons.Utilities;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * IME制御処理.
 *
 * @author tarshi
 *
 */
public final class ImeControler
{
    /**
     * インスタンス化させない.
     */
    private ImeControler()
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
}
