package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment.SelectFolderDialogFragment;

public final class SelectFolderDialogListeners {
    private SelectFolderDialogFragment.OnSelectFolderListener cancelOnClickListener;
    private SelectFolderDialogFragment.OnSelectFolderListener okOnClickListener;

    public SelectFolderDialogListeners(SelectFolderDialogFragment.OnSelectFolderListener ok) {
        this(ok, (SelectFolderDialogFragment.OnSelectFolderListener) null);
    }

    public SelectFolderDialogListeners(SelectFolderDialogFragment.OnSelectFolderListener ok, SelectFolderDialogFragment.OnSelectFolderListener cancel) {
        this.okOnClickListener = ok;
        this.cancelOnClickListener = cancel;
    }

    public SelectFolderDialogFragment.OnSelectFolderListener getOkOnClickListener() {
        return this.okOnClickListener;
    }

    public SelectFolderDialogFragment.OnSelectFolderListener getCancelOnClickListener() {
        return this.cancelOnClickListener;
    }
}
