package jp.gr.java_conf.kumagusu.control;

import android.content.DialogInterface;

/**
 * リスナ保持データ.
 *
 * @author tarshi
 *
 */
public final class ConfirmDialogListeners
{
    /**
     * OKまたはYesを処理するリスナ.
     */
    private DialogInterface.OnClickListener okOnClickListener;

    /**
     * Noを処理するリスナ.
     */
    private DialogInterface.OnClickListener noOnClickListener;

    /**
     * Cancelを処理するリスナ.
     */
    private DialogInterface.OnClickListener cancelOnClickListener;

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     * @param no Noを処理するリスナ
     * @param cancel Cancelを処理するリスナ
     */
    public ConfirmDialogListeners(DialogInterface.OnClickListener ok, DialogInterface.OnClickListener no,
            DialogInterface.OnClickListener cancel)
    {
        this.okOnClickListener = ok;
        this.noOnClickListener = no;
        this.cancelOnClickListener = cancel;
    }

    /**
     * OKまたはYesを処理するリスナを返す.
     *
     * @return OKまたはYesを処理するリスナ
     */
    public DialogInterface.OnClickListener getOkOnClickListener()
    {
        return okOnClickListener;
    }

    /**
     * Noを処理するリスナを返す.
     *
     * @return Noを処理するリスナ
     */
    public DialogInterface.OnClickListener getNoOnClickListener()
    {
        return noOnClickListener;
    }

    /**
     * Cancelを処理するリスナを返す.
     *
     * @return Cancelを処理するリスナ
     */
    public DialogInterface.OnClickListener getCancelOnClickListener()
    {
        return cancelOnClickListener;
    }
}
