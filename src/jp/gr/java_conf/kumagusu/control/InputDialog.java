package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.compat.EditorCompat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.method.SingleLineTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * テキスト入力ダイアログ.
 *
 * @author tarshi
 */
public final class InputDialog extends OldStyleDialog
{
    /**
     * コンテキスト.
     */
    private Context context;

    /**
     * テキスト入力ボックス.
     */
    private EditText edtInput;

    /**
     * 初期設定する入力テキスト.
     */
    private String initText = null;

    /**
     * ダイアログ.
     */
    private Dialog dialog = null;

    /**
     * コンストラクタ.
     *
     * @param con コンテキスト
     */
    public InputDialog(Context con)
    {
        this.context = con;
    }

    /**
     * テキスト入力ダイアログを表示する.
     *
     * @param title タイトル
     * @param inputType インプットタイプ
     * @param okListener Ok処理リスナ
     * @param cancelListener Cancel処理リスナ
     */
    public void showDialog(String title, int inputType, OnClickListener okListener, OnClickListener cancelListener)
    {
        showDialog(this.context.getResources().getDrawable(R.drawable.icon), title, inputType, okListener,
                cancelListener);
    }

    /**
     * テキスト入力ダイアログを表示する.
     *
     * @param icon アイコン
     * @param title タイトル
     * @param inputType インプットタイプ
     * @param okListener Ok処理リスナ
     * @param cancelListener Cancel処理リスナ
     */
    public void showDialog(Drawable icon, String title, int inputType, OnClickListener okListener,
            OnClickListener cancelListener)
    {
        showDialog(icon, title, inputType, okListener, cancelListener, null, null);
    }

    /**
     * テキスト入力ダイアログを表示する.
     *
     * @param icon アイコン
     * @param title タイトル
     * @param inputType インプットタイプ
     * @param okListener Ok処理リスナ
     * @param cancelListener Cancel処理リスナ
     * @param userButtonClickListener ボタンのクリックイベントリスナ
     * @param userButtonText ボタンのテキスト
     */
    public void showDialog(Drawable icon, String title, int inputType, OnClickListener okListener,
            OnClickListener cancelListener, View.OnClickListener userButtonClickListener, String userButtonText)
    {
        // カスタムViewを取得
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.input_dialog, null);

        // EditTextを設定
        edtInput = (EditText) view.findViewById(R.id.input_dialog_edit_text);

        edtInput.setTransformationMethod(SingleLineTransformationMethod.getInstance());

        if (inputType != 0)
        {
            edtInput.setInputType(inputType);
        }

        if (this.initText != null)
        {
            edtInput.setText(this.initText);
            edtInput.setSelection(this.initText.length());
        }

        // ユーザボタン設定
        Button userButton = (Button) view.findViewById(R.id.input_dialog_add_button);

        if (userButtonClickListener != null)
        {
            userButton.setVisibility(View.VISIBLE);
            userButton.setOnClickListener(userButtonClickListener);
            userButton.setText(userButtonText);
        }
        else
        {
            userButton.setVisibility(View.GONE);
        }

        // ダイアログ生成
        AlertDialog.Builder db = new AlertDialog.Builder(this.context);

        if (icon != null)
        {
            db.setIcon(icon);
        }

        db.setTitle(title);

        if (okListener != null)
        {
            db.setPositiveButton(R.string.ui_ok, okListener);
        }

        // キャンセル処理が指定されてなければ、デフォルトのキャンセル処理を設定
        if (cancelListener == null)
        {
            cancelListener = new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int w)
                {
                    // キャンセル処理なし
                }
            };
        }

        db.setNegativeButton(R.string.ui_cancel, cancelListener);

        // 戻るキーによるキャンセルを処理
        final OnClickListener cancelListenerFinal = cancelListener;

        db.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface d)
            {
                cancelListenerFinal.onClick(d, -1);
            }
        });

        // ダイアログ生成
        AlertDialog alertDialog = db.create();
        this.dialog = alertDialog;

        alertDialog.setView(view, 0, 0, 0, 0);

        // ダイアログのクローズ時の処理
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface d)
            {
                // ダイアログ消去後処理
                postDismissDialog(context);
            }
        });

        // ダイアログ表示前処理
        preShowDialog(context);

        // ダイアログを表示（IME表示）
        EditorCompat.showDialogWithIme(dialog, edtInput, null, null);
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

    /**
     * カーソル位置に文字列を貼り付ける.
     *
     * @param pasteString 貼り付ける文字列
     */
    public void pasteText(String pasteString)
    {
        int cStart = edtInput.getSelectionStart();
        int cEnd = edtInput.getSelectionEnd();
        Editable memoEditable = edtInput.getText();

        memoEditable.replace(Math.min(cStart, cEnd), Math.max(cStart, cEnd), pasteString);
    }

    /**
     * ダイアログを消去する.
     */
    public void dismissDialog()
    {
        if (this.dialog != null)
        {
            this.dialog.dismiss();
        }
    }
}
