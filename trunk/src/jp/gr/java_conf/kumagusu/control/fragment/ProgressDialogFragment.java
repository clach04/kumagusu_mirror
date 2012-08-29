package jp.gr.java_conf.kumagusu.control.fragment;

import jp.gr.java_conf.kumagusu.MainApplication;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * プログレスダイアログ.
 *
 * @author tarshi
 *
 */
public final class ProgressDialogFragment extends DialogFragment
{
    /**
     * プログレスダイアログ.
     */
    private ProgressDialog progressDialog;

    /**
     * メッセージ.
     */
    private CharSequence message = null;

    /**
     * 入力ダイアログを生成する.
     *
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param cancelable キャンセル可能のときtrue
     * @return ダイアログ
     */
    public static ProgressDialogFragment newInstance(int iconId, int titleId, int messageId, boolean cancelable)
    {
        ProgressDialogFragment frag = new ProgressDialogFragment();

        Bundle args = new Bundle();

        args.putInt("iconId", iconId);
        args.putInt("titleId", titleId);
        args.putInt("messageId", messageId);
        args.putBoolean("cancelable", cancelable);

        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("ProgressDialogFragment", "*** Start onCreate()");

        // 表示中プログレスダイアログを保存
        MainApplication.getInstance(getActivity()).setProgressDialog(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.d("ProgressDialogFragment", "*** Start onCreateDialog()");

        // バンドル変数取得
        int iconId = getArguments().getInt("iconId");
        int titleId = getArguments().getInt("titleId");
        int messageId = getArguments().getInt("messageId");
        boolean cancelable = getArguments().getBoolean("cancelable");

        // ダイアログ生成
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setCancelable(cancelable); // キャンセル可否
        this.setCancelable(cancelable);

        // アイコン設定
        if (iconId != 0)
        {
            this.progressDialog.setIcon(iconId);
        }

        // タイトル設定
        if (titleId != 0)
        {
            this.progressDialog.setTitle(titleId);
        }

        // メッセージ設定（保存メッセージを優先）
        CharSequence msg = null;

        if ((savedInstanceState != null) && (savedInstanceState.containsKey("message")))
        {
            msg = savedInstanceState.getCharSequence("message");
        }

        if ((msg == null) && (messageId != 0))
        {
            msg = getString(messageId);
        }

        if (msg != null)
        {
            setMessage(msg);
        }

        return this.progressDialog;
    }

    @Override
    public void onResume()
    {
        Log.d("ProgressDialogFragment", "*** Start onResume()");

        super.onResume();

        // 非表示中ならダイアログ消去
        if (!MainApplication.getInstance(getActivity()).continueDisplayingProgressDialog(this))
        {
            // 表示中断（消去はcontinueDisplayingProgressDialogで実施）
            return;
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d("ProgressDialogFragment", "*** Start onDestroy()");

        // 表示中プログレスダイアログをクリア
        MainApplication.getInstance(getActivity()).setProgressDialog(null);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle args)
    {
        Log.d("ProgressDialogFragment", "*** Start onSaveInstanceState()");

        if (this.message != null)
        {
            args.putCharSequence("message", this.message);
        }

        super.onSaveInstanceState(args);
    }

    /**
     * メッセージを設定する.
     *
     * @param msg メッセージ
     */
    public void setMessage(CharSequence msg)
    {
        this.message = msg;
        this.progressDialog.setMessage(msg);
    }
}