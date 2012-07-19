package som.kumagusu;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * タイマー.
 *
 * @author tarshi
 */
public final class Timer
{
    /**
     * コンテキスト.
     */
    private Context context;

    /**
     * 開始時間.
     */
    private long startDateTime;

    /**
     * タイマーを初期化する.
     *
     * @param con コンテキスト
     */
    public Timer(Context con)
    {
        this.context = con;
    }

    /**
     * タイマーを開始する.
     */
    public void start()
    {
        Log.d("AutoCloseTimer", "*** START start()");

        long delayTime = MainPreferenceActivity.getAutocloseDelaytime(this.context);

        this.startDateTime = 0;

        if (delayTime >= 0)
        {
            this.startDateTime = new Date().getTime();
        }
    }

    /**
     * タイマーを停止する.
     *
     * @return trueのときタイムアウト発生
     */
    public boolean stop()
    {
        Log.d("AutoCloseTimer", "*** START stop()");

        long delayTime = MainPreferenceActivity.getAutocloseDelaytime(this.context);

        boolean ret = false;

        if ((delayTime >= 0) && (this.startDateTime != 0))
        {
            if ((new Date().getTime() - this.startDateTime) >= delayTime)
            {
                // タイムアウト！
                ret = true;
            }
        }

        this.startDateTime = 0;

        return ret;
    }

    /**
     * 自動クローズ処理.
     *
     * @author tarshi
     */
    class AutoClosure implements Runnable
    {
        @Override
        public void run()
        {
            // 一応タイマを破棄
            stop();

            // パスワードをクリア
            CommonData.getInstance((Activity) context).clearPasswordList();

            // アプリケーションを終了する
            Activity act = (Activity) context;
            act.moveTaskToBack(true);
        }
    }
}
