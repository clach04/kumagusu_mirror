package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.compat.EditorCompat;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.SingleLineTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 入力ダイアログ.
 *
 * @author tarshi
 */
public class InputDialogFragment extends DialogFragment
{
    /**
     * メッセージ表示View.
     */
    private TextView messageTextView = null;

    /**
     * 文字入力EditText.
     */
    private EditText editText = null;

    /**
     * ユーザーボタン.
     */
    private Button userButton = null;

    /**
     * 入力ダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param inputType インプットタイプ
     * @param messageId メッセージID
     * @return ダイアログ
     */
    public static InputDialogFragment newInstance(int listenerId, int iconId, int titleId, int inputType,
            int messageId)
    {
        return newInstance(listenerId, iconId, titleId, inputType, messageId, null);
    }

    /**
     * 入力ダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param inputType インプットタイプ
     * @param messageId メッセージID
     * @param initText 初期入力テキスト
     * @return ダイアログ
     */
    public static InputDialogFragment newInstance(int listenerId, int iconId, int titleId, int inputType,
            int messageId, String initText)
    {
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

    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int listenerId = getArguments().getInt("listenerId");
        int titleId = getArguments().getInt("titleId");
        int iconId = getArguments().getInt("iconId");
        int inputType = getArguments().getInt("inputType");
        int messageId = getArguments().getInt("messageId");
        String initText = getArguments().getString("initText");

        // アクティビティーからリスナ取得
        Activity activity = getActivity();

        InputDialogListeners listeners = null;

        if (activity instanceof InputDialogListenerFolder)
        {
            InputDialogListenerFolder listenerFolder = (InputDialogListenerFolder) activity;
            listeners = listenerFolder.getInputDialogListeners(listenerId);
        }

        final InputDialogFragment.OnClickInputDialogListener okListener;
        final InputDialogFragment.OnClickInputDialogListener cancelListener;
        View.OnClickListener userButtonOnClickListener;
        String userButtonText;

        if (listeners != null)
        {
            okListener = listeners.getOkOnClickListener();
            cancelListener = listeners.getCancelOnClickListener();
            userButtonOnClickListener = listeners.getUserButtonClickListener();
            userButtonText = listeners.getUserButtonText();
        }
        else
        {
            okListener = null;
            cancelListener = null;
            userButtonOnClickListener = null;
            userButtonText = null;
        }

        // View取得
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.input_dialog, null);

        // メッセージ設定
        if (messageId > 0)
        {
            this.messageTextView = (TextView) view.findViewById(messageId);
            this.messageTextView.setVisibility(TextView.VISIBLE);
        }

        // EditText設定
        this.editText = (EditText) view.findViewById(R.id.input_dialog_edit_text);

        this.editText.setTransformationMethod(SingleLineTransformationMethod.getInstance());

        if (inputType != 0)
        {
            this.editText.setInputType(inputType);
        }

        if (initText != null)
        {
            this.editText.setText(initText);
            this.editText.setSelection(initText.length());
        }

        // ユーザボタンを設定
        this.userButton = (Button) view.findViewById(R.id.input_dialog_add_button);

        if (userButtonOnClickListener != null)
        {
            this.userButton.setVisibility(View.VISIBLE);
            this.userButton.setOnClickListener(userButtonOnClickListener);
            this.userButton.setText(userButtonText);
        }
        else
        {
            this.userButton.setVisibility(View.GONE);
        }

        // ダイアログ生成
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());

        if (iconId > 0)
        {
            db.setIcon(iconId);
        }

        db.setTitle(titleId);

        db.setPositiveButton(R.string.ui_ok, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (okListener != null)
                {
                    okListener.onClick(editText.getText().toString());
                }
            }
        });

        db.setNegativeButton(R.string.ui_cancel, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (cancelListener != null)
                {
                    cancelListener.onClick(editText.getText().toString());
                }
            }
        });

        // 戻るキーによるキャンセルを処理
        db.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface d)
            {
                if (cancelListener != null)
                {
                    cancelListener.onClick(editText.getText().toString());
                }
            }
        });

        // ダイアログ生成
        AlertDialog alertDialog = db.create();
        alertDialog.setView(view, 0, 0, 0, 0);

        // IME表示
        EditorCompat.showIme4DialogEditText(alertDialog, this.editText, null, null);

        return alertDialog;
    }

    /**
     * 入力文字決定イベントのリスナのインタフェース.
     *
     * @author tarshi
     */
    public interface OnClickInputDialogListener
    {
        /**
         * 入力文字決定イベントを処理する.
         *
         * @param text 入力文字
         */
        void onClick(String text);
    }
}
