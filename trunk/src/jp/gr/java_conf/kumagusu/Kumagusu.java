package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import jp.gr.java_conf.kumagusu.commons.Utilities;
import jp.gr.java_conf.kumagusu.compat.ActivityCompat;
import jp.gr.java_conf.kumagusu.control.ConfirmDialogFragment;
import jp.gr.java_conf.kumagusu.control.ConfirmDialogListenerFolder;
import jp.gr.java_conf.kumagusu.control.DialogListeners;
import jp.gr.java_conf.kumagusu.control.DirectorySelectDialog;
import jp.gr.java_conf.kumagusu.control.DirectorySelectDialog.OnDirectoryListDialogListener;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.control.ListDialogFragment;
import jp.gr.java_conf.kumagusu.control.ListDialogListenerFolder;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoFolder;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;
import jp.gr.java_conf.kumagusu.worker.AbstractMemoCreateTask;
import jp.gr.java_conf.kumagusu.worker.MemoCreateTask;
import jp.gr.java_conf.kumagusu.worker.MemoSearchTask;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * メモ一覧. Root Activity
 */
public final class Kumagusu extends FragmentActivity implements ConfirmDialogListenerFolder, ListDialogListenerFolder
{
    /**
     * メニュー項目「新規」.
     */
    private static final int MENU_ID_CREATE_MEMO = Menu.FIRST;

    /**
     * メニュー項目「最新の情報に更新」.
     */
    private static final int MENU_ID_REFRESH = (Menu.FIRST + 1);

    /**
     * メニュー項目「検索」.
     */
    private static final int MENU_ID_SEARCH = (Menu.FIRST + 2);

    /**
     * メニュー項目「並び替え方法」.
     */
    private static final int MENU_ID_SORT_METHOD = (Menu.FIRST + 3);

    /**
     * メニュー項目「設定」.
     */
    private static final int MENU_ID_SETTING = (Menu.FIRST + 4);

    /**
     * メモを追加.
     */
    private static final int FILE_LIST_CONTROL_ID_ADD_MEMO = 0;

    /**
     * フォルダを追加.
     */
    private static final int FILE_LIST_CONTROL_ID_ADD_FOLDER = 1;

    /**
     * ファイルコントロールID「コピー」.
     */
    private static final int FILE_CONTROL_ID_COPY = 0;

    /**
     * ファイルコントロールID「移動」.
     */
    private static final int FILE_CONTROL_ID_MOVE = 1;

    /**
     * ファイルコントロールID「削除」.
     */
    private static final int FILE_CONTROL_ID_DELETE = 2;

    /**
     * ファイルコントロールID「暗号化・復号化」.
     */
    private static final int FILE_CONTROL_ID_ENCRYPT_OR_DECRYPT = 3;

    /**
     * ファイルコントロールID「暗号化(ﾊﾟｽﾜｰﾄﾞ入力)」.
     */
    private static final int FILE_CONTROL_ID_ENCRYPT_NEW_PASSWORD = 4;

    /**
     * フォルダコントロールID「コピー」.
     */
    private static final int FOLDER_CONTROL_ID_COPY = 0;

    /**
     * フォルダコントロールID「移動」.
     */
    private static final int FOLDER_CONTROL_ID_MOVE = 1;

    /**
     * フォルダコントロールID「削除」.
     */
    private static final int FOLDER_CONTROL_ID_DELETE = 2;

    /**
     * フォルダコントロールID「名称変更」.
     */
    private static final int FOLDER_CONTROL_ID_RENAME = 3;

    /**
     * リスト.
     */
    private ListView mListView;

    /**
     * 選択中のメモのパス.
     */
    private String selectedMemoFilePath;

    /**
     * カレントディレクトリのメモ.
     */
    private List<IMemo> mCurrentFolderMemoFileList = new ArrayList<IMemo>();

    /**
     * メモビルダ.
     */
    private MemoBuilder memoBuilder = null;

    /**
     * カレントディレクトリのファイル.
     */
    private LinkedList<File> mCurrentFolderFileQueue = new LinkedList<File>();

    /**
     * メモ作成ワーカースレッド.
     */
    private AbstractMemoCreateTask memoCreator = null;

    /**
     * メモ表示モード.
     */
    private MemoListViewMode memoListViewMode = MemoListViewMode.FOLDER_VIEW;

    /**
     * リスト表示モード値.
     *
     * @author tarshi
     *
     */
    public enum MemoListViewMode
    {
        /**
         * フォルダ表示.
         */
        FOLDER_VIEW,

        /**
         * 検索表示.
         */
        SEARCH_VIEW,
    }

    /**
     * 検索ワード.
     */
    private String searchWords = null;

    /**
     * メモリストのソート処理.
     */
    private Comparator<IMemo> memoListComparator = null;

    /**
     * 確認ダイアログID「ファイル削除」.
     */
    private static final int DIALOG_ID_CONFIRM_DELETE_FILE = 1;

    /**
     * 確認ダイアログID「フォルダ削除」.
     */
    private static final int DIALOG_ID_CONFIRM_DELETE_FOLDER = 2;

    /**
     * 確認ダイアログID「フォルダ削除エラー」.
     */
    private static final int DIALOG_ID_CONFIRM_DELETE_FOLDER_ERROR = 3;

