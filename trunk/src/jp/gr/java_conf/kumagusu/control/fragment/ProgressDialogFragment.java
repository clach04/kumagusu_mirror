package jp.gr.java_conf.kumagusu.control.fragment;

import jp.gr.java_conf.kumagusu.MainApplication;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
     * プログレスダイアログID.
     */
    private int progressDialogId = -1;

    /**
     * プログレスダイアログIDを取得する.
     *
     * @return プログレスダイアログID
     */
    public int getProgressDialogId()
    {
        return this.progressDialogId;
    }

    /**
     * プログレスダイアログ.
     */
    private ProgressDialog progressDialog;

    /**
     * メッセージ.
     */
    private CharSequence message = null;

    /**
     * ダイアログを永続化する?
     */
    private boolean progressDialogPersistence = false;

    /**
     * 入力ダイアログを生成する.
     *
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param cancelable キャンセル可能のときtrue
     * @param persistence ダイアログを永続化するときtrue
     * @return ダイアログ
     */
    private static ProgressDialogFragment newInstance(int iconId, int titleId, int messageId, boolean cancelable,
            boolean persistence)
    {
        ProgressDialogFragment frag = new ProgressDialogFragment();

        Bundle args = new Bundle();

        args.putInt("iconId", iconId);
        args.putInt("titleId", titleId);
        args.putInt("messageId", messageId);
        args.putBoolean("cancelable", cancelable);
        args.putBoolean("persistence", persistence);

        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("ProgressDialogFragment", "*** Start onCreate()");

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
        this.progressDialogPersistence = getArguments().getBoolean("persistence");

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

        // ダイアログID
        if ((savedInstanceState != null) && (savedInstanceState.containsKey("progressDialogId")))
        {
            // 再表示時、バンドルデータからプログレスダイアログIDを取得
            this.progressDialogId = savedInstanceState.getInt("progressDialogId");
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

        if (this.progressDialogId >= 0)
        {
            // 表示中ダイアログと異なる場合廃棄
            if (this.progressDialogId != MainApplication.getInstance(getActivity()).getProgresDialogId())
            {
                dismiss();
                return;
            }

            // プログレスダイアログを旧IDを指定し共通情報に保存
            MainApplication.getInstance(getActivity()).registProgressDialog(this.progressDialogId, this);
        }
        else
        {
            // プログレスダイアログを共通情報に保存
            this.progressDialogId = MainApplication.getInstance(getActivity()).registProgressDialog(this);
        }
    }

    @Override
    public void onStop()
    {
        Log.d("ProgressDialogFragment", "*** Start onStop()");

        super.onStop();

        if (!this.progressDialogPersistence)
        {
            dismiss();
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d("ProgressDialogFragment", "*** Start onDestroy()");

        super.onDestroy();

        if (progressDialogPersistence)
        {
            // 表示中プログレスダイアログのインスタンスのみクリア
            MainApplication.getInstance(getActivity()).registProgressDialog(this.progressDialogId, null);
        }
        else
        {
            // 表示中プログレスダイアログの登録解除
            MainApplication.getInstance(getActivity()).unregistProgressDialog(this.progressDialogId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle args)
    {
        Log.d("ProgressDialogFragment", "*** Start onSaveInstanceState()");

        if (progressDialogPersistence)
        {
            // プログレスダイアログID
            args.putInt("progressDialogId", this.progressDialogId);

            // メッセージ
            if (this.message != null)
            {
                args.putCharSequence("message", this.message);
            }
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

    /**
     * プログレスダイアログを表示する.
     *
     * @param act Activity
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param cancelable キャンセル可能か？
     * @param persistence ダイアログを永続化するときtrue
     * @return プログレスダイアログ
     */
    public static ProgressDialogFragment showProgressDialog(FragmentActivity act, int iconId, int titleId,
            int messageId, boolean cancelable, boolean persistence)
    {
        synchronized (MainApplication.getInstance(act).getLockObject("ProgressDialog"))
        {
            ProgressDialogFragment dialog = null;

            try
            {
                dialog = ProgressDialogFragment.newInstance(iconId, titleId, messageId, cancelable, persistence);
                dialog.show(act.getSupportFragmentManager(), "");
            }
            catch (Exception e)
            {
                Log.d("MainApplication", "Progress dialog show error", e);
            }

            return dialog;
        }
    }

    /**
     * プログレスダイアログを消去する.
     *
     * @param act Activity
     * @param id プログレスダイアログID
     */
    public static void dismissProgressDialog(FragmentActivity act, int id)
    {
        synchronized (MainApplication.getInstance(act).getLockObject("ProgressDialog"))
        {
            try
            {
                ProgressDialogFragment dialog = MainApplication.getInstance(act).getProgressDialog(id);
                if (dialog != null)
                {
                    dialog.dismiss();
                }
            }
            catch (Exception ex)
            {
                Log.d("MainApplication", "Dialog dismiss error.", ex);
            }
        }
    }
}
