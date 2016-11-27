package jp.gr.java_conf.kumagusu.commons;

import java.util.Date;

import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;
import android.content.Context;
import android.os.Bundle;
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
     * タイムアウト発生？.
     */
    private boolean timeout;

    /**
     * タイマーを初期化する.
     *
     * @param con コンテキスト
     */
    public Timer(Context con)
    {
        this.context = con;

        this.startDateTime = 0;
        this.timeout = false;
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

        this.timeout = false;
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

                this.timeout = true;
            }
        }

        this.startDateTime = 0;

        return ret;
    }

    /**
     * タイムアウト発生？を返す.
     *
     * @return タイムアウト発生のときtrue
     */
    public boolean isTimeout()
    {
        return this.timeout;
    }

    /**
     * タイムアウト発生？をリセットする.
     */
    public void resetTimeout()
    {
        this.timeout = false;
    }

    /**
     * タイマー開始時刻を保存する。
     * @param outState バンドル
     */
    public void SaveInstanceState(Bundle outState)
    {
        Log.d("Timer", "*** START SaveInstanceState()");

        outState.putLong("kumagusuTimerStartDateTime", this.startDateTime);
        outState.putBoolean("kumagusuTimerTimeout", this.timeout);
    }

    /**
     * タイマー開始時刻を再現する。
     * @param savedInstanceState バンドル
     */
    public void RestoreInstanceState(Bundle savedInstanceState)
    {
        Log.d("Timer", "*** START RestoreInstanceState()");

        if (savedInstanceState.containsKey("kumagusuTimerStartDateTime"))
        {
            this.startDateTime = savedInstanceState.getLong("kumagusuTimerStartDateTime");
        }

        if (savedInstanceState.containsKey("kumagusuTimerTimeout"))
        {
            this.timeout = savedInstanceState.getBoolean("kumagusuTimerTimeout");
        }
    }
}
