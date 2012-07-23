package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.InputType;
import android.widget.ListView;

/**
 * メモ生成処理.
 *
 * @author somiya
 *
 */
public final class MemoCreator extends AsyncTask<Void, Boolean, Boolean>
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
     * 同期用オブジェクト.
     */
    private Object syncObject;

    /**
     * Memo作成処理を初期化する.
     *
     * @param act アクティビティ
     * @param fQueue Fileキュー
     * @param mBuilder Memoビルダ
     * @param lView ListView
     * @param mList メモリスト
     */
    public MemoCreator(Activity act, LinkedList<File> fQueue, MemoBuilder mBuilder, ListView lView, List<IMemo> mList)
    {
        this.activity = act;
        this.fileQueue = fQueue;
        this.memoBuilder = mBuilder;
        this.targetListView = lView;

        this.memoList = mList;
        this.syncObject = new Object();
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
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

            IMemo item;

            try
            {
                item = this.memoBuilder.buildFromFile(f.getAbsolutePath());
            }
            catch (Exception ex)
            {
                continue;
            }

            if (item.getMemoType() == MemoType.None)
            {
                continue;
            }

            if (item instanceof MemoFile)
            {
                MemoFile memoItem = (MemoFile) item;

                // 暗号化ファイルが解読出来ていない場合、新しいパスワードを入力
                if (!memoItem.isDecryptFg())
                {
                    // パスワード入力ダイアログ表示をUIスレッドに指示
                    publishProgress(false);

                    // 待機
                    try
                    {
                        synchronized (this.syncObject)
                        {
                            this.syncObject.wait();
                        }
                    }
                    catch (InterruptedException ex)
                    {
                    }

                    continue;
                }
                else
                {
                    // 最後の正しいパスワードを保存
                    if (MainApplication.getInstance(this.activity).getPasswordList().size() > 0)
                    {
                        MainApplication.getInstance(this.activity).setLastCorrectPassword(
                                MainApplication.getInstance(this.activity).getPasswordList()
                                        .get(MainApplication.getInstance(this.activity).getPasswordList().size() - 1));
                    }
                }
            }

            // キューの最古Fileを削除
            this.fileQueue.remove();

            this.memoList.add(item);
        }

        // キャンセルなら終了
        if (isCancelled())
        {
            return false;
        }

        // UIスレッドにMemoをPOST
        publishProgress(true);

        return true;
    }

    @Override
    protected void onProgressUpdate(Boolean... values)
    {
        if (values.length == 0)
        {
            return;
        }

        // キャンセルなら終了
        if (isCancelled())
        {
            return;
        }

        if (!values[0])
        {
            final InputDialog dialog = new InputDialog();
            dialog.showDialog(MemoCreator.this.activity,
                    MemoCreator.this.activity.getResources().getString(R.string.ui_td_input_password),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // OK処理
                            String tryPassword = dialog.getText();
                            if (!MainApplication.getInstance(MemoCreator.this.activity).getPasswordList()
                                    .contains(tryPassword))
                            {
                                MainApplication.getInstance(MemoCreator.this.activity).getPasswordList()
                                        .add(tryPassword);
                            }

                            // ワーカスレッド再開
                            synchronized (MemoCreator.this.syncObject)
                            {
                                MemoCreator.this.syncObject.notifyAll();
                            }
                        }
                    }, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // キャンセル処理
                            cancel(true);
                        }
                    });
            return;
        }
        else
        {
            // ListViewにメモリストを設定
            MemoListAdapter memoListAdapter = new MemoListAdapter(this.activity, this.memoList);
            this.targetListView.setAdapter(memoListAdapter);

            memoListAdapter.sort(new Comparator<IMemo>()
            {
                /**
                 * ソート項目を比較する.
                 *
                 * @param src 比較ベース
                 * @param target 比較対象
                 * @return 比較結果
                 */
                public int compare(IMemo src, IMemo target)
                {
                    int diff;

                    if (src.getMemoType() == MemoType.ParentFolder)
                    {
                        diff = -1;
                    }
                    else if (target.getMemoType() == MemoType.ParentFolder)
                    {
                        diff = 1;
                    }
                    else

                    if ((src.getMemoType() == MemoType.Folder) && (target.getMemoType() != MemoType.Folder))
                    {
                        diff = -1;
                    }
                    else if ((src.getMemoType() != MemoType.Folder) && (target.getMemoType() == MemoType.Folder))
                    {
                        diff = 1;
                    }
                    else
                    {
                        diff = src.getTitle().compareToIgnoreCase(target.getTitle());
                    }

                    return diff;
                }
            });

            // 表示位置を復元
            loadListViewStatus();
        }
    }

    /**
     * リストの表示位置を復元する.
     */
    private void loadListViewStatus()
    {
        MainApplication.MemoListViewStatus listViewStatus = MainApplication.getInstance(this.activity)
                .popMemoListViewStatus(MainApplication.getInstance(this.activity).getCurrentMemoFolder());

        if (listViewStatus != null)
        {
            this.targetListView.setSelectionFromTop(listViewStatus.getLastTopPosition(),
                    listViewStatus.getLastTopPositionY());
        }
    }
}
