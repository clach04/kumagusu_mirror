package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import jp.gr.java_conf.kumagusu.tarshi.widget.dialog.DialogListeners;

public interface ConfirmDialogListenerFolder {
    DialogListeners getConfirmDialogListeners(int i);

    void putConfirmDialogListeners(int i, DialogListeners dialogListeners);
}
