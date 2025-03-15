package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import jp.gr.java_conf.kumagusu.tarshi.widget.dialog.DialogListeners;
import jp.gr.java_conf.kumagusu.R;

public final class ConfirmDialogFragment extends DialogFragment {
    public static final int POSITIVE_CAPTION_KIND_OK = 1;
    public static final int POSITIVE_CAPTION_KIND_YES = 2;
    private Drawable icon = null;
    private String message = null;
    private String title = null;

    public static ConfirmDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId, int positiveCaptionKind) {
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

    public static ConfirmDialogFragment newInstance(int listenerId, Drawable ic, String ttl, String msg, int positiveCaptionKind) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        frag.icon = ic;
        frag.title = ttl;
        frag.message = msg;
        Bundle args = new Bundle();
        args.putInt("listenerId", listenerId);
        args.putInt("title", 0);
        args.putInt("icon", 0);
        args.putInt("message", 0);
        args.putInt("positiveCaptionKind", positiveCaptionKind);
        frag.setArguments(args);
        return frag;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int listenerId = getArguments().getInt("listenerId");
        int titleId = getArguments().getInt("title");
        int iconId = getArguments().getInt("icon");
        int messageId = getArguments().getInt("message");
        int positiveCaptionKind = getArguments().getInt("positiveCaptionKind");
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        if (this.icon != null) {
            db.setIcon(this.icon);
        } else if (iconId != 0) {
            db.setIcon(iconId);
        }
        if (this.title != null) {
            db.setTitle(this.title);
        } else if (titleId != 0) {
            db.setTitle(titleId);
        }
        if (this.message != null) {
            db.setMessage(this.message);
        } else if (messageId != 0) {
            db.setMessage(messageId);
        }
        FragmentActivity activity = getActivity();
        ConfirmDialogListenerFolder listenerFolder = null;
        if (activity instanceof ConfirmDialogListenerFolder) {
            listenerFolder = (ConfirmDialogListenerFolder) activity;
        }
        boolean visibleButton = true;
        DialogInterface.OnClickListener okListener = null;
        DialogInterface.OnClickListener noListener = null;
        DialogInterface.OnClickListener cancelListener = null;
        if (listenerFolder != null) {
            listenerFolder.getConfirmDialogListeners(listenerId);
            DialogListeners listeners = listenerFolder.getConfirmDialogListeners(listenerId);
            if (listeners != null) {
                okListener = listeners.getOkOnClickListener();
                noListener = listeners.getNoOnClickListener();
                cancelListener = listeners.getCancelOnClickListener();
                visibleButton = listeners.isVisibleButton();
            }
        }
        if (visibleButton) {
            if (okListener == null) {
                okListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                };
            }
            switch (positiveCaptionKind) {
                case 1:
                    db.setPositiveButton(R.string.ui_ok, okListener);
                    break;
                case 2:
                    db.setPositiveButton(R.string.ui_yes, okListener);
                    break;
                default:
                    db.setPositiveButton(R.string.ui_ok, okListener);
                    break;
            }
            if (noListener == null || cancelListener == null) {
                if (noListener != null) {
                    db.setNegativeButton(R.string.ui_no, noListener);
                }
                if (cancelListener != null) {
                    db.setNegativeButton(R.string.ui_cancel, cancelListener);
                }
            } else {
                db.setNeutralButton(R.string.ui_no, noListener);
                db.setNegativeButton(R.string.ui_cancel, cancelListener);
            }
        }
        if (cancelListener != null) {
            final DialogInterface.OnClickListener cancelListenerFinal = cancelListener;
            db.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    cancelListenerFinal.onClick(dialog, -1);
                }
            });
        }
        return db.create();
    }

    public void setMessage(String msg) {
        if (getDialog() != null) {
            ((AlertDialog) getDialog()).setMessage(msg);
        }
    }
}
