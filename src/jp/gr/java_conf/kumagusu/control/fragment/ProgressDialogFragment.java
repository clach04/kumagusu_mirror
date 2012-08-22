package jp.gr.java_conf.kumagusu.control.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

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
     * @return ダイアログ
     */
    public static ProgressDialogFragment newInstance(int iconId, int titleId, int messageId)
    {
        ProgressDialogFragment frag = new ProgressDialogFragment();

        Bundle args = new Bundle();

        args.putInt("iconId", iconId);
        args.putInt("titleId", titleId);
        args.putInt("messageId", messageId);

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

        // ダイアログ生成
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setCancelable(true);

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

        return this.progressDialog;
    }

    /**
     * メッセージを設定する.
     * @param message メッセージ
     */
    public void setMessage(CharSequence message)
    {
        this.progressDialog.setMessage(message);
    }
}