    /**
     * 確認ダイアログID「フォルダ名変更エラー（フォルダ名指定なし）」.
     */
    private static final int DIALOG_ID_CONFIRM_RENAME_FOLDER_ERROR_NONAME = 4;

    /**
     * 確認ダイアログID「フォルダ名変更エラー（フォルダ名重複）」.
     */
    private static final int DIALOG_ID_CONFIRM_RENAME_FOLDER_ERROR_CONFLICT = 5;

    /**
     * 確認ダイアログID「フォルダ追加エラー（フォルダ名重複）」.
     */
    private static final int DIALOG_ID_CONFIRM_ADD_FOLDER_ERROR_CONFLICT = 6;

    /**
     * 確認ダイアログID「フォルダ追加エラー（フォルダ名指定なし）」.
     */
    private static final int DIALOG_ID_CONFIRM_ADD_FOLDER_ERROR_NONAME = 7;

    /**
     * リストダイアログID「メモ操作」.
     */
    private static final int DIALOG_ID_LIST_MEMO_FILE_CONTROL = 101;

    /**
     * リストダイアログID「フォルダ操作」.
     */
    private static final int DIALOG_ID_LIST_FOLDER_CONTROL = 102;

    /**
     * リストダイアログID「並び替え方法」.
     */
    private static final int DIALOG_ID_LIST_MEMO_SORT_METHOD = 103;

    /**
     * リストダイアログID「メモリスト操作」.
     */
    private static final int DIALOG_ID_LIST_MEMO_LIST_CONTROL = 104;

    /**
     * 確認ダイアログ保管データMap.
     */
    private SparseArray<DialogListeners> dialogListenerMap = new SparseArray<DialogListeners>();

