package jp.gr.java_conf.kumagusu.control.fragment;

import jp.gr.java_conf.kumagusu.MainApplication;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
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

        if (iconId != 0)
        {
            this.progressDialog.setIcon(iconId);
        }

        if (titleId != 0)
        {
            this.progressDialog.setTitle(titleId);
        }

        if (messageId != 0)
        {
            this.progressDialog.setMessage(getString(messageId));
        }

        // 表示中プログレスダイアログを保存
        MainApplication.getInstance(getActivity()).setProgressDialog(this);

        return this.progressDialog;
    }

    @Override
    public void onActivityCreated(Bundle args)
    {
        // 表示中プログレスダイアログを保存
        MainApplication.getInstance(getActivity()).setProgressDialog(this);

        super.onActivityCreated(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // 表示中プログレスダイアログを保存
        MainApplication.getInstance(getActivity()).setProgressDialog(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // 表示中プログレスダイアログを保存
        MainApplication.getInstance(getActivity()).setProgressDialog(this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle args)
    {
        // 表示中プログレスダイアログをクリア
        // MainApplication.getInstance(getActivity()).setProgressDialog(null);

        super.onSaveInstanceState(args);
    }

    @Override
    public void onDestroy()
    {
        // 表示中プログレスダイアログをクリア
        MainApplication.getInstance(getActivity()).setProgressDialog(null);

        super.onDestroy();
    }

    /**
     * メッセージを設定する.
     *
     * @param message メッセージ
     */
    public void setMessage(CharSequence message)
    {
        this.progressDialog.setMessage(message);
    }
}
