package jp.gr.java_conf.kumagusu.control;

import jp.gr.java_conf.kumagusu.commons.Utilities;
import android.app.Activity;
import android.content.Context;

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
    protected final static void preShowDialog(Context con)
    {
        if (!(con instanceof Activity))
        {
            return;
        }

        Activity act = (Activity) con;

        Utilities.fixOrientation(act, true);
    }

    /**
     * ダイアログクローズ後の共通処理を実行する.
     *
     * @param con コンテキスト
     */
    protected final static void postDismissDialog(Context con)
    {
        if (!(con instanceof Activity))
        {
            return;
        }

        Activity act = (Activity) con;

        Utilities.fixOrientation(act, false);
    }
}
