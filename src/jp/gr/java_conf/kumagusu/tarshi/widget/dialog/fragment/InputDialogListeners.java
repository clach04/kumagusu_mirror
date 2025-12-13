package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import android.view.View;
import jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment.InputDialogFragment;

public final class InputDialogListeners {
    private InputDialogFragment.OnClickInputDialogListener cancelOnClickListener;
    private InputDialogFragment.OnClickInputDialogListener okOnClickListener;
    private View.OnClickListener userButtonClickListener;
    private String userButtonText;

    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok) {
        this(ok, (InputDialogFragment.OnClickInputDialogListener) null, (View.OnClickListener) null, (String) null);
    }

    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok, View.OnClickListener userListener, String userText) {
        this(ok, (InputDialogFragment.OnClickInputDialogListener) null, userListener, userText);
    }

    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok, InputDialogFragment.OnClickInputDialogListener cancel) {
        this(ok, cancel, (View.OnClickListener) null, (String) null);
    }

    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok, InputDialogFragment.OnClickInputDialogListener cancel, View.OnClickListener userListener, String userText) {
        this.okOnClickListener = ok;
        this.cancelOnClickListener = cancel;
        this.userButtonClickListener = userListener;
        this.userButtonText = userText;
    }

    public InputDialogFragment.OnClickInputDialogListener getOkOnClickListener() {
        return this.okOnClickListener;
    }

    public InputDialogFragment.OnClickInputDialogListener getCancelOnClickListener() {
        return this.cancelOnClickListener;
    }

    public View.OnClickListener getUserButtonClickListener() {
        return this.userButtonClickListener;
    }

    public String getUserButtonText() {
        return this.userButtonText;
    }
}
