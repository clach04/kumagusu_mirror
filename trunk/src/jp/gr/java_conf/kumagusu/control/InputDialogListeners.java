package jp.gr.java_conf.kumagusu.control;

import android.view.View;

/**
 * 入力ダイアログリスナ保持データ.
 *
 * @author tarshi
 *
 */
public final class InputDialogListeners
{
    /**
     * OKを処理するリスナ.
     */
    private InputDialogFragment.OnClickInputDialogListener okOnClickListener;

    /**
     * Cancelを処理するリスナ.
     */
    private InputDialogFragment.OnClickInputDialogListener cancelOnClickListener;

    /**
     * ユーザーボタンのクリックイベントリスナ.
     */
    private View.OnClickListener userButtonClickListener;

    /**
     * ユーザボタンのテキスト.
     */
    private String userButtonText;

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     */
    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok)
    {
        this(ok, null, null, null);
    }

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     * @param userListener ユーザボタンのクリックイベントリスナ
     * @param userText ユーザボタンのテキスト
     */
    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok, View.OnClickListener userListener,
            String userText)
    {
        this(ok, null, userListener, userText);
    }

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     * @param cancel Cancelを処理するリスナ
     */
    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok,
            InputDialogFragment.OnClickInputDialogListener cancel)
    {
        this(ok, cancel, null, null);
    }

    /**
     * リスナ保持データを初期化する.
     *
     * @param ok OKまたはYesを処理するリスナ
     * @param cancel Cancelを処理するリスナ
     * @param userListener ユーザボタンのクリックイベントリスナ
     * @param userText ユーザボタンのテキスト
     */
    public InputDialogListeners(InputDialogFragment.OnClickInputDialogListener ok,
            InputDialogFragment.OnClickInputDialogListener cancel, View.OnClickListener userListener, String userText)
    {
        this.okOnClickListener = ok;
        this.cancelOnClickListener = cancel;
        this.userButtonClickListener = userListener;
        this.userButtonText = userText;
    }

    /**
     * OKを処理するリスナを返す.
     *
     * @return OKまたはYesを処理するリスナ
     */
    public InputDialogFragment.OnClickInputDialogListener getOkOnClickListener()
    {
        return okOnClickListener;
    }

    /**
     * Cancelを処理するリスナを返す.
     *
     * @return Cancelを処理するリスナ
     */
    public InputDialogFragment.OnClickInputDialogListener getCancelOnClickListener()
    {
        return cancelOnClickListener;
    }

    /**
     * ユーザーボタンのクリックイベントリスナを返す.
     *
     * @return ユーザーボタンのクリックイベントリスナ
     */
    public View.OnClickListener getUserButtonClickListener()
    {
        return userButtonClickListener;
    }

    /**
     * ユーザーボタンのテキストを返す.
     *
     * @return ユーザーボタンのテキスト
     */
    public String getUserButtonText()
    {
        return userButtonText;
    }
}
