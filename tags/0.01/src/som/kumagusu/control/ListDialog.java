package som.kumagusu.control;

import som.kumagusu.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;

/**
 * リストダイアログ.
 *
 * @author tadashi
 *
 */
public final class ListDialog
{
    /**
     * インスタンス化させない.
     */
    private ListDialog()
    {
    }

    /**
     * リストダイアログを表示する.
     *
     * @param context コンテキスト
     * @param icon アイコン
     * @param title ダイアログのタイトル
     * @param conditionNames 選択リスト
     * @param clickListener 選択リストのクリックイベントのリスナ
     */
    public static void showDialog(Context context, Drawable icon, String title, String[] conditionNames,
            OnClickListener clickListener)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        if (icon != null)
        {
            b.setIcon(icon);
        }
        b.setTitle(title);
        b.setItems(conditionNames, clickListener);
        b.setNegativeButton(R.string.ui_cancel, new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // キャンセル処理なし
            }
        });
        b.show();
    }
}
