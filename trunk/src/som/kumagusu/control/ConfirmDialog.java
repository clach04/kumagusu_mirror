package som.kumagusu.control;

import som.kumagusu.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;

/**
 * 確認ダイアログ.
 *
 * @author tarshi
 *
 */
public final class ConfirmDialog
{
    /**
     * 肯定ボタンのキャプション種別.
     *
     * @author tarshi
     *
     */
    public enum PositiveCaptionKind
    {
        /**
         * OK.
         */
        OK,

        /**
         * はい.
         */
        YES,
    }

    /**
     * インスタンス化させない.
     */
    private ConfirmDialog()
    {
    }

    /**
     * 確認ダイアログを表示する.
     *
     * @param context コンテキスト
     * @param icon アイコン
     * @param title タイトル
     * @param message メッセージ
     * @param kind 肯定ボタンのキャプション種別
     * @param okListener Okを処理するリスナ
     * @param cancelListener Cancelを処理するリスナ
     */
    public static void showDialog(Context context, Drawable icon, String title, String message,
            PositiveCaptionKind kind, OnClickListener okListener, OnClickListener cancelListener)
    {
        showDialog(context, icon, title, message, kind, okListener, cancelListener, null);
    }

    /**
     * 確認ダイアログを表示する.
     *
     * @param context コンテキスト
     * @param icon アイコン
     * @param title タイトル
     * @param message メッセージ
     * @param kind 肯定ボタンのキャプション種別
     * @param okListener OKを処理するリスナ
     * @param noListener NOを処理するリスナ
     * @param cancelListener Cancelを処理するリスナ
     */
    public static void showDialog(Context context, Drawable icon, String title, String message,
            PositiveCaptionKind kind, OnClickListener okListener, OnClickListener noListener,
            OnClickListener cancelListener)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(context);

        // アイコン
        if (icon == null)
        {
            icon = context.getResources().getDrawable(R.drawable.icon);
        }
        b.setIcon(icon);

        // タイトル
        b.setTitle(title);

        // メッセージ
        if (message != null)
        {
            b.setMessage(message);
        }

        // 肯定ボタン
        if (okListener == null)
        {
            // OKボタンは常に表示
            okListener = new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                }
            };
        }
        switch (kind)
        {
        case OK:
            b.setPositiveButton(R.string.ui_ok, okListener);
            break;

        case YES:
            b.setPositiveButton(R.string.ui_yes, okListener);
            break;

        default:
            b.setPositiveButton(R.string.ui_ok, okListener);
            break;
        }

        // 中立ボタン
        if ((noListener != null) && (cancelListener != null))
        {
            b.setNeutralButton(R.string.ui_no, noListener);
            b.setNegativeButton(R.string.ui_cancel, cancelListener);

        }
        else
        {
            if (noListener != null)
            {
                b.setNegativeButton(R.string.ui_no, noListener);
            }
            if (cancelListener != null)
            {
                b.setNegativeButton(R.string.ui_cancel, cancelListener);
            }
        }

        b.show();
    }
}
