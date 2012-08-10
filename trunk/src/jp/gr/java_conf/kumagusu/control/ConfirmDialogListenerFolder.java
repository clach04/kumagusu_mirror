package jp.gr.java_conf.kumagusu.control;

/**
 * 確認ダイアログのリスナ保持処理インタフェース.
 *
 * @author tarshi
 *
 */
public interface ConfirmDialogListenerFolder
{
    /**
     * リスナ保持データを取得する.
     *
     * @param listenerId リスナ保持データID
     * @return リスナ保持データ
     */
    ConfirmDialogListeners getConfirmDialogListeners(int listenerId);

    /**
     * リスナ保持データを追加する.
     *
     * @param listenerId リスナ保持データID
     * @param listeners リスナ保持データ
     */
    void putConfirmDialogListeners(int listenerId, ConfirmDialogListeners listeners);
}
