package jp.gr.java_conf.kumagusu.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.kumagusu.control.fragment.ProgressDialogFragment;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * メモ種別（パスワード）統一処理.
 *
 * @author tarshi
 *
 */
public final class UnificationTypeMemoTask extends AbstractMemoCreateTask
{
    /**
     * 検索フォルダ.
     */
    private String baseFolder;

    /**
     * プログレスダイアログ.
     */
    private ProgressDialogFragment progressDialog;

    /**
     * メモ種別（パスワード）統一処理を初期化する.
     *
     * @param act アクティビティー
     * @param bFolder 検索フォルダ
     * @param mBuilder Memoビルダ
     * @param listener メモ発見時の処理
     * @param progress プログレスダイアログ
     */
    public UnificationTypeMemoTask(Activity act, String bFolder, MemoBuilder mBuilder, ProgressDialogFragment progress,
            OnFindMemoFileListener listener)
    {
        super(act, mBuilder, listener);

        this.baseFolder = bFolder;
        this.progressDialog = progress;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        Log.d("UnificationTypeMemoTask", "*** START doInBackground()");

        // メモファイルの検索処理
        findMemoFile(new File(this.baseFolder));

        return true;
    }

    @Override
    protected void onPreExecute()
    {
        Log.d("UnificationTypeMemoTask", "*** START onPreExecute()");

        // プログレスダイアログ表示
        this.progressDialog.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "");

        // 親クラスの処理実行
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        Log.d("UnificationTypeMemoTask", "*** START onPostExecute()");

        // プログレスダイアログ消去
        this.progressDialog.dismiss();

        // 親クラスの処理実行
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled()
    {
        Log.d("UnificationTypeMemoTask", "*** START onCancelled()");

        // プログレスダイアログ消去
        this.progressDialog.dismiss();

        // 親クラスの処理実行
        super.onCancelled();
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
