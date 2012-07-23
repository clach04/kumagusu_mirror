package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.util.ArrayList;
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
public final class MemoFileSearchTask extends AbstractMemoCreateTask
{
    /**
     * 検索フォルダ.
     */
    private String baseFolder;

    /**
     * 検索ワード.
     */
    private String searchWords;

    /**
     * 検索メモリスト作成処理を初期化する.
     *
     * @param act アクティビティー
     * @param viewMode メモリスト表示モード
     * @param bFolder 検索フォルダ
     * @param mBuilder Memoビルダ
     * @param lView ListView
     * @param mList メモリスト
     * @param sWords 検索ワード
     */
    public MemoFileSearchTask(Activity act, MemoListViewMode viewMode, String bFolder, MemoBuilder mBuilder,
            ListView lView, List<IMemo> mList, String sWords)
    {
        super(act, viewMode, mBuilder, lView, mList);

        this.baseFolder = bFolder;
        this.searchWords = sWords;
    }

    @Override
    protected void onPreExecute()
    {
        setMainTitleText(null, getActivity().getResources().getString(R.string.search_memo_list_post_title_start));
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        // メモファイルの検索処理
        findMemoFile(new File(this.baseFolder), (List<IMemo>) new ArrayList<IMemo>());

        return true;
    }

    /**
     * 指定検索ワードを含むメモファイルを検索する.
     *
     * @param targetFolderFile 検索フォルダ
     * @param iMemoList 検索結果保存先リスト
     */
    @SuppressWarnings("unchecked")
    private void findMemoFile(File targetFolderFile, List<IMemo> iMemoList)
    {
        // フォルダが存在しない場合終了
        if ((!targetFolderFile.exists()) || (!targetFolderFile.isDirectory()))
        {
            return;
        }

        File[] files = targetFolderFile.listFiles();

        for (File file : files)
        {
            // キャンセルなら終了
            if (isCancelled())
            {
                return;
            }

            if (file.isDirectory())
            {
                findMemoFile(file.getAbsoluteFile(), iMemoList);
            }
            else
            {
                decryptMemoFile(file, iMemoList, this.searchWords);
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

    @Override
    protected void onPostExecute(Boolean result)
    {
        setMainTitleText(null, getActivity().getResources().getString(R.string.search_memo_list_post_title_end));
    }
}
