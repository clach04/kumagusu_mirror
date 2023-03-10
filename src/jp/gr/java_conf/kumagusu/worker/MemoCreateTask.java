package jp.gr.java_conf.kumagusu.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import jp.gr.java_conf.kumagusu.Kumagusu.MemoListViewMode;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import android.app.Activity;
import android.widget.ListView;

/**
 * フォルダ内メモ生成処理.
 *
 * @author tarshi
 *
 */
public final class MemoCreateTask extends AbstractMemoCreateTask
{
    /**
     * Fileキュー.
     */
    private LinkedList<File> fileQueue;

    /**
     * フォルダ内メモ生成処理を初期化する.
     *
     * @param act アクティビティ
     * @param viewMode メモリスト表示モード
     * @param fQueue Fileキュー
     * @param mBuilder Memoビルダ
     * @param lView ListView
     * @param mList メモリスト
     * @param comparator メモリストのソート処理
     * @param taskStateListener メモ作成処理の状態変更を受け取るのリスナ
     */
    public MemoCreateTask(Activity act, MemoListViewMode viewMode, LinkedList<File> fQueue, MemoBuilder mBuilder,
            ListView lView, List<IMemo> mList, Comparator<IMemo> comparator,
            OnTaskStateListener taskStateListener)
    {
        super(act, viewMode, mBuilder, lView, mList, comparator, taskStateListener);

        this.fileQueue = fQueue;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Boolean doInBackground(Void... params)
    {
        try
        {
            List<IMemo> iMemoList = new ArrayList<IMemo>();

            while (this.fileQueue.size() > 0)
            {
                File f = this.fileQueue.peek();

                if (f == null)
                {
                    break;
                }

                // キャンセルなら終了
                if (isCancelled())
                {
                    return false;
                }

                if (!decryptMemoFile(f, iMemoList, null))
                {
                    continue;
                }

                // キューの最古Fileを削除
                this.fileQueue.remove();
            }

            // キャンセルなら終了
            if (isCancelled())
            {
                return false;
            }

            // UIスレッドにMemoをPOST
            publishProgress(iMemoList);

            return true;
        }
        finally
        {
            // スレッド終了を通知
            setBackgroundEnd();
        }
    }
}
