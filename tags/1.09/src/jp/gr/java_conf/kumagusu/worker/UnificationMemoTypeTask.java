package jp.gr.java_conf.kumagusu.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import android.app.Activity;
import android.util.Log;

/**
 * メモ種別（パスワード）統一で、パスワード未入力のファイルを探す処理.
 *
 * @author tarshi
 */
public final class UnificationMemoTypeTask extends AbstractMemoCreateTask
{
    /**
     * 検索フォルダ.
     */
    private String baseFolder;

    /**
     * コンストラクタ.
     *
     * @param act アクティビティー
     * @param bFolder 検索フォルダ
     * @param mBuilder Memoビルダ
     * @param findMemoFileListener メモ発見時の処理
     * @param taskStateListener メモ作成処理の状態変更を受け取るのリスナ
     */
    public UnificationMemoTypeTask(Activity act, String bFolder, MemoBuilder mBuilder,
            OnFindMemoFileListener findMemoFileListener, OnTaskStateListener taskStateListener)
    {
        super(act, mBuilder, findMemoFileListener, taskStateListener);

        this.baseFolder = bFolder;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        try
        {
            Log.d("UnificationMemoTypeTask", "*** START doInBackground()");

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
     * メモファイルを検索する.
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
                    // 下位フォルダを検索する場合、そこまでに見つけたメモを出力
                    publishProgress(iMemoList);
                    iMemoList = new ArrayList<IMemo>();

                    // 下位フォルダ検索
                    findMemoFile(file.getAbsoluteFile());
                }
                else
                {
                    if (!decryptMemoFile(file, iMemoList, null))
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
