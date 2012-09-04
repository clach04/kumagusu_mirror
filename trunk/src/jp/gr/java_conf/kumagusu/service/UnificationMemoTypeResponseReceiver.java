package jp.gr.java_conf.kumagusu.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * メモ種別・パスワード統一サービスの動作状態受信処理.
 *
 * @author tarshi
 *
 */
public class UnificationMemoTypeResponseReceiver extends BroadcastReceiver
{
    /**
     * アクション名.
     */
    public static final String ACTION_RESPONSE = "jp.gr.java_conf.kumagusu.service.intent.action.POST_PROCESSED";

    /**
     * アクション状態「開始」.
     */
    public static final int ACTION_STATUS_START = 1;

    /**
     * アクション状態「更新」.
     */
    public static final int ACTION_STATUS_UPDATE = 2;

    /**
     * アクション状態「終了」.
     */
    public static final int ACTION_STATUS_FINISH = 3;

    /**
     * サービス状態のOverver.
     */
    private Observer mObserver;

    /**
     * コンストラクタ.
     *
     * @param observer サービス状態のObserver
     */
    public UnificationMemoTypeResponseReceiver(Observer observer)
    {
        mObserver = observer;
    }

    @Override
    public final void onReceive(Context context, Intent intent)
    {
        // パラメータ取得
        int status = intent.getIntExtra("status", 0);

        Log.d("UnificationMemoTypeResponseReceiver", "*** START onReceive()  status:" + status);

        switch (status)
        {
        case ACTION_STATUS_START: // 開始
            mObserver.onStart();
            break;

        case ACTION_STATUS_UPDATE: // 更新
            String memoFileName = intent.getStringExtra("memoFileName");
            mObserver.onUpdate(memoFileName);
            break;

        case ACTION_STATUS_FINISH: // 終了
            boolean finishResult = intent.getBooleanExtra("finishResult", false);
            mObserver.onFinish(finishResult);
            break;

        default:
            break;
        }
    }

    /**
     * サービス状態のObserverのインタフェース.
     *
     * @author tarshi
     *
     */
    public interface Observer
    {
        /**
         * サービス開始通知を受信する.
         */
        void onStart();

        /**
         * サービス更新通知を受信する.
         *
         * @param memoFileName 処理したメモファイル
         */
        void onUpdate(String memoFileName);

        /**
         * サービス終了通知を受信する.
         *
         * @param result サービス終了状態（true＝正常）
         */
        void onFinish(boolean result);
    }
}
