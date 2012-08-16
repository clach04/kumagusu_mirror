package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * 確認ダイアログ.
 *
 * @author tarshi
 */
public final class ConfirmDialogFragment extends DialogFragment
{
    /**
     * 肯定ボタンのキャプション種別「OK」.
     */
    public static final int POSITIVE_CAPTION_KIND_OK = 1;

    /**
     * はい.
     */
    public static final int POSITIVE_CAPTION_KIND_YES = 2;

    /**
     * 確認ダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param iconId アイコン
     * @param titleId タイトル
     * @param messageId メッセージ
     * @param positiveCaptionKind 肯定ボタンのキャプション種別
     * @return 確認ダイアログ
     */
    public static ConfirmDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId,
            int positiveCaptionKind)
    {
        if (iconId == 0)
        {
            iconId = R.drawable.icon;
        }

        ConfirmDialogFragment frag = new ConfirmDialogFragment();

        Bundle args = new Bundle();

        args.putInt("listenerId", listenerId);
        args.putInt("title", titleId);
        args.putInt("icon", iconId);
        args.putInt("message", messageId);
        args.putInt("positiveCaptionKind", positiveCaptionKind);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int listenerId = getArguments().getInt("listenerId");
        int titleId = getArguments().getInt("title");
        int iconId = getArguments().getInt("icon");
        int messageId = getArguments().getInt("message");
        int positiveCaptionKind = getArguments().getInt("positiveCaptionKind");

        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());

        // アイコン
        db.setIcon(iconId);

        // タイトル
        db.setTitle(titleId);

        // メッセージ
        if (messageId != 0)
        {
            db.setMessage(messageId);
        }

        // アクティビティーからリスナ取得
        Activity activity = getActivity();

        ConfirmDialogListenerFolder listenerFolder = null;
        if (activity instanceof ConfirmDialogListenerFolder)
        {
            listenerFolder = (ConfirmDialogListenerFolder) activity;
        }

        DialogInterface.OnClickListener okListener = null;
        DialogInterface.OnClickListener noListener = null;
        DialogInterface.OnClickListener cancelListener = null;

        if (listenerFolder != null)
        {
            listenerFolder.getConfirmDialogListeners(listenerId);

            DialogListeners listeners = listenerFolder.getConfirmDialogListeners(listenerId);

            if (listeners != null)
            {
                okListener = listeners.getOkOnClickListener();
                noListener = listeners.getNoOnClickListener();
                cancelListener = listeners.getCancelOnClickListener();
            }
        }

        // 肯定ボタン
        if (okListener == null)
        {
            // OKボタンは常に表示
            okListener = new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                }
            };
        }

        switch (positiveCaptionKind)
        {
        case POSITIVE_CAPTION_KIND_OK:
            db.setPositiveButton(R.string.ui_ok, okListener);
            break;

        case POSITIVE_CAPTION_KIND_YES:
            db.setPositiveButton(R.string.ui_yes, okListener);
            break;

        default:
            db.setPositiveButton(R.string.ui_ok, okListener);
            break;
        }

        // 中立ボタン
        if ((noListener != null) && (cancelListener != null))
        {
            db.setNeutralButton(R.string.ui_no, noListener);
            db.setNegativeButton(R.string.ui_cancel, cancelListener);

        }
        else
        {
            if (noListener != null)
            {
                db.setNegativeButton(R.string.ui_no, noListener);
            }
            if (cancelListener != null)
            {
                db.setNegativeButton(R.string.ui_cancel, cancelListener);
            }
        }

        // 戻るキーによるキャンセルを処理
        if (cancelListener != null)
        {
            final DialogInterface.OnClickListener cancelListenerFinal = cancelListener;

            db.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    cancelListenerFinal.onClick(dialog, -1);
                }
            });
        }

        return db.create();
    }
}
