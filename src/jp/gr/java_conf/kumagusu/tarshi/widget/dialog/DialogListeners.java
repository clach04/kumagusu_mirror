package jp.gr.java_conf.kumagusu.tarshi.widget.dialog;

import android.content.DialogInterface;

public final class DialogListeners {
    private DialogInterface.OnClickListener cancelOnClickListener;
    private DialogInterface.OnClickListener noOnClickListener;
    private DialogInterface.OnClickListener okOnClickListener;
    private boolean visibleButton = false;

    public DialogListeners(DialogInterface.OnClickListener ok) {
        this.okOnClickListener = ok;
        this.noOnClickListener = null;
        this.cancelOnClickListener = null;
    }

    public DialogListeners(DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        this.okOnClickListener = ok;
        this.noOnClickListener = null;
        this.cancelOnClickListener = cancel;
    }

    public DialogListeners(DialogInterface.OnClickListener ok, DialogInterface.OnClickListener no, DialogInterface.OnClickListener cancel) {
        this.okOnClickListener = ok;
        this.noOnClickListener = no;
        this.cancelOnClickListener = cancel;
    }

    public DialogListeners() {
        this((DialogInterface.OnClickListener) null, (DialogInterface.OnClickListener) null, (DialogInterface.OnClickListener) null);
    }

    public DialogInterface.OnClickListener getOkOnClickListener() {
        return this.okOnClickListener;
    }

    public DialogInterface.OnClickListener getNoOnClickListener() {
        return this.noOnClickListener;
    }

    public DialogInterface.OnClickListener getCancelOnClickListener() {
        return this.cancelOnClickListener;
    }

    public boolean isVisibleButton() {
        return this.visibleButton;
    }
}
