package jp.gr.java_conf.kumagusu.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jp.gr.java_conf.kumagusu.Kumagusu.MemoListViewMode;
import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import android.app.Activity;
import android.util.Log;
import android.widget.ListView;

/**
 * 検索メモリスト作成処理.
 *
 * @author tarshi
 *
 */
public final class MemoChangePasswordTask extends AbstractMemoCreateTask
{
    /**
     * 検索フォルダ.
     */
    private String baseFolder;

    /**
     * 検索メモリスト作成処理を初期化する.
     *
     * @param act アクティビティー
     * @param bFolder 検索フォルダ
     * @param mBuilder Memoビルダ
     * @param listener メモ発見時の処理
     */
    public MemoChangePasswordTask(Activity act, String bFolder, MemoBuilder mBuilder, OnFindMemoFileListener listener)
    {
        super(act, mBuilder, listener);

        this.baseFolder = bFolder;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        // メモファイルの検索処理
        findMemoFile(new File(this.baseFolder));

        return true;
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
