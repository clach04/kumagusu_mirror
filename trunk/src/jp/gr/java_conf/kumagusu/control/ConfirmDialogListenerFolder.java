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
}
