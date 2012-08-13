package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.MainApplication;
import jp.gr.java_conf.kumagusu.commons.Utilities;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * 縦横切り替え表示に対応していないダイアログの共通処理.
 *
 * @author tarshi
 *
 */
public abstract class OldStyleDialog
{
    /**
     * ダイアログオープン前の共通処理を実行する.
     *
     * @param con コンテキスト
     */
    protected static final void preShowDialog(Context con)
    {
        Log.d("OldStyleDialog", "*** START preShowDialog()");

        if (con instanceof Activity)
        {
            Activity act = (Activity) con;

            // ダイアログ表示数をインクリメント
            MainApplication.getInstance(act).incrementOldStyleDialogCounter();

            // 縦横切り替え禁止
            Utilities.fixOrientation(act, true);
        }
    }

    /**
     * ダイアログクローズ後の共通処理を実行する.
     *
     * @param con コンテキスト
     */
    protected static final void postDismissDialog(Context con)
    {
        Log.d("OldStyleDialog", "*** START postDismissDialog()");

        if (con instanceof Activity)
        {
            Activity act = (Activity) con;

            // ダイアログ表示数をデクリメント
            MainApplication.getInstance(act).decrementOldStyleDialogCounter();

            // 縦横切り替えをシステムデフォルトに
            if (!MainApplication.getInstance(act).isPlusOldStyleDialogCounter())
            {
                Log.d("OldStyleDialog", "Orientation is default");

                Utilities.fixOrientation(act, false);
            }
        }
    }
}
