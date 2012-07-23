package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jp.gr.java_conf.kumagusu.compat.ActivityCompat;
import jp.gr.java_conf.kumagusu.control.ConfirmDialog;
import jp.gr.java_conf.kumagusu.control.DirectorySelectDialog;
import jp.gr.java_conf.kumagusu.control.DirectorySelectDialog.OnDirectoryListDialogListener;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.control.ListDialog;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoFolder;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

/**
 * メモ一覧. Root Activity
 */
@SuppressLint("NewApi")
public final class Kumagusu extends Activity
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
     * メニュー項目「設定」.
     */
    private static final int MENU_ID_SETTING = (Menu.FIRST + 3);

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
     * 子Activity起動中.
     */
    private boolean mExecutedChildActivityFg = false;

    /**
     * 自動クローズタイマ.
     */
    private Timer mAutoCloseTimer;

    /**
     * 選択中のメモ.
     */
    private IMemo mSelectedMemoFile;

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
    private MemoCreator memoCreator = null;

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
     * 検索文字.
     */
    private String searchStrings = null;

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
        ActivityCompat.initActivity(this, R.layout.main, R.drawable.icon, null, false);

        Log.d("Kumagusu", "*** START onCreate()");

        // Fileユーティリティ初期化
        MemoUtilities.setResources(getResources());

        // 自動クローズタイマ処理生成
        this.mAutoCloseTimer = new Timer(this);
        this.mExecutedChildActivityFg = false;

        // リストのインスタンスを取得
        this.mListView = (ListView) findViewById(R.id.list);

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
                        // 子Activity起動中設定
                        mExecutedChildActivityFg = true;

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
                    Kumagusu.this.mSelectedMemoFile = selectedItem;

                    // ダイアログ表示
                    switch (selectedItem.getMemoType())
                    {
                    case Text:
                    case Secret1:
                    case Secret2:

                        String[] dialogEntries;
                        final MemoType change2MemoType;
                        if (selectedItem.getMemoType() == MemoType.Text)
                        {
                            dialogEntries = getResources().getStringArray(
                                    R.array.memo_file_control_dialog_entries_4_text);
                            change2MemoType = MemoType.Secret1;
                        }
                        else
                        {
                            dialogEntries = getResources().getStringArray(
                                    R.array.memo_file_control_dialog_entries_4_secret);
                            change2MemoType = MemoType.Text;
                        }

                        ListDialog.showDialog(Kumagusu.this, getResources().getDrawable(R.drawable.memo_operation),
                                getResources().getString(R.string.memo_file_control_dialog_title), dialogEntries,
                                new OnClickListener()
                                {
                                    /**
                                     * メモ操作ダイアログの項目選択イベントを処理する。
                                     */
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // リストのインスタンスを取得
                                        File file = new File(Kumagusu.this.mSelectedMemoFile.getPath());

                                        if (!file.exists())
                                        {
                                            return;
                                        }

                                        switch (which)
                                        {
                                        case FILE_CONTROL_ID_COPY: // コピー
                                            // コピー先フォルダ選択ダイアログを表示
                                            DirectorySelectDialog copyDialog = new DirectorySelectDialog(Kumagusu.this);
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
                                                        MemoUtilities.copyMemoFile(
                                                                (MemoFile) Kumagusu.this.mSelectedMemoFile, path);

                                                        // メモリストを更新
                                                        refreshMemoList();
                                                    }
                                                }
                                            });
                                            copyDialog.show(MainPreferenceActivity.getMemoLocation(Kumagusu.this),
                                                    Kumagusu.this.mSelectedMemoFile.getParent());
                                            break;

                                        case FILE_CONTROL_ID_MOVE: // 移動
                                            // 移動先フォルダ選択ダイアログを表示
                                            DirectorySelectDialog moveDialog = new DirectorySelectDialog(Kumagusu.this);
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
                                                        MemoUtilities.moveMemoFile(
                                                                (MemoFile) Kumagusu.this.mSelectedMemoFile, path);

                                                        // メモリストを更新
                                                        refreshMemoList();
                                                    }
                                                }
                                            });
                                            moveDialog.show(MainPreferenceActivity.getMemoLocation(Kumagusu.this),
                                                    Kumagusu.this.mSelectedMemoFile.getParent());
                                            break;

                                        case FILE_CONTROL_ID_DELETE: // 削除
                                            // 削除の確認ダイアログを表示
                                            ConfirmDialog.showDialog(
                                                    Kumagusu.this,
                                                    getResources().getDrawable(android.R.drawable.ic_menu_delete),
                                                    getResources().getString(
                                                            R.string.memo_file_control_dialog_delete_title),
                                                    getResources().getString(
                                                            R.string.memo_file_control_dialog_delete_message),
                                                    ConfirmDialog.PositiveCaptionKind.YES, new OnClickListener()
                                                    {
                                                        /**
                                                         * 削除「OK 」 イベントを処理する 。
                                                         */
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            MemoUtilities.deleteFile(Kumagusu.this.mSelectedMemoFile
                                                                    .getPath());

                                                            // メモリストを更新
                                                            refreshMemoList();
                                                        }
                                                    }, new OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            // いいえは無視
                                                        }
                                                    });
                                            break;

                                        case FILE_CONTROL_ID_ENCRYPT_OR_DECRYPT: // 暗号化・復号化
                                            changeMemoType((MemoFile) Kumagusu.this.mSelectedMemoFile, change2MemoType);
                                            break;

                                        default:
                                            break;
                                        }
                                    }
                                });
                        break;

                    case Folder:
                        ListDialog.showDialog(Kumagusu.this, getResources().getDrawable(R.drawable.folder_operation),
                                getResources().getString(R.string.folder_control_dialog_title), getResources()
                                        .getStringArray(R.array.memo_file_control_dialog_entries_4_folder),
                                new OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // リストのインスタンスを取得
                                        File file = new File(Kumagusu.this.mSelectedMemoFile.getPath());

                                        if (!file.exists())
                                        {
                                            // コピー元が存在しない
                                            return;
                                        }

                                        switch (which)
                                        {
                                        case FOLDER_CONTROL_ID_COPY: // コピー
                                            // コピー先フォルダ選択ダイアログを表示
                                            DirectorySelectDialog copyDialog = new DirectorySelectDialog(Kumagusu.this);
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
                                                        MemoUtilities.copyMemoFolder(
                                                                (MemoFolder) Kumagusu.this.mSelectedMemoFile, path);

                                                        // メモリストを更新
                                                        refreshMemoList();
                                                    }
                                                }
                                            });
                                            copyDialog.show(MainPreferenceActivity.getMemoLocation(Kumagusu.this),
                                                    Kumagusu.this.mSelectedMemoFile.getParent());
                                            break;

                                        case FOLDER_CONTROL_ID_MOVE: // 移動
                                            // 移動先フォルダ選択ダイアログを表示
                                            DirectorySelectDialog moveDialog = new DirectorySelectDialog(Kumagusu.this);
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
                                                        MemoUtilities.moveMemoFolder(
                                                                (MemoFolder) Kumagusu.this.mSelectedMemoFile, path);

                                                        // メモリストを更新
                                                        refreshMemoList();
                                                    }
                                                }
                                            });
                                            moveDialog.show(MainPreferenceActivity.getMemoLocation(Kumagusu.this),
                                                    Kumagusu.this.mSelectedMemoFile.getParent());
                                            break;

                                        case FOLDER_CONTROL_ID_DELETE: // 削除
                                            // 削除の確認ダイアログを表示
                                            ConfirmDialog.showDialog(
                                                    Kumagusu.this,
                                                    getResources().getDrawable(android.R.drawable.ic_menu_delete),
                                                    getResources().getString(
                                                            R.string.folder_control_dialog_delete_title),
                                                    getResources().getString(
                                                            R.string.folder_control_dialog_delete_message),
                                                    ConfirmDialog.PositiveCaptionKind.YES, new OnClickListener()
                                                    {
                                                        /**
                                                         * 削除「OK 」 イベントを処理する 。
                                                         */
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            if (!MemoUtilities
                                                                    .deleteFile(Kumagusu.this.mSelectedMemoFile
                                                                            .getPath()))
                                                            {
                                                                ConfirmDialog
                                                                        .showDialog(
                                                                                Kumagusu.this,
                                                                                getResources()
                                                                                        .getDrawable(
                                                                                                android.R.drawable.ic_menu_info_details),
                                                                                getResources()
                                                                                        .getString(
                                                                                                R.string.folder_control_dialog_delete_error),

                                                                                null,
                                                                                ConfirmDialog.PositiveCaptionKind.OK,
                                                                                null, null);
                                                            }

                                                            // メモリストを更新
                                                            refreshMemoList();

                                                        }
                                                    }, new OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            // いいえは無視
                                                        }
                                                    });

                                            break;

                                        case FOLDER_CONTROL_ID_RENAME: // 名称変更
                                            final InputDialog renameFolderDialog = new InputDialog();
                                            renameFolderDialog.setText(Kumagusu.this.mSelectedMemoFile.getName());

                                            renameFolderDialog.showDialog(Kumagusu.this, Kumagusu.this.getResources()
                                                    .getString(R.string.folder_rename_control_dialog_title),
                                                    InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            String folderName = MemoUtilities
                                                                    .sanitizeFileNameString(renameFolderDialog
                                                                            .getText());

                                                            if (folderName.length() == 0)
                                                            {
                                                                // フォルダ名が空
                                                                ConfirmDialog
                                                                        .showDialog(
                                                                                Kumagusu.this,
                                                                                getResources()
                                                                                        .getDrawable(
                                                                                                android.R.drawable.ic_menu_info_details),
                                                                                getResources()
                                                                                        .getString(
                                                                                                R.string.folder_rename_control_dialog_error_noinput),
                                                                                null,
                                                                                ConfirmDialog.PositiveCaptionKind.OK,
                                                                                null, null);

                                                                return;
                                                            }

                                                            if (!folderName.equals(Kumagusu.this.mSelectedMemoFile
                                                                    .getName()))
                                                            {
                                                                File srcFolderFile = new File(
                                                                        Kumagusu.this.mSelectedMemoFile.getPath());
                                                                File newFolderFile = new File(
                                                                        Kumagusu.this.mSelectedMemoFile.getParent(),
                                                                        folderName);

                                                                if (!newFolderFile.exists())
                                                                {
                                                                    srcFolderFile.renameTo(newFolderFile);

                                                                    // メモリストを更新
                                                                    refreshMemoList();
                                                                }
                                                                else
                                                                {
                                                                    // すでに同名のフォルダまたはファイルが存在
                                                                    ConfirmDialog
                                                                            .showDialog(
                                                                                    Kumagusu.this,
                                                                                    getResources()
                                                                                            .getDrawable(
                                                                                                    android.R.drawable.ic_menu_info_details),
                                                                                    getResources()
                                                                                            .getString(
                                                                                                    R.string.folder_rename_control_dialog_error_duplicate),
                                                                                    null,
                                                                                    ConfirmDialog.PositiveCaptionKind.OK,
                                                                                    null, null);
                                                                }
                                                            }
                                                        }
                                                    }, null);
                                            break;

                                        default:
                                            break;
                                        }
                                    }
                                });
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

        // メモリスト処理初期化
        initMemoList();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d("Kumagusu", "*** START onPause()");

        // タイマ開始
        if (this.mAutoCloseTimer != null)
        {
            this.mAutoCloseTimer.start();
        }

        // ワーカスレッド破棄
        if (this.memoCreator != null)
        {
            this.memoCreator.cancel(true);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.d("Kumagusu", "*** START onResume()");

        // アプリケーションが非実行中のとき、即終了
        if (this.mAutoCloseTimer == null)
        {
            return;
        }

        // タイムアウトの確認
        if ((this.mAutoCloseTimer.stop()) && (!this.mExecutedChildActivityFg))
        {
            // タイマを破棄
            this.mAutoCloseTimer = null;

            // パスワードをクリア
            MainApplication.getInstance(this).clearPasswordList();

            // アプリケーションを初期表示
            Intent intent = new Intent(Kumagusu.this, Kumagusu.class);
            startActivity(intent);

            return;
        }

        // エディタ起動中クリア
        this.mExecutedChildActivityFg = false;

        // パラメータ取得
        Bundle bundle = getIntent().getExtras();

        MainApplication.getInstance(this).setCurrentMemoFolder(null);

        if (bundle != null)
        {
            if ((bundle.containsKey("VIEW_MODE")) && (bundle.getString("VIEW_MODE").equals("SEARCH")))
            {
                this.memoListViewMode = MemoListViewMode.SEARCH_VIEW;
                this.searchStrings = bundle.getString("SEARCH_STRINGS");
            }

            if ((bundle.containsKey("CURRENT_FOLDER"))
                    && (bundle.getString("CURRENT_FOLDER").startsWith(MainPreferenceActivity.getMemoLocation(this))))
            {
                MainApplication.getInstance(this).setCurrentMemoFolder(bundle.getString("CURRENT_FOLDER"));
            }
        }

        if (MainApplication.getInstance(this).getCurrentMemoFolder() == null)
        {
            MainApplication.getInstance(this).setCurrentMemoFolder(MainPreferenceActivity.getMemoLocation(this));
        }

        // ファイルリスト再生成
        refreshMemoList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        Log.d("Kumagusu", "*** START onCreateOptionsMenu()");

        // メニュー項目「新規」
        MenuItem actionItemCreate = menu.add(Menu.NONE, MENU_ID_CREATE_MEMO, Menu.NONE, R.string.ui_create);
        actionItemCreate.setIcon(R.drawable.memo_folder_add);
        ActivityCompat.setShowAsAction4ActionBar(actionItemCreate);

        // メニュー項目「リフレッシュ」
        MenuItem actionItemRefresh = menu.add(Menu.NONE, MENU_ID_REFRESH, Menu.NONE, R.string.ui_refresh);
        actionItemRefresh.setIcon(R.drawable.refresh);
        ActivityCompat.setShowAsAction4ActionBar(actionItemRefresh);

        // メニュー項目「検索」
        MenuItem actionItemSearch = menu.add(Menu.NONE, MENU_ID_SEARCH, Menu.NONE, R.string.ui_search);
        actionItemSearch.setIcon(R.drawable.search);
        ActivityCompat.setShowAsAction4ActionBar(actionItemSearch);

        //TODO 当面非表示
        actionItemSearch.setVisible(false);

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

        case MENU_ID_SEARCH: // 検索（メモ検索）
            final InputDialog searchMemoDialog = new InputDialog();
            searchMemoDialog.showDialog(Kumagusu.this,
                    this.getResources().getString(R.string.search_memo_control_dialog_title),
                    InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (searchMemoDialog.getText().length() > 0)
                            {
                                // Activetyを呼び出す
                                Intent intent = new Intent(Kumagusu.this, Kumagusu.class);
                                intent.putExtra("CURRENT_FOLDER", new File(MainApplication.getInstance(Kumagusu.this)
                                        .getCurrentMemoFolder()).getParent());
                                intent.putExtra("VIEW_MODE", "SEARCH");
                                intent.putExtra("SEARCH_STRINGS", searchMemoDialog.getText());

                                startActivity(intent);
                            }
                        }
                    }, null);
            break;

        case MENU_ID_REFRESH: // 最新の情報に更新
            refreshMemoList();
            break;

        case MENU_ID_CREATE_MEMO: // 新規

            ListDialog.showDialog(Kumagusu.this, getResources().getDrawable(R.drawable.memo_folder_add), getResources()
                    .getString(R.string.memo_list_control_dialog_title),
                    getResources().getStringArray(R.array.memo_list_control_dialog_entries), new OnClickListener()
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
                                final InputDialog addFolderDialog = new InputDialog();
                                addFolderDialog.showDialog(Kumagusu.this,
                                        Kumagusu.this.getResources()
                                                .getString(R.string.folder_add_control_dialog_title),
                                        InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                String folderName = MemoUtilities
                                                        .sanitizeFileNameString(addFolderDialog.getText());

                                                if (folderName.length() > 0)
                                                {
                                                    File addFolderFile = new File(MainApplication.getInstance(
                                                            Kumagusu.this).getCurrentMemoFolder(), folderName);

                                                    if (!addFolderFile.exists())
                                                    {
                                                        addFolderFile.mkdirs();

                                                        // メモリストを更新
                                                        refreshMemoList();
                                                    }
                                                    else
                                                    {
                                                        // すでに同名のフォルダまたはファイルが存在
                                                        ConfirmDialog
                                                                .showDialog(
                                                                        Kumagusu.this,
                                                                        getResources()
                                                                                .getDrawable(
                                                                                        android.R.drawable.ic_menu_info_details),
                                                                        getResources()
                                                                                .getString(
                                                                                        R.string.memo_list_control_dialog_add_error_duplicate),
                                                                        null, ConfirmDialog.PositiveCaptionKind.OK,
                                                                        null, null);
                                                    }
                                                }
                                                else
                                                {
                                                    // フォルダ名が空
                                                    ConfirmDialog
                                                            .showDialog(
                                                                    Kumagusu.this,
                                                                    getResources().getDrawable(
                                                                            android.R.drawable.ic_menu_info_details),
                                                                    getResources()
                                                                            .getString(
                                                                                    R.string.memo_list_control_dialog_add_error_noinput),
                                                                    null, ConfirmDialog.PositiveCaptionKind.OK, null,
                                                                    null);
                                                }
                                            }
                                        }, null);
                                break;

                            default:
                                break;

                            }
                        }
                    });

            break;

        case android.R.id.home: // UPアイコン
            // Activetyを呼び出す
            Intent intent = new Intent(Kumagusu.this, Kumagusu.class);
            intent.putExtra("CURRENT_FOLDER",
                    new File(MainApplication.getInstance(this).getCurrentMemoFolder()).getParent());

            startActivity(intent);

            break;

        default:
            break;
        }

        return ret;
    }

    /**
     * メモリスト処理を初期化する.
     */
    private void initMemoList()
    {
        this.memoBuilder = new MemoBuilder(this, MainPreferenceActivity.getEncodingName(this),
                MainPreferenceActivity.isTitleLink(this));
    }

    /**
     * ファイルリストをリフレッシュする.
     */
    private void refreshMemoList()
    {
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
        setMainTitleText("", null);

        // リスト初期化
        this.mCurrentFolderMemoFileList.clear();
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
            // タイトル表示
            setMainTitleText(null, null);
            // フォルダ内のメモを表示
            memoFolderView();
            break;

        case SEARCH_VIEW: // メモ検索表示
            // タイトル表示
            setMainTitleText(null, getResources().getString(R.string.search_memo_list_post_title_start));
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

        this.memoCreator = new MemoCreator(this, this.memoListViewMode, this.mCurrentFolderFileQueue, this.memoBuilder,
                this.mListView, this.mCurrentFolderMemoFileList);
        this.memoCreator.execute();
    }

    /**
     * メモを検索する.
     *
     * @param 検索フォルダ
     */
    private void memoSearchView(String searchFolder)
    {
        for (File f : getFileList(searchFolder))
        {

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

        listViewStatus.setMemoListViewMode(this.memoListViewMode);
        listViewStatus.setLastFolder(MainApplication.getInstance(this).getCurrentMemoFolder());
        listViewStatus.setLastTopPosition(this.mListView.getFirstVisiblePosition());
        if (this.mListView.getChildCount() > 0)
        {
            listViewStatus.setLastTopPositionY(this.mListView.getChildAt(0).getTop());
        }

        MainApplication.getInstance(this).pushMemoListStatusStack(listViewStatus);
    }

    /**
     * メモのメモ種別を変更する.
     *
     * @param srcMemoFile 変更するメモ
     * @param dstMemoType 変更先のメモ種別
     */
    private void changeMemoType(MemoFile srcMemoFile, MemoType dstMemoType)
    {
        // ファイル名のランダム化設定値により暗号化種別を変更
        if ((dstMemoType == MemoType.Secret1) && (MainPreferenceActivity.isRandamName(this)))
        {
            dstMemoType = MemoType.Secret2;
        }

        // 元ファイル読み込み
        String srcMemoData = srcMemoFile.getText();

        MemoBuilder mb = new MemoBuilder(this, MainPreferenceActivity.getEncodingName(this),
                MainPreferenceActivity.isTitleLink(this));

        if ((dstMemoType != MemoType.Text) && (MainApplication.getInstance(this).getLastCorrectPassword() == null))
        {
            final MemoFile srcMemoFileTemp = srcMemoFile;
            final MemoType dstMemoTypeTemp = dstMemoType;

            // パスワード入力（１回目）
            final InputDialog dialog = new InputDialog();
            dialog.showDialog(this, getResources().getString(R.string.ui_td_input_password), InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // OK処理
                    final String tryPassword1 = dialog.getText();

                    if (tryPassword1.length() == 0)
                    {
                        // 入力されていない
                        ConfirmDialog.showDialog(Kumagusu.this,
                                getResources().getDrawable(android.R.drawable.ic_menu_info_details), getResources()
                                        .getString(R.string.ui_td_input_password_empty), null,
                                ConfirmDialog.PositiveCaptionKind.OK, null, null);
                        return;
                    }

                    // パスワード入力（２回目）
                    dialog.showDialog(Kumagusu.this, getResources().getString(R.string.ui_td_reinput_password),
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface d, int which)
                                {
                                    // OK処理
                                    String tryPassword2 = dialog.getText();

                                    if (tryPassword1.equals(tryPassword2))
                                    {
                                        MainApplication.getInstance(Kumagusu.this).addPassword(tryPassword1);
                                        MainApplication.getInstance(Kumagusu.this).setLastCorrectPassword(tryPassword1);

                                        // 再呼出
                                        changeMemoType(srcMemoFileTemp, dstMemoTypeTemp);
                                    }
                                    else
                                    {
                                        // パスワードが一致しない
                                        ConfirmDialog.showDialog(Kumagusu.this,
                                                getResources().getDrawable(android.R.drawable.ic_menu_info_details),
                                                getResources().getString(R.string.ui_td_input_password_incorrect),
                                                null, ConfirmDialog.PositiveCaptionKind.OK, null, null);
                                        return;
                                    }
                                }
                            }, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface d, int which)
                                {
                                    // キャンセル処理
                                }
                            });

                }
            }, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // キャンセル処理
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
     * タイトルバーにタイトルを設定する.
     *
     * @param titleText タイトル文字列
     * @param postTitleText 付加タイトル文字列
     */
    private void setMainTitleText(String titleText, String postTitleText)
    {
        StringBuilder titleBuilder = new StringBuilder();

        // タイトルの指定がなければカレントフォルダを表示
        if (titleText == null)
        {
            File currentFolderFile = new File(MainApplication.getInstance(this).getCurrentMemoFolder());
            File rootFolderFile = new File(MainPreferenceActivity.getMemoLocation(this));
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

        setTitle(titleBuilder.toString());
    }
}
