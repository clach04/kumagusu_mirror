package jp.gr.java_conf.kumagusu.control.fragment;

/**
 * 入力ダイアログのリスナ保持処理インタフェース.
 *
 * @author tarshi
 *
 */
public interface InputDialogListenerFolder
{
    /**
     * リスナ保持データを取得する.
     *
     * @param listenerId リスナ保持データID
     * @return リスナ保持データ
     */
    InputDialogListeners getInputDialogListeners(int listenerId);

    /**
     * リスナ保持データを追加する.
     *
     * @param listenerId リスナ保持データID
     * @param listeners リスナ保持データ
     */
    void putInputDialogListeners(int listenerId, InputDialogListeners listeners);
}
