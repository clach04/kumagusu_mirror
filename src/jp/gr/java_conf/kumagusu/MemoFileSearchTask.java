package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import jp.gr.java_conf.kumagusu.Kumagusu.MemoListViewMode;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;

/**
 * メモファイルを検索し、メモリストを作成する.
 *
 * @author tarshi
 *
 */
public final class MemoFileSearchTask extends AsyncTask<Void, MemoFile, Boolean>
{
    /**
     * アクティビティ.
     */
    private Activity activity;

    /**
     * 表示先のListView.
     */
    private ListView targetListView;

    /**
     * Fileキュー.
     */
    private LinkedList<File> fileQueue;

    /**
     * メモビルダー.
     */
    private MemoBuilder memoBuilder;

    /**
     * メモリスト.
     */
    private List<IMemo> memoList;

    /**
     * メモリスト表示モード.
     */
    private MemoListViewMode memoListViewMode;

    private String searchWords;

    /**
     *
     * @param act
     * @param viewMode
     * @param fQueue
     * @param mBuilder
     * @param lView
     * @param mList
     */
    public MemoFileSearchTask(Activity act, MemoListViewMode viewMode, LinkedList<File> fQueue, MemoBuilder mBuilder,
            ListView lView, List<IMemo> mList, String sWords)
    {
        this.activity = act;
        this.memoListViewMode = viewMode;
        this.fileQueue = fQueue;
        this.memoBuilder = mBuilder;
        this.targetListView = lView;
        this.memoList = mList;
        this.searchWords = sWords;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        // メモファイルの検索処理

        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    protected void onProgressUpdate(MemoFile... values)
    {
    }

    private void findMemoFile(File targetFolderFile)
    {
        // フォルダが存在しない場合終了
        if ((!targetFolderFile.exists()) || (!targetFolderFile.isDirectory()))
        {
            return;
        }

        File[] files = targetFolderFile.listFiles();

        for (File file : files)
        {
            if (file.isDirectory())
            {
                findMemoFile(file.getAbsoluteFile());
            }
            else
            {
                MemoFile memoFile;

                try
                {
                    memoFile = (MemoFile) this.memoBuilder.buildFromFile(file.getAbsolutePath());
                }
                catch (FileNotFoundException e)
                {
                    continue;
                }

                if (memoFile.getMemoType() == MemoType.None)
                {
                    continue;
                }

                if (memoFile.getText().contains(this.searchWords))
                {
                    publishProgress(memoFile);
                }
            }
        }

        return;
    }
}