    /**
     * onCreate（アクティビティの生成）状態の処理を実行する.
     *
     * @param savedInstanceState Activityの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Activity初期化
        ActivityCompat.initActivity(this, R.layout.main, R.drawable.icon, null, true, false);

        Log.d("Kumagusu", "*** START onCreate()");

        // ダイアログのリスナを生成
        initConfirmDialogListener();
        initListDialogListener();

        // リストのインスタンスを取得
        this.mListView = (ListView) findViewById(R.id.list);

        // リストにアイテムがない場合のメッセージを設定
        View listEmptyView = findViewById(R.id.list_empty_text);
        this.mListView.setEmptyView(listEmptyView);

        // リストのクリックイベントを登録
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // 選択行取得
                ListView listView = (ListView) parent;

                IMemo selectedItem = (IMemo) listView.getItemAtPosition(position);

                if (selectedItem != null)
                {
                    Intent intent = null;

                    if ((selectedItem.getMemoType() == MemoType.Text)
                            || (selectedItem.getMemoType() == MemoType.Secret1)
                            || (selectedItem.getMemoType() == MemoType.Secret2))
                    {
                        intent = new Intent(Kumagusu.this, EditorActivity.class);

                        // 受け渡すデータを設定
                        intent.putExtra("FULL_PATH", selectedItem.getPath());
                        intent.putExtra("CURRENT_FOLDER", selectedItem.getParent());

                        if (Kumagusu.this.memoListViewMode == MemoListViewMode.SEARCH_VIEW)
                        {
                            // 検索時はエディタでに検索文字を渡し、
                            // エディタに検索処理を開始させる
                            intent.putExtra("SEARCH_WORDS", Kumagusu.this.searchWords);
                        }
                    }
                    else if ((selectedItem.getMemoType() == MemoType.Folder)
                            || (selectedItem.getMemoType() == MemoType.ParentFolder))
                    {
                        intent = new Intent(Kumagusu.this, Kumagusu.class);
                        intent.putExtra("CURRENT_FOLDER", selectedItem.getPath());
                    }

                    // Activetyを呼び出す
                    if (intent != null)
                    {
                        startActivity(intent);
                    }
                }
            }
        });

        // リスト項目の長押しイベントを登録
        this.mListView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            /**
             * リスト項目の長押しイベントを処理する。
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                // 選択行取得
                ListView listView = (ListView) parent;

                IMemo selectedItem = (IMemo) listView.getItemAtPosition(position);

                if (selectedItem != null)
                {
                    Kumagusu.this.selectedMemoFilePath = selectedItem.getPath();

                    // ダイアログ表示
                    switch (selectedItem.getMemoType())
                    {
                    case Text:
                    case Secret1:
                    case Secret2:
                        String[] dialogEntries;
                        if (selectedItem.getMemoType() == MemoType.Text)
                        {
                            if (MainApplication.getInstance(Kumagusu.this).getLastCorrectPassword() == null)
                            {
                                dialogEntries = getResources().getStringArray(
                                        R.array.memo_file_control_dialog_entries_4_text);
                            }
                            else
                            {
                                dialogEntries = getResources().getStringArray(
                                        R.array.memo_file_control_dialog_entries_4_text_2);
                            }
                        }
                        else
                        {
                            dialogEntries = getResources().getStringArray(
                                    R.array.memo_file_control_dialog_entries_4_secret);
                        }

                        ListDialogFragment.newInstance(DIALOG_ID_LIST_MEMO_FILE_CONTROL, R.drawable.memo_operation,
                                R.string.memo_file_control_dialog_title, 0, dialogEntries).show(
                                getSupportFragmentManager(), "");

                        break;

                    case Folder:
                        ListDialogFragment.newInstance(DIALOG_ID_LIST_FOLDER_CONTROL, R.drawable.folder_operation,
                                R.string.folder_control_dialog_title, 0,
                                getResources().getStringArray(R.array.memo_file_control_dialog_entries_4_folder)).show(
                                getSupportFragmentManager(), "");

                        break;

                    default:
                        break;
                    }
                }

                return true;
            }
        });

        // スクロールイベント処理
        this.mListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                // スクロール終了時、スクロール位置を保存
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                {
                    // リストの状態を保存
                    saveListViewStatus();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.d("Kumagusu", "*** START onResume()");

        // タイムアウトの確認
        if (MainApplication.getInstance(this).getPasswordTimer().stop())
        {
            // パスワードをクリア
            MainApplication.getInstance(this).clearPasswordList();

            // リストをクリア
            clearMemoList();

            // 検索モードのときリスト終了
            if (this.memoListViewMode == MemoListViewMode.SEARCH_VIEW)
            {
                finish();

                return;
            }
        }

        // パラメータ取得
        Bundle bundle = getIntent().getExtras();

        MainApplication.getInstance(this).setCurrentMemoFolder(null);

        if (bundle != null)
        {
            if ((bundle.containsKey("VIEW_MODE")) && (bundle.getString("VIEW_MODE").equals("SEARCH")))
            {
                this.memoListViewMode = MemoListViewMode.SEARCH_VIEW;
                this.searchWords = bundle.getString("SEARCH_WORDS");
            }

            if ((bundle.containsKey("CURRENT_FOLDER"))
                    && (bundle.getString("CURRENT_FOLDER").startsWith(MainPreferenceActivity.getMemoLocation(this))))
            {
                MainApplication.getInstance(this).setCurrentMemoFolder(bundle.getString("CURRENT_FOLDER"));
            }
        }

        // メモリストのソート処理を生成
        this.memoListComparator = new MemoListComparator(this, this.memoListViewMode);

        // メモフォルダ設定
        if (MainApplication.getInstance(this).getCurrentMemoFolder() == null)
        {
            MainApplication.getInstance(this).setCurrentMemoFolder(MainPreferenceActivity.getMemoLocation(this));
        }

        // ファイルリスト再生成
        if ((this.memoListViewMode != MemoListViewMode.SEARCH_VIEW) || (this.mCurrentFolderMemoFileList.size() == 0)
                || (this.memoCreator == null) || (this.memoCreator.isCancelled())
                || (MainApplication.getInstance(this).isUpdateMemo()))
        {
            // エディタによる更新ありをリセット
            MainApplication.getInstance(this).setUpdateMemo(false);

            refreshMemoList();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d("Kumagusu", "*** START onPause()");

        // タイマ開始
        MainApplication.getInstance(this).getPasswordTimer().start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Log.d("Kumagusu", "*** START onDestroy()");

        // ワーカスレッド破棄
        if (this.memoCreator != null)
        {
            this.memoCreator.cancelTask(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d("Kumagusu", "*** START onSaveInstanceState()");

        super.onSaveInstanceState(outState);

        // 選択中メモファイルパス
        outState.putString("selectedMemoFilePath", this.selectedMemoFilePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.d("Kumagusu", "*** START onRestoreInstanceState()");

        // 選択中メモファイルパス
        if (savedInstanceState.containsKey("selectedMemoFilePath"))
        {
            this.selectedMemoFilePath = savedInstanceState.getString("selectedMemoFilePath");
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        Log.d("Kumagusu", "*** START onCreateOptionsMenu()");

        if (this.memoListViewMode == MemoListViewMode.FOLDER_VIEW)
        {
            // メニュー項目「新規」
            MenuItem actionItemCreate = menu.add(Menu.NONE, MENU_ID_CREATE_MEMO, Menu.NONE, R.string.ui_create);
            actionItemCreate.setIcon(R.drawable.memo_folder_add);
            ActivityCompat.setShowAsAction4ActionBar(actionItemCreate);
        }

        // メニュー項目「リフレッシュ」
        MenuItem actionItemRefresh = menu.add(Menu.NONE, MENU_ID_REFRESH, Menu.NONE, R.string.ui_refresh);
        actionItemRefresh.setIcon(R.drawable.refresh);
        ActivityCompat.setShowAsAction4ActionBar(actionItemRefresh);

        if (this.memoListViewMode == MemoListViewMode.FOLDER_VIEW)
        {
            // メニュー項目「検索」
            MenuItem actionItemSearch = menu.add(Menu.NONE, MENU_ID_SEARCH, Menu.NONE, R.string.ui_search);
            actionItemSearch.setIcon(R.drawable.search);
            ActivityCompat.setShowAsAction4ActionBar(actionItemSearch);
        }

        // メニュー項目「並び替え方法」
        MenuItem actionMemoSortMedhod = menu.add(Menu.NONE, MENU_ID_SORT_METHOD, Menu.NONE,
                R.string.memo_sort_method_dialog_title);
        actionMemoSortMedhod.setIcon(R.drawable.memo_sort);
        ActivityCompat.setShowAsAction4ActionBar(actionMemoSortMedhod);

        // メニュー項目「設定」
        MenuItem actionItemSetting = menu.add(Menu.NONE, MENU_ID_SETTING, Menu.NONE, R.string.ui_setting);
        actionItemSetting.setIcon(R.drawable.setting);
        ActivityCompat.setShowAsAction4ActionBar(actionItemSetting);

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean ret = super.onOptionsItemSelected(item);

        Log.d("Kumagusu", "*** START onOptionsItemSelected()");

        switch (item.getItemId())
        {
        case MENU_ID_SETTING: // 設定
            // 設定画面を表示
            Intent prefIntent = new Intent(Kumagusu.this, MainPreferenceActivity.class);

            startActivity(prefIntent);
            break;

        case MENU_ID_SORT_METHOD: // 並び替え方法
            int memoSortMethod = MainPreferenceActivity.getMemoSortMethod(this,
                    ((this.memoListViewMode == MemoListViewMode.FOLDER_VIEW) ? 0 : 1));
            int memoSortMothodIndex = 0;
            final int[] memoSortMethodValues = getResources().getIntArray(R.array.memo_sort_method_kind_values);

            for (int i = 0; i < memoSortMethodValues.length; i++)
            {
                if (memoSortMethod == memoSortMethodValues[i])
                {
                    memoSortMothodIndex = i;
                    break;
                }
            }

            ListDialogFragment.newInstance(DIALOG_ID_LIST_MEMO_SORT_METHOD, R.drawable.memo_sort,
                    R.string.memo_sort_method_dialog_title, 0,
                    getResources().getStringArray(R.array.memo_sort_method_kind_entries), memoSortMothodIndex).show(
                    getSupportFragmentManager(), "");
            break;

        case MENU_ID_SEARCH: // 検索（メモ検索）
            final InputDialog searchMemoDialog = new InputDialog(Kumagusu.this);
            searchMemoDialog.showDialog(this.getResources().getDrawable(R.drawable.search), this.getResources()
                    .getString(R.string.search_memo_control_dialog_title), InputType.TYPE_CLASS_TEXT,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (searchMemoDialog.getText().length() > 0)
                            {
                                // メモリストの状態をクリア
                                MainApplication.getInstance(Kumagusu.this).setMemoListStatus4Search(null);

                                // Activetyを呼び出す
                                Intent intent = new Intent(Kumagusu.this, Kumagusu.class);
                                intent.putExtra("CURRENT_FOLDER", MainApplication.getInstance(Kumagusu.this)
                                        .getCurrentMemoFolder());
                                intent.putExtra("VIEW_MODE", "SEARCH");
                                intent.putExtra("SEARCH_WORDS", searchMemoDialog.getText());

                                startActivity(intent);
                            }
                        }
                    }, null);
            break;

        case MENU_ID_REFRESH: // 最新の情報に更新
            refreshMemoList();
            break;

        case MENU_ID_CREATE_MEMO: // 新規
            ListDialogFragment.newInstance(DIALOG_ID_LIST_MEMO_LIST_CONTROL, R.drawable.memo_folder_add,
                    R.string.memo_list_control_dialog_title, 0,
                    getResources().getStringArray(R.array.memo_list_control_dialog_entries)).show(
                    getSupportFragmentManager(), "");
            break;

        case android.R.id.home: // UPアイコン
            // 上位Activetyを呼び出す
            finish();
            break;

        default:
            break;
        }

        return ret;
    }

    /**
     * ファイルリストをリフレッシュする.
     */
    private void refreshMemoList()
    {
        // ビルダを生成
        this.memoBuilder = new MemoBuilder(this, MainPreferenceActivity.getEncodingName(this),
                MainPreferenceActivity.isTitleLink(this));

        // ファイルリスト再生成
        clearMemoList();
        createMemoList();
    }

