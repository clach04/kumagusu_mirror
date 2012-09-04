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
     * プログレスダイアログ小分類ID.
     */
    private int progressDialogSid = -1;

    /**
     * プログレスダイアログ小分類IDを返す.
     *
     * @return プログレスダイアログ小分類ID
     */
    public int getProgressDialogSid()
    {
        return this.progressDialogSid;
    }

    /**
     * プログレスダイアログ小分類IDを設定する.
     *
     * @param sid プログレスダイアログ小分類ID
     */
    public void setProgressDialogSid(int sid)
    {
        this.progressDialogSid = sid;
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
     * ダイアログを消去.
     */
    private boolean goDismiss = false;

    /**
     * 入力ダイアログを生成する.
     *
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param cancelable キャンセル可能のときtrue
     * @return ダイアログ
     */
    private static ProgressDialogFragment newInstance(int iconId, int titleId, int messageId, boolean cancelable)
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

        // ダイアログID
        if ((savedInstanceState != null) && (savedInstanceState.containsKey("progressDialogId")))
        {
            this.progressDialogId = savedInstanceState.getInt("progressDialogId");

            Log.d("ProgressDialogFragment", " id:" + this.progressDialogId);

            if (this.progressDialogId >= 0)
            {
                // プログレスダイアログを旧IDを指定し共通情報に保存
                if (!MainApplication.getInstance(getActivity()).registProgressDialog(this.progressDialogId, this))
                {
                    // 廃棄済みプログレスダイアログ
                    this.goDismiss = true;
                }
            }
        }

        // メッセージ設定（保存メッセージを優先）
        CharSequence msg = null;

        if (this.message == null)
        {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey("message")))
            {
                msg = savedInstanceState.getCharSequence("message");
            }

            if ((msg == null) && (messageId != 0))
            {
                msg = getString(messageId);
            }
        }
        else
        {
            msg = this.message;
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
        Log.d("ProgressDialogFragment", "*** Start onResume() id:" + this.progressDialogId);

        super.onResume();

        if (this.goDismiss)
        {
            Log.d("ProgressDialogFragment", "Destroy progress dialog!! id:" + this.progressDialogId);
            dismiss();
        }
    }

    @Override
    public void onPause()
    {
        Log.d("ProgressDialogFragment", "*** Start onPause() id:" + this.progressDialogId);

        super.onPause();
    }

    @Override
    public void onStop()
    {
        Log.d("ProgressDialogFragment", "*** Start onStop() id:" + this.progressDialogId);

        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        Log.d("ProgressDialogFragment", "*** Start onDestroy() id:" + this.progressDialogId);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle args)
    {
        Log.d("ProgressDialogFragment", "*** Start onSaveInstanceState() id:" + this.progressDialogId);

        // プログレスダイアログID
        args.putInt("progressDialogId", this.progressDialogId);

        // メッセージ
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
        Log.d("ProgressDialogFragment", "*** Start setMessage() id:" + this.progressDialogId);

        this.message = msg;

        if (this.progressDialog != null)
        {
            this.progressDialog.setMessage(msg);
        }
    }

    /**
     * プログレスダイアログを表示する.
     *
     * @param act Activity
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param cancelable キャンセル可能か？
     * @return プログレスダイアログID
     */
    public static int showProgressDialog(FragmentActivity act, int iconId, int titleId, int messageId,
            boolean cancelable)
    {
        synchronized (MainApplication.getInstance(act).getLockObject("ProgressDialog"))
        {
            Log.d("ProgressDialogFragment", "*** Start showProgressDialog()");

            int id = -1;

            try
            {
                // ダイアログ生成
                ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(iconId, titleId, messageId,
                        cancelable);

                // 新規表示時、バンドルデータからプログレスダイアログIDを取得
                id = MainApplication.getInstance(act).registProgressDialog(dialog);
                dialog.progressDialogId = id;

                // 表示
                dialog.show(act.getSupportFragmentManager(), "");
            }
            catch (Exception e)
            {
                Log.d("MainApplication", "Progress dialog show error", e);
            }

            return id;
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
                Log.d("ProgressDialogFragment", "*** Start dismissProgressDialog() id:" + id);

                ProgressDialogFragment dialog = MainApplication.getInstance(act).clearProgressDialog(id);

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
