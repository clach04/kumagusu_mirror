package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import jp.gr.java_conf.kumagusu.Kumagusu.MemoListViewMode;
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
 *
 * @author tarshi
 *
 */
public abstract class AbstractMemoCreateTask extends AsyncTask<Void, List<IMemo>, Boolean>
{
    /**
     * アクティビティ.
     */
    private Activity activity;

    /**
     * アクティビティを返す.
     *
     * @return アクティビティ
     */
    protected final Activity getActivity()
    {
        return activity;
    }

    /**
     * メモリスト表示モード.
     */
    private MemoListViewMode memoListViewMode;

    /**
     * 表示先のListView.
     */
    private ListView targetListView;

    /**
     * メモリスト.
     */
    private List<IMemo> memoList;

    /**
     * メモリストのアダプタ.
     */
    private MemoListAdapter memoListAdapter = null;

    /**
     * メモビルダー.
     */
    private MemoBuilder memoBuilder;

    /**
     * 同期用オブジェクト.
     */
    private Object syncObject = new Object();

    /**
     * 同期用オブジェクトを返す.
     *
     * @return 同期用オブジェクト
     */
    protected final Object getSyncObject()
    {
        return syncObject;
    }

    /**
     *
     * @param act アクティビティ
     * @param viewMode メモリスト表示モード
     * @param mBuilder Memoビルダ
     * @param lView ListView
     * @param mList メモリスト
     */
    public AbstractMemoCreateTask(Activity act, MemoListViewMode viewMode, MemoBuilder mBuilder, ListView lView,
            List<IMemo> mList)
    {
        this.activity = act;
        this.memoListViewMode = viewMode;
        this.memoBuilder = mBuilder;
        this.targetListView = lView;
        this.memoList = mList;
    }

    @Override
    protected final void onProgressUpdate(List<IMemo>... values)
    {
        // キャンセルなら終了
        if (isCancelled())
        {
            return;
        }

        if (values == null)
        {
            final InputDialog dialog = new InputDialog();
            dialog.showDialog(AbstractMemoCreateTask.this.activity, AbstractMemoCreateTask.this.activity.getResources()
                    .getString(R.string.ui_td_input_password), InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // OK処理
                    String tryPassword = dialog.getText();
                    if (!MainApplication.getInstance(AbstractMemoCreateTask.this.activity).getPasswordList()
                            .contains(tryPassword))
                    {
                        MainApplication.getInstance(AbstractMemoCreateTask.this.activity).getPasswordList()
                                .add(tryPassword);
                    }

                    // ワーカスレッド再開
                    synchronized (AbstractMemoCreateTask.this.syncObject)
                    {
                        AbstractMemoCreateTask.this.syncObject.notifyAll();
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
            this.memoList.addAll(values[0]);

            // ListViewにメモリストを設定
            if (this.memoListAdapter == null)
            {
                this.memoListAdapter = new MemoListAdapter(this.activity, this.memoList);
                this.targetListView.setAdapter(memoListAdapter);
            }

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
     * メモファイルをデコードする. 必要であればパスワードを入力する.
     *
     * @param f メモファイル
     * @param iMemoList メモリスト
     * @param searchWords 検索ワード
     * @return itemがメモの時true
     */
    protected final boolean decryptMemoFile(File f, List<IMemo> iMemoList, String searchWords)
    {
        IMemo item;

        try
        {
            item = this.memoBuilder.buildFromFile(f.getAbsolutePath());
        }
        catch (Exception ex)
        {
            item = null;
        }

        if ((item != null) && (item.getMemoType() != MemoType.None))
        {
            if (item instanceof MemoFile)
            {
                MemoFile memoItem = (MemoFile) item;

                // 暗号化ファイルが解読出来ていない場合、新しいパスワードを入力
                if (!memoItem.isDecryptFg())
                {
                    // パスワード入力ダイアログ表示をUIスレッドに指示
                    publishProgress((List<IMemo>[]) null);

                    // 待機
                    try
                    {
                        synchronized (getSyncObject())
                        {
                            getSyncObject().wait();
                        }
                    }
                    catch (InterruptedException ex)
                    {
                    }

                    return false;
                }
                else
                {
                    // 最後の正しいパスワードを保存
                    if (MainApplication.getInstance(getActivity()).getPasswordList().size() > 0)
                    {
                        MainApplication.getInstance(getActivity()).setLastCorrectPassword(
                                MainApplication.getInstance(getActivity()).getPasswordList()
                                        .get(MainApplication.getInstance(getActivity()).getPasswordList().size() - 1));
                    }

                    if ((searchWords != null) && (!memoItem.getText().contains(searchWords)))
                    {
                        return true;
                    }
                }
            }

            // Memo追加
            iMemoList.add(item);
        }

        return true;
    }

    /**
     * リストの表示位置を復元する.
     */
    private void loadListViewStatus()
    {
        MainApplication.MemoListViewStatus listViewStatus = null;

        switch (this.memoListViewMode)
        {
        case FOLDER_VIEW:
            listViewStatus = MainApplication.getInstance(this.activity).popMemoListViewStatus(
                    MainApplication.getInstance(this.activity).getCurrentMemoFolder());
            break;

        case SEARCH_VIEW:
            listViewStatus = MainApplication.getInstance(this.activity).getMemoListStatus4Search();
            break;

        default:
            break;
        }

        if (listViewStatus != null)
        {
            this.targetListView.setSelectionFromTop(listViewStatus.getLastTopPosition(),
                    listViewStatus.getLastTopPositionY());
        }
    }

    /**
     * タイトルバーにタイトルを設定する.
     *
     * @param titleText タイトル文字列
     * @param postTitleText 付加タイトル文字列
     */
    protected final void setMainTitleText(String titleText, String postTitleText)
    {
        StringBuilder titleBuilder = new StringBuilder();

        // タイトルの指定がなければカレントフォルダを表示
        if (titleText == null)
        {
            File currentFolderFile = new File(MainApplication.getInstance(getActivity()).getCurrentMemoFolder());
            File rootFolderFile = new File(MainPreferenceActivity.getMemoLocation(getActivity()));
            String memoCurrentPath = currentFolderFile.getAbsolutePath().substring(
                    rootFolderFile.getAbsolutePath().length());

            if (memoCurrentPath.length() > 0)
            {
                titleBuilder.append(currentFolderFile.getName());
            }
            titleBuilder.append("/");
        }

        // 付加タイトル文字列があれば付加
        if (postTitleText != null)
        {
            titleBuilder.append(postTitleText);
        }

        getActivity().setTitle(titleBuilder.toString());
    }
}
