package jp.gr.java_conf.kumagusu.worker;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import jp.gr.java_conf.kumagusu.Kumagusu.MemoListViewMode;
import jp.gr.java_conf.kumagusu.MainApplication;
import jp.gr.java_conf.kumagusu.MemoListAdapter;
import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;
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
     * メモリストのソート処理.
     */
    private Comparator<IMemo> memoListComparator;

    /**
     *
     * @param act アクティビティ
     * @param viewMode メモリスト表示モード
     * @param mBuilder Memoビルダ
     * @param lView ListView
     * @param mList メモリスト
     * @param comparator メモリストのソート処理
     */
    public AbstractMemoCreateTask(Activity act, MemoListViewMode viewMode, MemoBuilder mBuilder, ListView lView,
            List<IMemo> mList, Comparator<IMemo> comparator)
    {
        this.activity = act;
        this.memoListViewMode = viewMode;
        this.memoBuilder = mBuilder;
        this.targetListView = lView;
        this.memoList = mList;
        this.memoListComparator = comparator;
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
            // ListViewにメモリストを設定
            if (this.memoListAdapter == null)
            {
                this.memoList.addAll(values[0]);
                this.memoListAdapter = new MemoListAdapter(this.activity, this.memoList);
                this.targetListView.setAdapter(memoListAdapter);
            }
            else
            {
                for (IMemo memo : values[0])
                {
                    this.memoListAdapter.add(memo);
                }
            }

            // ソート
            sort();

            // 表示位置を復元
            loadListViewStatus();
        }
    }

    /**
     * メモファイルをデコードする. 必要であればパスワードを入力する.
     *
     * @param f メモファイル
     * @param iMemoList メモリスト
     * @param searchLowerCaseWords 検索ワード(常に小文字で入力すること）
     * @return ファイルを処理したときtrue、パスワード入力などで処理していないときfalse
     */
    protected final boolean decryptMemoFile(File f, List<IMemo> iMemoList, String searchLowerCaseWords)
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
                        return false;
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

                    if ((searchLowerCaseWords != null)
                            && (!memoItem.getText().toLowerCase().contains(searchLowerCaseWords)))
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
     * メモリストをソートする.
     */
    public final void sort()
    {
        if (this.memoListComparator != null)
        {
            this.memoListAdapter.sort(this.memoListComparator);
        }
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
            else
            {
                titleBuilder.append("/");
            }
        }

        // 付加タイトル文字列があれば付加
        if (postTitleText != null)
        {
            titleBuilder.append(" ");
            titleBuilder.append(postTitleText);
        }

        getActivity().setTitle(titleBuilder.toString());
    }
}
