package jp.gr.java_conf.kumagusu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.gr.java_conf.kumagusu.commons.Utilities;
import jp.gr.java_conf.kumagusu.compat.ActivityCompat;
import jp.gr.java_conf.kumagusu.compat.EditorCompat;
import jp.gr.java_conf.kumagusu.control.AutoLinkClickableSpan;
import jp.gr.java_conf.kumagusu.control.DialogListeners;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.control.fragment.ConfirmDialogFragment;
import jp.gr.java_conf.kumagusu.control.fragment.ConfirmDialogListenerFolder;
import jp.gr.java_conf.kumagusu.control.fragment.ListDialogFragment;
import jp.gr.java_conf.kumagusu.control.fragment.ListDialogListenerFolder;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * 編集画面.
 *
 * @author tarshi
 *
 */
@SuppressLint("NewApi")
public final class EditorActivity extends FragmentActivity implements ConfirmDialogListenerFolder,
        ListDialogListenerFolder
{
    /**
     * メモファイル（フルパス）.
     */
    private String memoFileFullPath = null;

    /**
     * メモファイル保存フォルダ.
     */
    private String currentFolderPath = null;

    /**
     * メモ編集EditText.
     */
    private EditText memoEditText = null;

    /**
     * メモファイル.
     */
    private MemoFile memoFile = null;

    /**
     * メニュー項目「検索」.
     */
    private static final int MENU_ID_SEARCH = (Menu.FIRST);

    /**
     * メニュー項目「編集」.
     */
    private static final int MENU_ID_EDIT = (Menu.FIRST + 1);

    /**
     * メニュー項目「編集終了」.
     */
    private static final int MENU_ID_EDIT_END = (Menu.FIRST + 2);

    /**
     * メニュー項目「閉じる」.
     */
    private static final int MENU_ID_CLOSE = (Menu.FIRST + 3);

    /**
     * メニュー項目「定型文」.
     */
    private static final int MENU_ID_FIXED_PHRASE = (Menu.FIRST + 4);

    /**
     * 確認ダイアログID「保存」.
     */
    private static final int DIALOG_ID_CONFIRM_SAVE = 1;

    /**
     * 確認ダイアログID「保存（キャンセルあり）」.
     */
    private static final int DIALOG_ID_CONFIRM_SAVE_WITH_CANCEL = 2;

    /**
     * 定型文選択ダイアログID.
     */
    private static final int DIALOG_ID_LIST_FIXED_PHRASE = 11;

    /**
     * 開いた時点でのメモ内容.
     */
    private String originalMemoString = "";

    /**
     * 編集可否.
     */
    private boolean editable = false;

    /**
     * 検索ワード（nullでないとき検索文字指定で起動）.
     */
    private String searchWords = null;

    /**
     * メモ変更イベント発生時の自動リンク再登録処理.
     */
    private TextWatcher memoEditTextWatcher4AutoLink = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence charsequence, int i, int j, int k)
        {
        }

        @Override
        public void beforeTextChanged(CharSequence charsequence, int i, int j, int k)
        {
        }

        @Override
        public void afterTextChanged(Editable e)
        {
            // 自動リンクを再設定
            updateSpan();
        }
    };

    /**
     * URL抽出パターン.
     */
    private static final Pattern URL_MATCH_PATTERN = Pattern.compile(
            "(http|https|rtsp):([^\\x00-\\x20()\"<>\\x7F-\\xFF])*", Pattern.CASE_INSENSITIVE);

    /**
     * メールアドレス抽出パターン.
     */
    private static final Pattern EMAIL_MATCH_PATTERN = Pattern.compile(
            "[a-z0-9\\+\\.\\_\\%\\-]{1,256}\\@[a-z0-9][a-z0-9\\-]{0,64}(\\.[a-z0-9][a-z0-9\\-]{0,25})+",
            Pattern.CASE_INSENSITIVE);

    /**
     * 電話番号抽出パターン.
     */
    private static final Pattern PHONE_MATCH_PATTERN = Pattern
            .compile("(\\+[0-9]{2}[\\- \\.]*)?(([0-9])*(\\([0-9]+\\)[\\- \\.]*)([0-9][0-9\\- \\.]{2,}[0-9])|([0-9][0-9\\- \\.]{3,}[0-9]))+");

    /**
     * エディタのLongクリック検出.
     */
    private boolean editorLongClick = false;

    /**
     * ダイアログ保管データMap.
     */
    private SparseArray<DialogListeners> dialogListenerMap = new SparseArray<DialogListeners>();

    /**
     * OK,NOのあと終了するか ?
     */
    private boolean confirmSaveDialogFinishActivity = false;

    /**
     * キャンセルを表示するときtrue.
     */
    private boolean confirmSaveDialogDispCancel = false;

    /**
     * 定型文リスト.
     */
    private String[] fixedPhraseStrings = null;

    /**
     * Kumagusuから起動.
     */
    private boolean executeByKumagusu = false;

    /**
     * Kumagusuに戻る？.
     */
    private boolean return2Kumagusu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Activity初期設定
        // ※タイトル文字を空白
        ActivityCompat.initActivity(this, R.layout.editor, R.drawable.icon, "",
                MainPreferenceActivity.isEnableEditorTitle(this), true);

        Log.d("EditorActivity", "*** START onCreate()");

        // ダイアログのリスナを生成
        initDialogListener();

        // EditTextの編集可否処理を登録
        this.memoEditText = (EditText) findViewById(R.id.editor);

        this.memoEditText.setFilters(new InputFilter[]
            {
                new InputFilter()
                {
                    public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest,
                            int dstart, int dend)
                    {
                        CharSequence inputCharSeq;

                        if (editable)
                        {
                            // 入力文字を返す
                            inputCharSeq = source;
                        }
                        else
                        {
                            // 入力文字を廃棄（read only）
                            // inputCharSeq = source.length() < 1 ?
                            // dest.subSequence(dstart, dend) : "";
                            inputCharSeq = dest.subSequence(dstart, dend);
                        }

                        return inputCharSeq;
                    }
                }
            });

        // エディタのイベントを設定
        initialyzeEditorEvent();

        // パラメータ取得
        if ((Intent.ACTION_VIEW.equals(getIntent().getAction()))
                || (Intent.ACTION_EDIT.equals(getIntent().getAction())))
        {
            // Kumagusu以外から起動
            this.executeByKumagusu = false;

            // 起動情報からパラメータ取得
            Uri uri = getIntent().getData();

            this.memoFileFullPath = uri.getPath();

            File targetFile = new File(this.memoFileFullPath);
            if ((!targetFile.exists()) || (!targetFile.isFile()))
            {
                finishEditorActivity();
            }

            this.currentFolderPath = targetFile.getParent();

            // タイムアウト？
            if (isPasswordTimeout())
            {
                // パスワードをクリア
                MainApplication.getInstance(this).clearPasswordList();
            }
        }
        else
        {
            // Kumagusuからの起動
            this.executeByKumagusu = true;

            // intentからパラメータを取得
            this.memoFileFullPath = getIntent().getStringExtra("FULL_PATH");
            this.currentFolderPath = getIntent().getStringExtra("CURRENT_FOLDER");

            this.searchWords = getIntent().getStringExtra("SEARCH_WORDS");
        }

        // 検索ツールクローズイベント登録
        ImageButton searchCloseButton = (ImageButton) findViewById(R.id.edit_search_close);
        searchCloseButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displaySearchView(false, null);
            }
        });

        // 検索ボタンイベント登録
        ImageButton nextSearchButton = (ImageButton) findViewById(R.id.edit_next_search);
        nextSearchButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchWord(true);
            }
        });

        ImageButton prevSearchButton = (ImageButton) findViewById(R.id.edit_prev_search);
        prevSearchButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchWord(false);
            }
        });

        // メモを表示
        MemoBuilder builder = new MemoBuilder(this, MainPreferenceActivity.getEncodingName(this),
                MainPreferenceActivity.isTitleLink(this));

        try
        {
            if (this.memoFileFullPath != null)
            {
                // 編集
                this.memoFile = (MemoFile) builder.buildFromFile(this.memoFileFullPath);

                // 表示モード
                setEditable(false);
            }
            else
            {
                // 新規
                MemoType createMemoType;

                if (MainPreferenceActivity.isEnctyptNewMemo(this))
                {
                    if (MainPreferenceActivity.isRandamName(this))
                    {
                        createMemoType = MemoType.Secret2;
                    }
                    else
                    {
                        createMemoType = MemoType.Secret1;
                    }
                }
                else
                {
                    createMemoType = MemoType.Text;
                }

                this.memoFile = (MemoFile) builder.build(this.currentFolderPath, createMemoType);

                // 編集モード
                setEditable(true);
            }
        }
        catch (FileNotFoundException ex)
        {
            // 無視
            Log.w("EditorActivity", "Memo file not found", ex);
        }

        // メモデータがあれば表示する
        // なければ（復号できなければ）リストに戻る
        setMemoData();
    }

    @Override
    protected void onResume()
    {
        Log.d("EditorActivity", "*** START onResume()");

        super.onResume();

        // タイムアウト？
        if (isPasswordTimeout())
        {
            // エディタ終了
            finishEditorActivity();

            return;
        }
    }

    @Override
    protected void onPause()
    {
        Log.d("EditorActivity", "*** START onPause()");

        super.onPause();

        // タイマ開始
        if (!this.return2Kumagusu)
        {
            MainApplication.getInstance(this).getPasswordTimer().start();
        }
    }

    @Override
    protected void onDestroy()
    {
        Log.d("EditorActivity", "*** START onDestroy()");

        super.onDestroy();

        // 編集終了
        if (isEditable())
        {
            setEditable(false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d("EditorActivity", "*** START onSaveInstanceState()");

        super.onSaveInstanceState(outState);

        // 編集フラグを保存
        outState.putBoolean("editable", isEditable());

        // 保存後終了フラグ保存
        outState.putBoolean("confirmSaveDialogFinishActivity", this.confirmSaveDialogFinishActivity);

        // 検索ビュー表示状態保存
        outState.putBoolean("visibilitySearchView", isVisibilitySearchView());

        // 定型文保存
        if (this.fixedPhraseStrings != null)
        {
            outState.putStringArray("fixedPhraseStrings", this.fixedPhraseStrings);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.d("EditorActivity", "*** START onRestoreInstanceState()");

        // 編集フラグをリストア
        if (savedInstanceState.containsKey("editable"))
        {
            setEditable(savedInstanceState.getBoolean("editable"));
        }

        // 保存後終了フラグをリストア
        if (savedInstanceState.containsKey("confirmSaveDialogFinishActivity"))
        {
            this.confirmSaveDialogFinishActivity = savedInstanceState.getBoolean("confirmSaveDialogFinishActivity");
        }

        // 検索ビュー表示状態リストア
        if (savedInstanceState.containsKey("visibilitySearchView"))
        {
            setVisibilitySearchView(savedInstanceState.getBoolean("visibilitySearchView"));
        }

        // 定型文をリストア
        if (savedInstanceState.containsKey("fixedPhraseStrings"))
        {
            this.fixedPhraseStrings = savedInstanceState.getStringArray("fixedPhraseStrings");
        }

        // onRestoreInstanceStateで、EditText（ほか）をリストア
        boolean editableTemp = this.editable;
        try
        {
            setEditable(true, true);
            super.onRestoreInstanceState(savedInstanceState);
        }
        finally
        {
            setEditable(editableTemp, true);
        }
    }

    /**
     * 戻るキーをフックする. ※Android2.0未満にも対応するためonBackPressedを使わない
     *
     * @param event イベント
     * @return ここで処理を終了するときtrue
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        Log.d("EditorActivity", "*** START dispatchKeyEvent()");

        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN))
        {
            // エディタ終了
            close(false);

            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        Log.d("EditorActivity", "*** START onCreateOptionsMenu()");

        // メニュー項目「検索」
        MenuItem actionItemSeaerch = menu.add(Menu.NONE, MENU_ID_SEARCH, Menu.NONE, R.string.ui_search);
        actionItemSeaerch.setIcon(R.drawable.search);
        ActivityCompat.setShowAsAction4ActionBar(actionItemSeaerch);

        // メニュー項目「編集」
        MenuItem actionItemEdit = menu.add(Menu.NONE, MENU_ID_EDIT, Menu.NONE, R.string.ui_edit_start);
        actionItemEdit.setIcon(R.drawable.memo_compose);
        ActivityCompat.setShowAsAction4ActionBar(actionItemEdit);

        // メニュー項目「編集終了」（「編集」と編集フラグにより切り替え）
        MenuItem actionItemEditEnd = menu.add(Menu.NONE, MENU_ID_EDIT_END, Menu.NONE, R.string.ui_edit_end);
        actionItemEditEnd.setIcon(R.drawable.save);
        ActivityCompat.setShowAsAction4ActionBar(actionItemEditEnd);

        // メニュー項目「定型文」
        MenuItem actionItemFixedPhrese = menu.add(Menu.NONE, MENU_ID_FIXED_PHRASE, Menu.NONE,
                R.string.fixed_phrase_dialog_title);
        actionItemFixedPhrese.setIcon(R.drawable.fixed_phrase);
        ActivityCompat.setShowAsAction4ActionBar(actionItemFixedPhrese);

        // 3.0以下
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        {
            // メニュー項目「閉じる」
            MenuItem actionItemClose = menu.add(Menu.NONE, MENU_ID_CLOSE, Menu.NONE, R.string.ui_close);
            actionItemClose.setIcon(R.drawable.close);
            ActivityCompat.setShowAsAction4ActionBar(actionItemClose);
        }

        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // 編集中フラグによりメニューの項目を切り替え
        menu.findItem(MENU_ID_EDIT).setVisible(!editable);
        menu.findItem(MENU_ID_EDIT_END).setVisible(editable);
        menu.findItem(MENU_ID_FIXED_PHRASE).setEnabled(editable);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean ret = super.onOptionsItemSelected(item);

        Log.d("EditorActivity", "*** START onOptionsItemSelected()");

        switch (item.getItemId())
        {
        case MENU_ID_SEARCH: // 検索
            displaySearchView(true, null);
            break;

        case MENU_ID_EDIT: // 編集
            setEditable(true);
            break;

        case MENU_ID_EDIT_END: // 編集終了
            // 保存確認（OK or キャンセル）
            boolean modified = saveMemoData(false, true);

            // 変更がないならそのまま編集終了
            if (!modified)
            {
                setEditable(false);
            }
            break;

        case MENU_ID_CLOSE: // 閉じる
            // エディタ終了
            close(true);
            break;

        case MENU_ID_FIXED_PHRASE: // 定型文
            this.fixedPhraseStrings = MainPreferenceActivity.getFixedPhraseStrings(this).toArray(new String[0]);
            Date nowDate = new Date();

            for (int i = 0; i < this.fixedPhraseStrings.length; i++)
            {
                this.fixedPhraseStrings[i] = Utilities.getDateTimeFormattedString(this, fixedPhraseStrings[i], nowDate);
            }

            ListDialogFragment.newInstance(DIALOG_ID_LIST_FIXED_PHRASE, R.drawable.fixed_phrase,
                    R.string.fixed_phrase_dialog_title, 0, this.fixedPhraseStrings).show(getSupportFragmentManager(),
                    "");
            break;

        case android.R.id.home: // UPアイコン
            // エディタ終了
            close(true);
            break;

        default:
            break;
        }

        return ret;
    }

    /**
     * パスワード保持がタイムアウトしているかを調べる.
     *
     * @return タイムアウトしていればtrue
     */
    private boolean isPasswordTimeout()
    {
        // タイムアウト発生?
        if (MainApplication.getInstance(this).getPasswordTimer().stop())
        {
            // パスワードをクリア
            MainApplication.getInstance(this).clearPasswordList();

            return true;
        }

        return false;
    }

    /**
     * エディタを閉じる.
     *
     * @param dispCancel キャンセルボタン表示（キャンセルクリック時、変更なし扱い）
     */
    private void close(boolean dispCancel)
    {
        boolean modify = false;

        if (isEditable())
        {
            modify = saveMemoData(true, dispCancel);
        }

        if (!modify)
        {
            setEditable(false);

            // エディタ終了
            finishEditorActivity();
        }
    }

    /**
     * 文字列を検索し、検索された文字列を選択する.
     *
     * @param nextFg trueのとき後方検索、falseのとき前方検索
     */
    private void searchWord(boolean nextFg)
    {
        EditText searchWordEditText = (EditText) findViewById(R.id.edit_search_word);
        String searchWord = searchWordEditText.getText().toString();

        // IMEを消去
        EditorCompat.setImeVisibility(this, getWindow(), false, searchWordEditText);

        // フォーカスをエディタに移す
        this.memoEditText.requestFocus();

        // 検索および文字列を選択
        if (searchWord.length() > 0)
        {
            String editingText = this.memoEditText.getText().toString();

            int searchStartIndex;
            if (nextFg)
            {
                searchStartIndex = this.memoEditText.getSelectionEnd();
            }
            else
            {
                searchStartIndex = this.memoEditText.getSelectionStart() - 1;
            }

            if (searchStartIndex < 0)
            {
                searchStartIndex = 0;
            }
            else if (searchStartIndex >= editingText.length())
            {
                searchStartIndex = editingText.length() - 1;
            }

            int searchIndex;
            if (nextFg)
            {
                searchIndex = editingText.toLowerCase().indexOf(searchWord.toLowerCase(), searchStartIndex);
            }
            else
            {
                searchIndex = editingText.toLowerCase().lastIndexOf(searchWord.toLowerCase(), searchStartIndex);
            }

            if (searchIndex >= 0)
            {
                this.memoEditText.setSelection(searchIndex, searchIndex + searchWord.length());
            }
        }
    }

    /**
     * 検索処理を開始する.
     *
     * @param visible 表示するときtrue
     * @param srchWords 検索文字(設定しないときnull)
     */
    private void displaySearchView(boolean visible, String srchWords)
    {
        setVisibilitySearchView(visible);

        if (visible)
        {
            EditText searchWordEditText = (EditText) findViewById(R.id.edit_search_word);
            searchWordEditText.requestFocus();

            if (srchWords != null)
            {
                searchWordEditText.setText(srchWords);
                searchWord(true);
            }
        }
    }

    /**
     * 検索ビューが表示中か?
     *
     * @return 表示中ならtrue
     */
    private boolean isVisibilitySearchView()
    {
        LinearLayout editSearchToolLayout = (LinearLayout) findViewById(R.id.edit_search_tool);

        return (editSearchToolLayout.getVisibility() == View.VISIBLE);
    }

    /**
     * 検索ビューの表示状態を変更する.
     *
     * @param visible 表示するときtrue
     */
    private void setVisibilitySearchView(boolean visible)
    {
        int visibility = (visible) ? View.VISIBLE : View.GONE;

        LinearLayout editSearchToolLayout = (LinearLayout) findViewById(R.id.edit_search_tool);

        editSearchToolLayout.setVisibility(visibility);
    }

    /**
     * メモデータが変更されていれば、ファイルに保存する.
     *
     * @param finishActivity OK,NOの後終了するか?
     * @param dispCancel キャンセルを表示するときtrue
     * @return メモデータに変更があるときtrue
     */
    private boolean saveMemoData(boolean finishActivity, final boolean dispCancel)
    {
        // 保存するか確認し、OKであればファイルに保存
        if (isModifiedMemo())
        {
            showConfirmSaveDialog(finishActivity, dispCancel);

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * メモデータをエディタに設定する.
     */
    private void setMemoData()
    {
        String title = this.memoFile.getTitle();
        String memoData = this.memoFile.getText();

        boolean openedFg = true;

        if ((title == null) || (memoData == null))
        {
            openedFg = false;

            title = "";
            memoData = "";
        }

        // タイトルと本文を設定
        setTitle(title);

        this.originalMemoString = memoData.replaceAll("\r", "");
        boolean editableTemp = this.editable;
        try
        {
            setEditable(true, true);
            this.memoEditText.setText(this.originalMemoString);

            // カーソルを先頭に移動
            this.memoEditText.setSelection(0, this.memoEditText.getText().length());
            this.memoEditText.setSelection(0);

        }
        finally
        {
            setEditable(editableTemp, true);
        }

        // 自動リンクを再設定
        updateSpan();

        // IMEを消去
        if (!isEditable())
        {
            getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        // ファイルが開けない場合、暗号化ファイルであればパスワードを再入力
        // プレーンテキストならエディタ終了
        if (!openedFg)
        {
            if ((this.memoFile.getMemoType() == MemoType.Secret1) || (this.memoFile.getMemoType() == MemoType.Secret2))
            {
                // パスワードを入力
                final InputDialog dialog = new InputDialog(this);
                dialog.showDialog(getResources().getString(R.string.ui_td_input_password), InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD, new DialogInterface.OnClickListener()
                {
                    /**
                     * OKを処理する.
                     */
                    @Override
                    public void onClick(DialogInterface d, int which)
                    {
                        String tryPassword = dialog.getText();

                        MainApplication.getInstance(EditorActivity.this).addPassword(tryPassword);

                        setMemoData();
                    }
                }, new DialogInterface.OnClickListener()
                {
                    /**
                     * キャンセルを処理する.
                     */
                    @Override
                    public void onClick(DialogInterface d, int which)
                    {
                        // エディタ終了
                        finishEditorActivity();
                    }
                });
            }
            else
            {
                // エディタ終了
                finishEditorActivity();
            }
        }
        else
        {
            // 検索ワードが指定されていれば検索処理を開始
            if (this.searchWords != null)
            {
                displaySearchView(true, this.searchWords);

                this.searchWords = null; // 検索ワード指定を解除
            }
        }
    }

    /**
     * 編集中かを返す.
     *
     * @return 編集中のときtrue
     */
    private boolean isEditable()
    {
        return editable;
    }

    /**
     * 編集中を設定する.
     *
     * @param edtbl 編集中のときtrue
     */
    private void setEditable(boolean edtbl)
    {
        setEditable(edtbl, false);
    }

    /**
     * 編集中を設定する.
     *
     * @param edtbl 編集中のときtrue
     * @param forceWrite 強制書き込み時
     */
    private void setEditable(boolean edtbl, boolean forceWrite)
    {
        this.editable = edtbl;

        if (!forceWrite)
        {
            // InputTypeを設定
            EditorCompat.setEditorInputType(this.memoEditText, this.editable);

            // IME制御
            EditorCompat.setImeVisibility(this, getWindow(), edtbl, this.memoEditText);
        }

        // オプションメニューを再表示
        ActivityCompat.refreshMenu4ActionBar(this);

        // 自動リンク再生成
        updateSpan();

        if (!forceWrite)
        {
            // 編集中はエディタの枠線を赤に設定
            if (this.editable)
            {
                this.memoEditText.setBackgroundDrawable(getResources().getDrawable(R.drawable.editable_border_true));
            }
            else
            {
                this.memoEditText.setBackgroundDrawable(getResources().getDrawable(R.drawable.editable_border_false));
            }
        }
    }

    /**
     * メモデータが変更された?
     *
     * @return 変更されたときtrue
     */
    private boolean isModifiedMemo()
    {
        String currentMemoString = this.memoEditText.getText().toString();

        return (!currentMemoString.equals(this.originalMemoString));
    }

    /**
     * エディタのイベントを設定する.
     */
    private void initialyzeEditorEvent()
    {
        // クリック
        this.memoEditText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!isEditable())
                {
                    // IME非表示
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    EditorCompat.setImeVisibility(EditorActivity.this, EditorActivity.this.getWindow(), false,
                            EditorActivity.this.memoEditText);
                }
            }
        });

        // Longクリック
        this.memoEditText.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if ((!isEditable()) && (MainPreferenceActivity.isEnableAutoLink(EditorActivity.this)))
                {
                    EditorActivity.this.editorLongClick = true;
                }

                return false;
            }
        });

        // タッチ
        this.memoEditText.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (!isEditable())
                {
                    // 自動リンク
                    if (MainPreferenceActivity.isEnableAutoLink(EditorActivity.this))
                    {
                        int action = event.getAction();

                        Log.d("EditorActivity", new StringBuilder("action:").append(action).toString());

                        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN)
                        {
                            int x = (int) event.getX();
                            int y = (int) event.getY();
                            x -= ((EditText) v).getTotalPaddingLeft();
                            y -= ((EditText) v).getTotalPaddingTop();
                            x += v.getScrollX();
                            y += v.getScrollY();

                            Layout layout = ((EditText) v).getLayout();
                            int line = layout.getLineForVertical(y);
                            int off = layout.getOffsetForHorizontal(line, x);
                            Spannable buffer = ((EditText) v).getText();

                            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                            if (link.length != 0)
                            {
                                Log.d("EditorActivity",
                                        new StringBuilder("start:").append(buffer.getSpanStart(link[0]))
                                                .append(" end:").append(buffer.getSpanEnd(link[0]))
                                                .append(" LongClick:").append(EditorActivity.this.editorLongClick)
                                                .toString());

                                if ((action == MotionEvent.ACTION_UP) && (!EditorActivity.this.editorLongClick))
                                {
                                    Log.d("EditorActivity", "onClick!");

                                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]),
                                            buffer.getSpanEnd(link[0]));

                                    link[0].onClick(v);
                                }
                                else if (action == MotionEvent.ACTION_DOWN)
                                {
                                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]),
                                            buffer.getSpanEnd(link[0]));
                                }
                            }

                            EditorActivity.this.editorLongClick = false;
                        }
                    }

                    // IME非表示
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    EditorCompat.setImeVisibility(EditorActivity.this, EditorActivity.this.getWindow(), false,
                            EditorActivity.this.memoEditText);
                }

                return false;
            }
        });
    }

    /**
     * 自動リンクを設定する.
     */
    private void updateSpan()
    {
        CharSequence text = this.memoEditText.getText();
        Spannable span = (Spannable) text;

        // 自動リンクをクリア
        AutoLinkClickableSpan[] clickableLink = span.getSpans(0, text.length(), AutoLinkClickableSpan.class);

        for (AutoLinkClickableSpan autoLinkClickableSpan : clickableLink)
        {
            span.removeSpan(autoLinkClickableSpan);
        }

        this.memoEditText.removeTextChangedListener(this.memoEditTextWatcher4AutoLink);

        // 自動リンクを設定
        if ((!isEditable()) && (MainPreferenceActivity.isEnableAutoLink(EditorActivity.this)))
        {
            // URL
            Matcher urlMatcher = URL_MATCH_PATTERN.matcher(span);

            while (urlMatcher.find())
            {
                span.setSpan(new AutoLinkClickableSpan(URL_MATCH_PATTERN,
                        new AutoLinkClickableSpan.AutoLinkOnClickListener()
                        {
                            @Override
                            public void onClick(String matchString)
                            {
                                Uri uri = Uri.parse(matchString);
                                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                EditorActivity.this.startActivity(i);
                            }
                        }), urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // メールアドレス
            Matcher emailMatcher = EMAIL_MATCH_PATTERN.matcher(span);

            while (emailMatcher.find())
            {
                span.setSpan(new AutoLinkClickableSpan(EMAIL_MATCH_PATTERN,
                        new AutoLinkClickableSpan.AutoLinkOnClickListener()
                        {
                            @Override
                            public void onClick(String matchString)
                            {
                                Uri uri = Uri.parse("mailto:" + matchString);
                                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                EditorActivity.this.startActivity(i);
                            }
                        }), emailMatcher.start(), emailMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // 電話番号
            Matcher phoneMatcher = PHONE_MATCH_PATTERN.matcher(span);

            while (phoneMatcher.find())
            {
                span.setSpan(new AutoLinkClickableSpan(PHONE_MATCH_PATTERN,
                        new AutoLinkClickableSpan.AutoLinkOnClickListener()
                        {
                            @Override
                            public void onClick(String matchString)
                            {
                                Uri uri = Uri.parse("tel:" + matchString);
                                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                EditorActivity.this.startActivity(i);
                            }
                        }), phoneMatcher.start(), phoneMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // テキスト変更イベントで自動リンク再登録
            this.memoEditText.addTextChangedListener(this.memoEditTextWatcher4AutoLink);
        }
    }

    /**
     * 確認ダイアログ「保存」を表示する.
     *
     * @param finishActivity OK,NOの後終了するか?
     * @param dispCancel キャンセルを表示するときtrue
     */
    private void showConfirmSaveDialog(boolean finishActivity, boolean dispCancel)
    {
        this.confirmSaveDialogFinishActivity = finishActivity;
        this.confirmSaveDialogDispCancel = dispCancel;

        int dialogId;

        if (dispCancel)
        {
            dialogId = DIALOG_ID_CONFIRM_SAVE_WITH_CANCEL;
        }
        else
        {
            dialogId = DIALOG_ID_CONFIRM_SAVE;
        }

        ConfirmDialogFragment.newInstance(dialogId, 0, R.string.memo_edit_dialog_confiem_save, 0,
                ConfirmDialogFragment.POSITIVE_CAPTION_KIND_YES).show(getSupportFragmentManager(), "");
    }

    /**
     * ダイアログのリスナを初期化する.
     */
    private void initDialogListener()
    {
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener()
        {
            /**
             * Okを処理する.
             */
            @Override
            public void onClick(DialogInterface d, int which)
            {
                final String memoData = EditorActivity.this.memoEditText.getText().toString();

                // 改行コードをDOS形式に
                final String saveMemoData = memoData.replaceAll("\n", "\r\n");

                if ((EditorActivity.this.memoFile.getMemoType() != MemoType.Text)
                        && (MainApplication.getInstance(EditorActivity.this).getLastCorrectPassword() == null))
                {
                    // パスワードを入力し、暗号化ファイル保存
                    Utilities.inputPassword(EditorActivity.this, new DialogInterface.OnClickListener()
                    {
                        /**
                         * 入力パスワードが有効時を処理する.
                         */
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // 編集元テキストを更新
                            EditorActivity.this.originalMemoString = memoData;

                            // メモを保存
                            EditorActivity.this.memoFile.setText(MainApplication.getInstance(EditorActivity.this)
                                    .getLastCorrectPassword(), saveMemoData);

                            // タイトルを設定
                            setTitle(EditorActivity.this.memoFile.getTitle());

                            // メモ変更を設定
                            MainApplication.getInstance(EditorActivity.this).setUpdateMemo(true);

                            // OK、No後処理
                            confirmSaveDialogPostOkNo();
                        }
                    }, new DialogInterface.OnClickListener()
                    {
                        /**
                         * パスワード入力キャンセル時を処理する.
                         */
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // キャンセル時、再度保存処理（Yes/Noを再度聞くため）
                            saveMemoData(EditorActivity.this.confirmSaveDialogFinishActivity,
                                    EditorActivity.this.confirmSaveDialogDispCancel);
                        }
                    });
                }
                else
                {
                    // 編集元テキストを更新
                    EditorActivity.this.originalMemoString = memoData;

                    // メモを保存
                    EditorActivity.this.memoFile.setText(MainApplication.getInstance(EditorActivity.this)
                            .getLastCorrectPassword(), saveMemoData);

                    // タイトルを設定
                    setTitle(EditorActivity.this.memoFile.getTitle());

                    // メモ変更を設定
                    MainApplication.getInstance(EditorActivity.this).setUpdateMemo(true);

                    // OK、No後処理
                    confirmSaveDialogPostOkNo();
                }
            }
        };

        DialogInterface.OnClickListener noListener = new DialogInterface.OnClickListener()
        {
            /**
             * Noを処理する.
             */
            @Override
            public void onClick(DialogInterface d, int which)
            {
                // メモデータを再設定
                setMemoData();

                // OK、No後処理
                confirmSaveDialogPostOkNo();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
        {
            /**
             * キャンセルを処理する.
             */
            @Override
            public void onClick(DialogInterface d, int which)
            {
                // キャンセル処理なし
            }
        };

        // 確認ダイアログ「キャンセルボタンあり」
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_SAVE_WITH_CANCEL, new DialogListeners(okListener, noListener,
                cancelListener));

        // 確認ダイアログ「キャンセルボタンなし」
        putConfirmDialogListeners(DIALOG_ID_CONFIRM_SAVE, new DialogListeners(okListener, noListener, null));

        // リストダイアログ「定型文選択」
        putListDialogListeners(DIALOG_ID_LIST_FIXED_PHRASE, new DialogListeners(new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String insertString = EditorActivity.this.fixedPhraseStrings[which];

                int cStart = EditorActivity.this.memoEditText.getSelectionStart();
                int cEnd = EditorActivity.this.memoEditText.getSelectionEnd();
                Editable memoEditable = EditorActivity.this.memoEditText.getText();

                memoEditable.replace(Math.min(cStart, cEnd), Math.max(cStart, cEnd), insertString);
            }
        }));
    }

    /**
     * 確認ダイアログ「保存」で、OkまたはNoボタンを押した後処理を実行する.
     */
    private void confirmSaveDialogPostOkNo()
    {
        setEditable(false);

        // 終了
        if (EditorActivity.this.confirmSaveDialogFinishActivity)
        {
            EditorActivity.this.confirmSaveDialogFinishActivity = false;
            finishEditorActivity();
        }
    }

    /**
     * エディタを終了する.
     */
    private void finishEditorActivity()
    {
        // Kumagusuに戻る？
        this.return2Kumagusu = this.executeByKumagusu;

        // アクティビティー終了
        finish();
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
