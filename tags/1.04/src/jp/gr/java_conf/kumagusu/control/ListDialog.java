package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.R;
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
     * コンテキスト.
     */
    private Context context;

    /**
     * 選択行.
     */
    private int checkedItem = 0;

    /**
     * コンストラクタ.
     *
     * @param con コンテキスト
     */
    public ListDialog(Context con)
    {
        this.context = con;
    }

    /**
     * リストダイアログを表示する.
     *
     * @param icon アイコン
     * @param title ダイアログのタイトル
     * @param conditionNames 選択リスト
     * @param clickListener 選択リストのクリックイベントのリスナ
     */
    public void showDialog(Drawable icon, String title, String[] conditionNames, OnClickListener clickListener)
    {
        // カスタムViewを取得
        AlertDialog.Builder db = new AlertDialog.Builder(this.context);
        if (icon != null)
        {
            db.setIcon(icon);
        }
        db.setTitle(title);
        db.setItems(conditionNames, clickListener);
        db.setNegativeButton(R.string.ui_cancel, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // キャンセル処理なし
            }
        });

        db.show();
    }

    /**
     * リストダイアログを表示する.
     *
     * @param icon アイコン
     * @param title ダイアログのタイトル
     * @param conditionNames 選択リスト
     * @param chItem 選択アイテム
     * @param clickListener 選択リストのクリックイベントのリスナ
     */
    public void showSingleChoiceDialog(Drawable icon, String title, String[] conditionNames, int chItem,
            final OnClickListener clickListener)
    {
        this.checkedItem = chItem;

        // カスタムViewを取得
        AlertDialog.Builder db = new AlertDialog.Builder(this.context);
        if (icon != null)
        {
            db.setIcon(icon);
        }
        db.setTitle(title);
        db.setSingleChoiceItems(conditionNames, chItem, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ListDialog.this.checkedItem = which;
            }
        });
        db.setPositiveButton(R.string.ui_ok, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (clickListener != null)
                {
                    clickListener.onClick(dialog, ListDialog.this.checkedItem);
                }
            }
        });
        db.setNegativeButton(R.string.ui_cancel, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // キャンセル処理なし
            }
        });
        db.show();
    }
}
