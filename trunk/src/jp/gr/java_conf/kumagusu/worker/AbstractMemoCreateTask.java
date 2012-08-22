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
import android.util.Log;
import android.widget.ListView;

/**
 * メモ生成共通処理.
 *
 * @author tarshi
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
     * 発見したメモファイルを受け取るリスナ.
     */
    private OnFindMemoFileListener onFindMemoFileListener = null;

    /**
     * メモ作成処理の状態変更を受け取るのリスナ.
     */
    private OnTaskStateListener onTaskStateListener = null;

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
     * パスワード入力ダイアログ.
     */
    private InputDialog inputPasswordDialog = null;

    /**
     * アクティビティのタイトル（タスク開始時）.
     */
    private String activityTitleStartTask = "";

    /**
     * アクティビティのタイトル（タスク終了時）.
     */
    private String activityTitleEndTask = "";

    /**
     * アクティビティのタイトル（タスク開始時）を設定する.
     *
     * @param activityTitleInit アクティビティのタイトル（タスク開始時）
     */
    public final void setActivityTitleStartTask(String activityTitleInit)
    {
        this.activityTitleStartTask = activityTitleInit;
    }

    /**
     * アクティビティのタイトル（Task終了時）を設定する.
     *
     * @param activityTitleStart アクティビティのタイトル（タスク終了時）
     */
    public final void setActivityTitleEndTask(String activityTitleStart)
    {
        this.activityTitleEndTask = activityTitleStart;
    }

    /**
     * メモ生成共通処理を初期化する.
     *
     * @param act アクティビティ
     * @param mBuilder Memoビルダ
     * @param listener 発見したメモファイルを受け取るリスナ
     * @param stateListener メモ作成処理の状態変更を受け取るのリスナ
     */
    public AbstractMemoCreateTask(Activity act, MemoBuilder mBuilder, OnFindMemoFileListener listener,
            OnTaskStateListener stateListener)
    {
        this(act, MemoListViewMode.NONE, mBuilder, null, null, null);

        this.onFindMemoFileListener = listener;
        this.onTaskStateListener = stateListener;
    }

    /**
     * メモ生成共通処理を初期化する.
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

        this.onFindMemoFileListener = null;
    }

    @Override
    protected final void onPreExecute()
    {
        Log.d("AbstractMemoCreateTask", "*** START onPreExecute()");

        // リスナ呼び出し
        if (this.onTaskStateListener != null)
        {
            this.onTaskStateListener.onChangeState(TaskState.PreExecute);
        }

        // タイトル設定
        setMainTitleText(activityTitleStartTask);
    }

    @Override
    protected final void onPostExecute(Boolean result)
    {
        Log.d("AbstractMemoCreateTask", "*** START onPostExecute()");

        // リスナ呼び出し
        if (this.onTaskStateListener != null)
        {
            this.onTaskStateListener.onChangeState(TaskState.PostExecute);
        }

        setMainTitleText(activityTitleEndTask);

        if (this.inputPasswordDialog != null)
        {
            this.inputPasswordDialog.dismissDialog();
            this.inputPasswordDialog = null;
        }
    }

    @Override
    protected final void onCancelled()
    {
        Log.d("AbstractMemoCreateTask", "*** START onCancelled()");

        // リスナ呼び出し
        if (this.onTaskStateListener != null)
        {
            this.onTaskStateListener.onChangeState(TaskState.Cancel);
        }

        setMainTitleText(activityTitleEndTask);

        if (this.inputPasswordDialog != null)
        {
            this.inputPasswordDialog.dismissDialog();
            this.inputPasswordDialog = null;
        }
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
            this.inputPasswordDialog = new InputDialog(AbstractMemoCreateTask.this.activity);
            this.inputPasswordDialog.showDialog(
                    AbstractMemoCreateTask.this.activity.getResources().getString(R.string.ui_td_input_password),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // OK処理
                            String tryPassword = AbstractMemoCreateTask.this.inputPasswordDialog.getText();
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

                            AbstractMemoCreateTask.this.inputPasswordDialog = null;
                        }
                    }, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // キャンセル処理
                            cancel(true);

                            AbstractMemoCreateTask.this.inputPasswordDialog = null;
                        }
                    });
            return;
        }
        else
        {
            // 発見メモの処理が設定されていなければデフォルト処理を使用
            if (this.onFindMemoFileListener == null)
            {
                this.onFindMemoFileListener = new OnFindMemoFileListener()
                {
                    @Override
                    public void onFind(List<IMemo> mList)
                    {
                        // ListViewにメモリストを設定
                        if (AbstractMemoCreateTask.this.targetListView != null)
                        {
                            if (AbstractMemoCreateTask.this.memoListAdapter == null)
                            {
                                AbstractMemoCreateTask.this.memoList.addAll(mList);
                                AbstractMemoCreateTask.this.memoListAdapter = new MemoListAdapter(
                                        AbstractMemoCreateTask.this.activity, AbstractMemoCreateTask.this.memoList);
                                AbstractMemoCreateTask.this.targetListView.setAdapter(memoListAdapter);
                            }
                            else
                            {
                                for (IMemo memo : mList)
                                {
                                    AbstractMemoCreateTask.this.memoListAdapter.add(memo);
                                }
                            }

                            // ソート
                            sort();

                            // 表示位置を復元
                            loadListViewStatus();
                        }
                    }
                };
            }

            // メモの発見時の処理
            this.onFindMemoFileListener.onFind(values[0]);
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
        if ((this.targetListView != null) && (this.memoListComparator != null))
        {
            this.memoListAdapter.sort(this.memoListComparator);
        }
    }

    /**
     * Taskをキャンセルする.
     *
     * @param mayInterruptIfRunning 実行中Taskを中断するならtrue
     */
    public final void cancelTask(boolean mayInterruptIfRunning)
    {
        Log.d("AbstractMemoCreateTask", "*** START cancelTask()");

        if (this.inputPasswordDialog != null)
        {
            this.inputPasswordDialog.dismissDialog();
            this.inputPasswordDialog = null;
        }
        super.cancel(mayInterruptIfRunning);
    }

    /**
     * リストの表示位置を復元する.
     */
    private void loadListViewStatus()
    {
        // リストViewが設定されている場合、リストの表示位置を復元
        // ※画面処理でなければリストViewが設定されない。
        if (this.targetListView != null)
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
    }

    /**
     * タイトルバーにタイトルを設定する.
     *
     * @param postTitleText 付加タイトル文字列
     */
    private void setMainTitleText(String postTitleText)
    {
        // リストViewが設定されている場合、アクティビティのタイトルを変更
        // ※画面処理でなければリストViewが設定されない。
        if (this.targetListView != null)
        {
            StringBuilder titleBuilder = new StringBuilder();

            // カレントフォルダをタイトルの最初に設定
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

            // 付加タイトル文字列があれば付加
            if (postTitleText != null)
            {
                titleBuilder.append(" ");
                titleBuilder.append(postTitleText);
            }

            getActivity().setTitle(titleBuilder.toString());
        }
    }

    /**
     * 発見したメモファイルを受け取るリスナ.
     *
     * @author tarshi
     *
     */
    public interface OnFindMemoFileListener
    {
        /**
         * メモを発見したとき呼び出される.
         *
         * @param mList メモファイル/メモフォルダ
         */
        void onFind(List<IMemo> mList);
    }

    /**
     * メモ作成処理の状態.
     *
     * @author tarshi
     *
     */
    public enum TaskState
    {
        /**
         * 実行前.
         */
        PreExecute,
        /**
         * 実行後.
         */
        PostExecute,
        /**
         * キャンセル.
         */
        Cancel,
    }

    /**
     * メモ作成処理の状態変更を受け取るのリスナ.
     *
     * @author tarshi
     *
     */
    public interface OnTaskStateListener
    {
        /**
         * 状態変更があったとき呼び出される.
         *
         * @param state メモ作成処理の状態
         */
        void onChangeState(TaskState state);
    }
}
