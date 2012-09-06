package jp.gr.java_conf.kumagusu.control.fragment;

/**
 * フォルダ選択ダイアログのリスナ保持処理インタフェース.
 *
 * @author tarshi
 *
 */
public interface SelectFolderDialogListenerFolder
{
    /**
     * リスナ保持データを取得する.
     *
     * @param listenerId リスナ保持データID
     * @return リスナ保持データ
     */
    SelectFolderDialogListeners getSelectFolderDialogListeners(int listenerId);

    /**
     * リスナ保持データを追加する.
     *
     * @param listenerId リスナ保持データID
     * @param listeners リスナ保持データ
     */
    void putSelectFolderDialogListeners(int listenerId, SelectFolderDialogListeners listeners);
}
