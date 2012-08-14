package jp.gr.java_conf.kumagusu.control;

/**
 * リストダイアログのリスナ保持処理インタフェース.
 *
 * @author tarshi
 *
 */
public interface ListDialogListenerFolder
{
    /**
     * リスナ保持データを取得する.
     *
     * @param listenerId リスナ保持データID
     * @return リスナ保持データ
     */
    DialogListeners getListDialogListeners(int listenerId);

    /**
     * リスナ保持データを追加する.
     *
     * @param listenerId リスナ保持データID
     * @param listeners リスナ保持データ
     */
    void putListDialogListeners(int listenerId, DialogListeners listeners);
}