    /**
     * メモリストをクリアする.
     */
    private void clearMemoList()
    {
        // メモリスト生成処理をキャンセル
        if (this.memoCreator != null)
        {
            this.memoCreator.cancel(true);
            this.memoCreator = null;
        }

        // タイトルをクリア
        setTitle("");

        // リスト初期化
        @SuppressWarnings("unchecked")
        ArrayAdapter<IMemo> adapter = (ArrayAdapter<IMemo>) this.mListView.getAdapter();
        if (adapter != null)
        {
            adapter.clear();
        }

        this.mCurrentFolderFileQueue.clear();
    }

    /**
     * メモのリストを生成する.
     */
    private void createMemoList()
    {
        // ファイルリストを取得
        switch (this.memoListViewMode)
        {
        case FOLDER_VIEW: // フォルダ表示
            // フォルダ内のメモを表示
            memoFolderView();
            break;

        case SEARCH_VIEW: // メモ検索表示
            // 検索結果のメモを表示
            memoSearchView(MainApplication.getInstance(this).getCurrentMemoFolder());
            break;

        default:
            break;
        }

        return;
    }

    /**
     * メモリストをフォルダ表示する.
     */
    private void memoFolderView()
    {
        for (File f : getFileList(MainApplication.getInstance(this).getCurrentMemoFolder()))
        {
            this.mCurrentFolderFileQueue.add(f);
        }

        this.memoCreator = new MemoCreateTask(this, this.memoListViewMode, this.mCurrentFolderFileQueue,
                this.memoBuilder, this.mListView, this.mCurrentFolderMemoFileList, this.memoListComparator);
        this.memoCreator.execute();
    }

