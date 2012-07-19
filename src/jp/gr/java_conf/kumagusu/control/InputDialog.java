package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.method.SingleLineTransformationMethod;
import android.widget.EditText;

/**
 * テキスト入力ダイアログ.
 *
 * @author tarshi
 */
public final class InputDialog
{
    /**
     * テキスト入力ボックス.
     */
    private EditText edtInput;

    /**
     * 初期設定する入力テキスト.
     */
    private String initText = null;

    /**
     * テキスト入力ダイアログを表示する.
     *
     * @param context コンテキスト
     * @param title タイトル
     * @param inputType インプットタイプ
     * @param okListener Ok処理リスナ
     * @param cancelListener Cancel処理リスナ
     */
    public void showDialog(Context context, String title, int inputType, OnClickListener okListener,
            OnClickListener cancelListener)
    {
        edtInput = new EditText(context);
        edtInput.setTransformationMethod(SingleLineTransformationMethod.getInstance());
        edtInput.setInputType(inputType);

        if (this.initText != null)
        {
            edtInput.setText(this.initText);
            edtInput.setSelection(this.initText.length());
        }

        AlertDialog.Builder db = new AlertDialog.Builder(context);

        db.setIcon(R.drawable.icon);
        db.setTitle(title).setView(edtInput);

        if (okListener != null)
        {
            db.setPositiveButton(R.string.ui_ok, okListener);
        }

        if (cancelListener == null)
        {
            cancelListener = new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // キャンセル処理なし
                }
            };
        }

        db.setNegativeButton(R.string.ui_cancel, cancelListener).show();
    }

    /**
     * 入力テキストを取得する.
     *
     * @return 入力テキスト
     */
    public String getText()
    {
        return (edtInput != null) ? edtInput.getText().toString() : "";
    }

    /**
     * 入力テキストを設定する.
     *
     * @param text 入力テキスト
     */
    public void setText(String text)
    {
        this.initText = text;

        if (edtInput != null)
        {
            edtInput.setText(text);
        }
    }
}
