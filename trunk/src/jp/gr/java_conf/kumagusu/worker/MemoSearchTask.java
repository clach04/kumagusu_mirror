package jp.gr.java_conf.kumagusu.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jp.gr.java_conf.kumagusu.Kumagusu.MemoListViewMode;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import android.app.Activity;
import android.widget.ListView;

/**
 * 検索メモリスト作成処理.
 *
 * @author tarshi
 *
 */
public final class MemoSearchTask extends AbstractMemoCreateTask
{
    /**
     * 検索フォルダ.
     */
    private String baseFolder;

    /**
     * 検索ワード（小文字）.
     */
    private String searchLowerCaseWords;

    /**
     * 検索メモリスト作成処理を初期化する.
     *
     * @param act アクティビティー
     * @param viewMode メモリスト表示モード
     * @param bFolder 検索フォルダ
     * @param mBuilder Memoビルダ
     * @param lView ListView
     * @param mList メモリスト
     * @param comparator メモリストのソート処理
     * @param sWords 検索ワード
     * @param taskStateListener メモ作成処理の状態変更を受け取るのリスナ
     */
    public MemoSearchTask(Activity act, MemoListViewMode viewMode, String bFolder, MemoBuilder mBuilder,
            ListView lView, List<IMemo> mList, Comparator<IMemo> comparator, String sWords,
            OnTaskStateListener taskStateListener)
    {
        super(act, viewMode, mBuilder, lView, mList, comparator, taskStateListener);

        this.baseFolder = bFolder;
        this.searchLowerCaseWords = sWords.toLowerCase();
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        try
        {
            // メモファイルの検索処理
            findMemoFile(new File(this.baseFolder));

            return true;
        }
        finally
        {
            // スレッド終了を通知
            setBackgroundEnd();
        }
    }

    /**
     * 指定検索ワードを含むメモファイルを検索する.
     *
     * @param targetFolderFile 検索フォルダ
     */
    @SuppressWarnings("unchecked")
    private void findMemoFile(File targetFolderFile)
    {
        // フォルダが存在しない場合終了
        if ((!targetFolderFile.exists()) || (!targetFolderFile.isDirectory()))
        {
            return;
        }

        File[] files = targetFolderFile.listFiles();
        List<IMemo> iMemoList = new ArrayList<IMemo>();

        for (File file : files)
        {
            while (true)
            {
                // キャンセルなら終了
                if (isCancelled())
                {
                    return;
                }

                if (file.isDirectory())
                {
                    findMemoFile(file.getAbsoluteFile());
                }
                else
                {
                    if (!decryptMemoFile(file, iMemoList, this.searchLowerCaseWords))
                    {
                        continue;
                    }
                }

                break;
            }
        }

        // キャンセルなら終了
        if (isCancelled())
        {
            return;
        }

        publishProgress(iMemoList);

        return;
    }
}