    /**
     * メモを検索する.
     *
     * @param searchFolder 検索フォルダ
     */
    private void memoSearchView(String searchFolder)
    {
        // 検索結果リストのクローズ手段を設定
        Button closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // アクティビティを終了
                finish();
            }
        });

        ActivityCompat.setCloseSearchResultFunction(this, closeButton);

        // メモを検索
        if (MainApplication.getInstance(Kumagusu.this).getCurrentMemoFolder() != null)
        {
            this.memoCreator = new MemoSearchTask(this, this.memoListViewMode, MainApplication.getInstance(this)
                    .getCurrentMemoFolder(), this.memoBuilder, this.mListView, this.mCurrentFolderMemoFileList,
                    this.memoListComparator, this.searchWords);
            this.memoCreator.execute();
        }
    }

    /**
     * 指定フォルダ内のファイルリストを取得する.
     *
     * @param targetFolder 指定フォルダ
     * @return ファイルリスト
     */
    private File[] getFileList(String targetFolder)
    {
        // カレントフォルダのファイルオブジェクト取得
        File currentFolderfile = new File(targetFolder);

        // カレントフォルダが存在しなければ作成
        if (!currentFolderfile.exists())
        {
            currentFolderfile.mkdirs();
        }

        // 親フォルダへの移動手段を設定（最上位以外）
        if (!targetFolder.equals(MainPreferenceActivity.getMemoLocation(this)))
        {
            ActivityCompat.setUpFolderFunction(this, this.mCurrentFolderMemoFileList,
                    this.memoBuilder.build(currentFolderfile.getParent(), MemoType.ParentFolder));
        }

        // カレントフォルダのファイル一覧を取得
        File[] currentFolderFileList = currentFolderfile.listFiles();

        if (currentFolderFileList == null)
        {
            currentFolderFileList = new File[0];
        }

        Arrays.sort(currentFolderFileList);

        return currentFolderFileList;
    }

    /**
     * リストの表示位置を保存する.
     */
    private void saveListViewStatus()
    {
        MainApplication.MemoListViewStatus listViewStatus = new MainApplication.MemoListViewStatus();

        listViewStatus.setLastFolder(MainApplication.getInstance(this).getCurrentMemoFolder());
        listViewStatus.setLastTopPosition(this.mListView.getFirstVisiblePosition());
        if (this.mListView.getChildCount() > 0)
        {
            listViewStatus.setLastTopPositionY(this.mListView.getChildAt(0).getTop());
        }

        switch (this.memoListViewMode)
        {
        case FOLDER_VIEW:
            MainApplication.getInstance(this).pushMemoListStatusStack(listViewStatus);
            break;

        case SEARCH_VIEW:
            MainApplication.getInstance(this).setMemoListStatus4Search(listViewStatus);
            break;

        default:
            break;
        }
    }

    /**
     * メモのメモ種別を変更する.
     *
     * @param srcMemoFile 変更するメモ
     * @param dstMemoType 変更先のメモ種別
     * @param refreshPassword パスワードを再入力
     */
    private void changeMemoType(MemoFile srcMemoFile, MemoType dstMemoType, final boolean refreshPassword)
    {
        // ファイル名のランダム化設定値により暗号化種別を変更
        if ((dstMemoType == MemoType.Secret1) && (MainPreferenceActivity.isRandamName(this)))
        {
            dstMemoType = MemoType.Secret2;
        }

        // パスワードを再入力
        final String oldLastPassword;
        if (refreshPassword)
        {
            oldLastPassword = MainApplication.getInstance(this).getLastCorrectPassword();
            MainApplication.getInstance(this).setLastCorrectPassword(null);
        }
        else
        {
            oldLastPassword = null;
        }

        // 元ファイル読み込み
        String srcMemoData = srcMemoFile.getText();

        MemoBuilder mb = new MemoBuilder(this, MainPreferenceActivity.getEncodingName(this),
                MainPreferenceActivity.isTitleLink(this));

        if ((dstMemoType != MemoType.Text) && (MainApplication.getInstance(this).getLastCorrectPassword() == null))
        {
            final MemoFile srcMemoFileTemp = srcMemoFile;
            final MemoType dstMemoTypeTemp = dstMemoType;

            // パスワードを入力し、暗号化ファイル保存
            Utilities.inputPassword(this, new DialogInterface.OnClickListener()
            {
                /**
                 * 入力パスワードが有効時を処理する.
                 */
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // 再呼出
                    changeMemoType(srcMemoFileTemp, dstMemoTypeTemp, false);
                }
            }, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // キャンセル処理
                    MainApplication.getInstance(Kumagusu.this).setLastCorrectPassword(oldLastPassword);
                }
            });
        }
        else
        {
            MemoFile dstMemoFile = (MemoFile) mb.build(srcMemoFile.getParent(), dstMemoType);

            if (dstMemoFile.setText(MainApplication.getInstance(this).getLastCorrectPassword(), srcMemoData))
            {
                // メモ種別の変更に成功した場合、元のファイルを削除
                if (!srcMemoFile.getPath().equals(dstMemoFile.getPath()))
                {
                    MemoUtilities.deleteFile(srcMemoFile.getPath());

                    // メモリストを更新
                    refreshMemoList();
                }
            }
        }
    }

    /**
     * 並び替え方法を変更する.
     *
     * @param method 並び替え方法
     */
    private void setMemoSortMethod(int method)
    {
        // ソート方法を保存
        MainPreferenceActivity.setMemoSortMethod(this, method,
                ((this.memoListViewMode == MemoListViewMode.FOLDER_VIEW) ? 0 : 1));

        // 再ソート
        MemoListAdapter adapter = (MemoListAdapter) this.mListView.getAdapter();

        if (adapter != null)
        {
            this.memoCreator.sort();
        }
    }

    /**
     * 選択中メモファイルを取得する.
     *
     * @return 選択中メモファイル
     */
    private IMemo getSelectedMemoFile()
    {
        @SuppressWarnings("unchecked")
        ArrayAdapter<IMemo> adapter = (ArrayAdapter<IMemo>) Kumagusu.this.mListView.getAdapter();

        IMemo selectedMemoFileTemp = null;

        for (int i = 0; i < adapter.getCount(); i++)
        {
            if (Kumagusu.this.selectedMemoFilePath.equals(adapter.getItem(i).getPath()))
            {
                selectedMemoFileTemp = adapter.getItem(i);
                break;
            }
        }

        return selectedMemoFileTemp;
    }

    /**
     * 確認ダイアログのリスナを初期化する.
     */
    private void initConfirmDialogListener()
    {
        // ファイル削除
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_DELETE_FILE, new DialogListeners(new OnClickListener()
        {
            /**
             * 削除「OK 」 イベントを処理する 。
             */
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                MemoUtilities.deleteFile(getSelectedMemoFile().getPath());

                // メモリストを更新
                refreshMemoList();
            }
        }, null, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // いいえは無視
            }
        }));

        // フォルダ削除
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_DELETE_FOLDER, new DialogListeners(new OnClickListener()
        {
            /**
             * 削除「OK 」 イベントを処理する 。
             */
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (!MemoUtilities.deleteFile(getSelectedMemoFile().getPath()))
                {
                    ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_DELETE_FOLDER_ERROR,
                            android.R.drawable.ic_menu_info_details, R.string.folder_control_dialog_delete_error, 0,
                            ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(getSupportFragmentManager(), "");
                }

                // メモリストを更新
                refreshMemoList();

            }
        }, null, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // いいえは無視
            }
        }));

        // フォルダ削除エラー
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_DELETE_FOLDER_ERROR, new DialogListeners(null, null, null));

        // フォルダ名変更エラー（フォルダ名指定なし）
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_RENAME_FOLDER_ERROR_NONAME, new DialogListeners(null, null, null));

        // フォルダ名変更エラー（フォルダ名重複）
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_RENAME_FOLDER_ERROR_CONFLICT, new DialogListeners(null, null, null));

        // フォルダ追加エラー（フォルダ名重複）
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_ADD_FOLDER_ERROR_CONFLICT, new DialogListeners(null, null, null));

        // フォルダ追加エラー（フォルダ名指定なし）
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_ADD_FOLDER_ERROR_NONAME, new DialogListeners(null, null, null));
    }

    @Override
    public DialogListeners getConfirmDialogListeners(int listenerId)
    {
        // 確認ダイアログ保管データを返す
        return this.dialogListenerMap.get(listenerId);
    }

    @Override
    public void putConfirmDialogListeners(int listenerId, DialogListeners listeners)
    {
        // 確認ダイアログデータを追加
        this.dialogListenerMap.put(listenerId, listeners);
    }

    /**
     * リストダイアログのリスナを初期化する.
     */
    private void initListDialogListener()
    {
        // メモ操作
        putListDialogListeners(DIALOG_ID_LIST_MEMO_FILE_CONTROL, new DialogListeners(new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // リストのインスタンスを取得
                File file = new File(Kumagusu.this.selectedMemoFilePath);

                if (!file.exists())
                {
                    return;
                }

                // 選択中メモファイルを取得
                final IMemo selectedMemoFile = getSelectedMemoFile();

                if (selectedMemoFile == null)
                {
                    return;
                }

                switch (which)
                {
                case FILE_CONTROL_ID_COPY: // コピー
                    // コピー先フォルダ選択ダイアログを表示
                    DirectorySelectDialog copyDialog = new DirectorySelectDialog(Kumagusu.this,
                            getString(R.string.memo_file_control_dialog_copy_title), MainPreferenceActivity
                                    .getMemoLocation(Kumagusu.this), selectedMemoFile.getParent());

                    copyDialog.setOnFileListDialogListener(new OnDirectoryListDialogListener()
                    {
                        /**
                         * コピー先ディレクトリ指定完了のイベントを処理する 。
                         */
                        @Override
                        public void onClickFileList(String path)
                        {
                            if (path != null)
                            {
                                MemoUtilities.copyMemoFile((MemoFile) selectedMemoFile, path);

                                // メモリストを更新
                                refreshMemoList();
                            }
                        }
                    });

                    copyDialog.show();
                    break;

                case FILE_CONTROL_ID_MOVE: // 移動
                    // 移動先フォルダ選択ダイアログを表示
                    DirectorySelectDialog moveDialog = new DirectorySelectDialog(Kumagusu.this,
                            getString(R.string.memo_file_control_dialog_move_title), MainPreferenceActivity
                                    .getMemoLocation(Kumagusu.this), selectedMemoFile.getParent());

                    moveDialog.setOnFileListDialogListener(new OnDirectoryListDialogListener()
                    {
                        /**
                         * 移動先ディレクトリ指定完了のイベントを処理する 。
                         */
                        @Override
                        public void onClickFileList(String path)
                        {
                            if (path != null)
                            {
                                MemoUtilities.moveMemoFile((MemoFile) selectedMemoFile, path);

                                // メモリストを更新
                                refreshMemoList();
                            }
                        }
                    });

                    moveDialog.show();
                    break;

                case FILE_CONTROL_ID_DELETE: // 削除
                    // 削除の確認ダイアログを表示
                    ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_DELETE_FILE, android.R.drawable.ic_menu_delete,
                            R.string.memo_file_control_dialog_delete_title,
                            R.string.memo_file_control_dialog_delete_message,
                            ConfirmDialogFragment.POSITIVE_CAPTION_KIND_YES).show(getSupportFragmentManager(), "");
                    break;

                case FILE_CONTROL_ID_ENCRYPT_OR_DECRYPT: // 暗号化・復号化
                    changeMemoType((MemoFile) selectedMemoFile, getChange2MemoType(selectedMemoFile), false);
                    break;

                case FILE_CONTROL_ID_ENCRYPT_NEW_PASSWORD: // 暗号化(ﾊﾟｽﾜｰﾄﾞ入力)
                    if (getChange2MemoType(selectedMemoFile) == MemoType.Secret1)
                    {
                        changeMemoType((MemoFile) selectedMemoFile, getChange2MemoType(selectedMemoFile), true);
                    }
                    break;

                default:
                    break;
                }
            }
        }));

        // フォルダ操作
        putListDialogListeners(DIALOG_ID_LIST_FOLDER_CONTROL, new DialogListeners(new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // リストのインスタンスを取得
                File file = new File(Kumagusu.this.selectedMemoFilePath);

                if (!file.exists())
                {
                    // コピー元が存在しない
                    return;
                }

                // 選択中メモファイルを取得
                final IMemo selectedMemoFile = getSelectedMemoFile();

                if (selectedMemoFile == null)
                {
                    return;
                }

                switch (which)
                {
                case FOLDER_CONTROL_ID_COPY: // コピー
                    // コピー先フォルダ選択ダイアログを表示
                    DirectorySelectDialog copyDialog = new DirectorySelectDialog(Kumagusu.this,
                            getString(R.string.folder_control_dialog_copy_title), MainPreferenceActivity
                                    .getMemoLocation(Kumagusu.this), selectedMemoFile.getParent());

                    copyDialog.setOnFileListDialogListener(new OnDirectoryListDialogListener()
                    {
                        /**
                         * コピー先ディレクトリ指定完了のイベントを処理する 。
                         */
                        @Override
                        public void onClickFileList(String path)
                        {
                            if (path != null)
                            {
                                MemoUtilities.copyMemoFolder((MemoFolder) selectedMemoFile, path);

                                // メモリストを更新
                                refreshMemoList();
                            }
                        }
                    });

                    copyDialog.show();
                    break;

                case FOLDER_CONTROL_ID_MOVE: // 移動
                    // 移動先フォルダ選択ダイアログを表示
                    DirectorySelectDialog moveDialog = new DirectorySelectDialog(Kumagusu.this,
                            getString(R.string.folder_control_dialog_move_title), MainPreferenceActivity
                                    .getMemoLocation(Kumagusu.this), selectedMemoFile.getParent());

                    moveDialog.setOnFileListDialogListener(new OnDirectoryListDialogListener()
                    {
                        /**
                         * 移動先ディレクトリ指定完了のイベントを処理する 。
                         */
                        @Override
                        public void onClickFileList(String path)
                        {
                            if (path != null)
                            {
                                MemoUtilities.moveMemoFolder((MemoFolder) selectedMemoFile, path);

                                // メモリストを更新
                                refreshMemoList();
                            }
                        }
                    });

                    moveDialog.show();
                    break;

                case FOLDER_CONTROL_ID_DELETE: // 削除
                    // 削除の確認ダイアログを表示
                    ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_DELETE_FOLDER,
                            android.R.drawable.ic_menu_delete, R.string.folder_control_dialog_delete_title,
                            R.string.folder_control_dialog_delete_message,
                            ConfirmDialogFragment.POSITIVE_CAPTION_KIND_YES).show(getSupportFragmentManager(), "");
                    break;

                case FOLDER_CONTROL_ID_RENAME: // 名称変更
                    final InputDialog renameFolderDialog = new InputDialog(Kumagusu.this);
                    renameFolderDialog.setText(selectedMemoFile.getName());

                    renameFolderDialog.showDialog(
                            Kumagusu.this.getResources().getDrawable(R.drawable.folder_operation), Kumagusu.this
                                    .getResources().getString(R.string.folder_rename_control_dialog_title),
                            InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    String folderName = MemoUtilities.sanitizeFileNameString(renameFolderDialog
                                            .getText());

                                    if (folderName.length() == 0)
                                    {
                                        // フォルダ名が空
                                        ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_RENAME_FOLDER_ERROR_NONAME,
                                                android.R.drawable.ic_menu_info_details,
                                                R.string.folder_rename_control_dialog_error_noinput, 0,
                                                ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(
                                                getSupportFragmentManager(), "");
                                        return;
                                    }

                                    if (!folderName.equals(selectedMemoFile.getName()))
                                    {
                                        File srcFolderFile = new File(selectedMemoFile.getPath());
                                        File newFolderFile = new File(selectedMemoFile.getParent(), folderName);

                                        if (!newFolderFile.exists())
                                        {
                                            srcFolderFile.renameTo(newFolderFile);

                                            // メモリストを更新
                                            refreshMemoList();
                                        }
                                        else
                                        {
                                            // すでに同名のフォルダまたはファイルが存在
                                            ConfirmDialogFragment.newInstance(
                                                    DIALOG_ID_CONFIRM_RENAME_FOLDER_ERROR_CONFLICT,
                                                    android.R.drawable.ic_menu_info_details,
                                                    R.string.folder_rename_control_dialog_error_duplicate, 0,
                                                    ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(
                                                    getSupportFragmentManager(), "");
                                        }
                                    }
                                }
                            }, null);
                    break;

                default:
                    break;
                }
            }
        }));

        // 並び替え方法選択
        putListDialogListeners(DIALOG_ID_LIST_MEMO_SORT_METHOD, new DialogListeners(new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int[] memoSortMethodValues = getResources().getIntArray(R.array.memo_sort_method_kind_values);
                setMemoSortMethod(memoSortMethodValues[which]);
            }
        }));

        // メモリスト操作
        putListDialogListeners(DIALOG_ID_LIST_MEMO_LIST_CONTROL, new DialogListeners(new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                case FILE_LIST_CONTROL_ID_ADD_MEMO: // メモ追加
                    // エディタを開始
                    Intent editIntent = new Intent(Kumagusu.this, EditorActivity.class);

                    editIntent.putExtra("FULL_PATH", (String) null);
                    editIntent.putExtra("CURRENT_FOLDER", MainApplication.getInstance(Kumagusu.this)
                            .getCurrentMemoFolder());

                    startActivity(editIntent);
                    break;

                case FILE_LIST_CONTROL_ID_ADD_FOLDER: // フォルダ追加
                    final InputDialog addFolderDialog = new InputDialog(Kumagusu.this);
                    addFolderDialog.showDialog(Kumagusu.this.getResources().getDrawable(R.drawable.folder_add),
                            Kumagusu.this.getResources().getString(R.string.folder_add_control_dialog_title),
                            InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    String folderName = MemoUtilities.sanitizeFileNameString(addFolderDialog.getText());

                                    if (folderName.length() > 0)
                                    {
                                        File addFolderFile = new File(MainApplication.getInstance(Kumagusu.this)
                                                .getCurrentMemoFolder(), folderName);

                                        if (!addFolderFile.exists())
                                        {
                                            addFolderFile.mkdirs();

                                            // メモリストを更新
                                            refreshMemoList();
                                        }
                                        else
                                        {
                                            // すでに同名のフォルダまたはファイルが存在
                                            ConfirmDialogFragment.newInstance(
                                                    DIALOG_ID_CONFIRM_ADD_FOLDER_ERROR_CONFLICT,
                                                    android.R.drawable.ic_menu_info_details,
                                                    R.string.memo_list_control_dialog_add_error_duplicate, 0,
                                                    ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(
                                                    getSupportFragmentManager(), "");
                                        }
                                    }
                                    else
                                    {
                                        // フォルダ名が空
                                        ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_ADD_FOLDER_ERROR_NONAME,
                                                android.R.drawable.ic_menu_info_details,
                                                R.string.memo_list_control_dialog_add_error_noinput, 0,
                                                ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(
                                                getSupportFragmentManager(), "");
                                    }
                                }
                            }, null);
                    break;

                default:
                    break;
                }
            }
        }));
    }

    /**
     * メモ操作・メモ種別変更で、変更先メモ種別を取得する.
     *
     * @param memoFile 変更元メモ
     * @return 変更先メモ種別
     */
    private MemoType getChange2MemoType(IMemo memoFile)
    {
        MemoType change2MemoType;
        if (memoFile.getMemoType() == MemoType.Text)
        {
            change2MemoType = MemoType.Secret1;
        }
        else
        {
            change2MemoType = MemoType.Text;
        }

        return change2MemoType;
    }

    @Override
    public DialogListeners getListDialogListeners(int listenerId)
    {
        // リストダイアログ保管データを返す
        return this.dialogListenerMap.get(listenerId);
    }

    @Override
    public void putListDialogListeners(int listenerId, DialogListeners listeners)
    {
        // リストダイアログデータを追加
        this.dialogListenerMap.put(listenerId, listeners);
    }
}
