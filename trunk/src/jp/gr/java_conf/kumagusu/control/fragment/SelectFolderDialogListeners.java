package jp.gr.java_conf.kumagusu.control.fragment;

/**
 * フォルダ選択ダイアログリスナ保持データ.
 *
 * @author tarshi
 *
 */
public final class SelectFolderDialogListeners
{
    /**
     * OKを処理するリスナ.
     */
    private SelectFolderDialogFragment.OnSelectFolderListener okOnClickListener;

    /**
     * Cancelを処理するリスナ.
     */
    private SelectFolderDialogFragment.OnSelectFolderListener cancelOnClickListener;

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     */
    public SelectFolderDialogListeners(SelectFolderDialogFragment.OnSelectFolderListener ok)
    {
        this(ok, null);
    }

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     * @param cancel Cancelを処理するリスナ
     */
    public SelectFolderDialogListeners(SelectFolderDialogFragment.OnSelectFolderListener ok,
            SelectFolderDialogFragment.OnSelectFolderListener cancel)
    {
        this.okOnClickListener = ok;
        this.cancelOnClickListener = cancel;
    }

    /**
     * OKを処理するリスナを返す.
     *
     * @return OKまたはYesを処理するリスナ
     */
    public SelectFolderDialogFragment.OnSelectFolderListener getOkOnClickListener()
    {
        return okOnClickListener;
    }

    /**
     * Cancelを処理するリスナを返す.
     *
     * @return Cancelを処理するリスナ
     */
    public SelectFolderDialogFragment.OnSelectFolderListener getCancelOnClickListener()
    {
        return cancelOnClickListener;
    }
}
