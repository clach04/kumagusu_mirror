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
import jp.gr.java_conf.kumagusu.control.ConfirmDialog;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.control.ListDialog;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
public final class EditorActivity extends Activity
{
    /**
     * メモ編集EditText.
     */
    private EditText memoEditText = null;

    /**
     * メモファイル.
     */
    private MemoFile memoFile = null;

    /**
     * 自動クローズ処理タイマー.
     */
    private Timer autoCloseTimer;

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
     * 編集モード.
     */
    private EditMode editMode;

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
     * 編集モード.
     *
     * @author tarshi
     *
     */
    private enum EditMode
    {
        /**
         * 新規.
         */
        Create,

        /**
         * 既存ファイル編集.
         */
        Edit,
    }

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

    private boolean editorLongClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Activity初期設定
        // ※タイトル文字を空白
        ActivityCompat.initActivity(this, R.layout.editor, R.drawable.icon, "",
                MainPreferenceActivity.isEnableEditorTitle(this), true);

        Log.d("EditorActivity", "*** START onCreate()");

        // EditTextの編集可否処理を登録
        this.memoEditText = (EditText) findViewById(R.id.editor);

        this.memoEditText.setFilters(new InputFilter[]
            {
                new InputFilter()
                {
                    public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest,
                            int dstart, int dend)
                    {
                        if (editable)
                        {
                            // 入力文字を返す
                            return source;
                        }
                        else
                        {
                            // 入力文字を廃棄（read only）
                            return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
                        }
                    }
                }
            });

        // IMEの表示非表示処理
        initialyzeEditorImeVisibility();

        // 自動クローズタイマ処理生成
        this.autoCloseTimer = new Timer(this);

        // パラメータ取得
        String fullPath = null;
        String currentFolderPath = null;

        if ((Intent.ACTION_VIEW.equals(getIntent().getAction()))
                || (Intent.ACTION_EDIT.equals(getIntent().getAction())))
        {
            // 起動情報からパラメータ取得
            Uri uri = getIntent().getData();

            fullPath = uri.getPath();

            File targetFile = new File(fullPath);
            if ((!targetFile.exists()) || (!targetFile.isFile()))
            {
                finish();
            }

            currentFolderPath = targetFile.getParent();
        }
        else
        {
            // intentからパラメータを取得
            fullPath = getIntent().getStringExtra("FULL_PATH");
            currentFolderPath = getIntent().getStringExtra("CURRENT_FOLDER");

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
            if (fullPath != null)
            {
                // 編集
                this.editMode = EditMode.Edit;

                this.memoFile = (MemoFile) builder.buildFromFile(fullPath);

                // 表示モード
                setEditable(false);
            }
            else
            {
                // 新規
                this.editMode = EditMode.Create;

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

                this.memoFile = (MemoFile) builder.build(currentFolderPath, createMemoType);

                // 編集モード
                setEditable(true);
            }
        }
        catch (FileNotFoundException ex)
        {
            // 無視
            Log.e("EditorActivity", "*** START onCreate()", ex);
            return;
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
        Utilities.setImeVisibility(this, false, searchWordEditText);

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

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d("EditorActivity", "*** START onPause()");

        // 編集終了
        if (isEditable())
        {
            setEditable(false);
        }

        // タイマ開始
        if (this.autoCloseTimer != null)
        {
            this.autoCloseTimer.start();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.d("EditorActivity", "*** START onResume()");

        // アプリケーションが非実行中のとき、即終了
        if (this.autoCloseTimer == null)
        {
            // リストに戻る
            Intent intent = new Intent(EditorActivity.this, Kumagusu.class);
            intent.putExtra("CURRENT_FOLDER", memoFile.getParent());

            return;
        }

        // タイムアウト？
        if (this.autoCloseTimer.stop())
        {
            // パスワードをクリア
            MainApplication.getInstance(this).clearPasswordList();

            // エディタ終了
            finish();

            return;
        }

        // メモデータがあれば表示する
        // なければ（復号できなければ）リストに戻る
        setMemoData();
    }

    /**
     * 戻るキーをフックする. ※Android2.0未満にも対応するばてonBackPressedを使わない
     *
     * @param event イベント
     * @return ここで処理を終了するときtrue
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
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
            boolean modified = saveMemoData(new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // 保存OKの場合編集終了
                    setEditable(false);

                    if (EditorActivity.this.editMode == EditMode.Create)
                    {
                        // 新規モードなら終了
                        finish();
                    }
                }
            }, true);

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
            final String[] fixedPhraseStrings = MainPreferenceActivity.getFixedPhraseStrings(this).toArray(
                    new String[0]);
            Date nowDate = new Date();

            for (int i = 0; i < fixedPhraseStrings.length; i++)
            {
                fixedPhraseStrings[i] = Utilities.getDateTimeFormattedString(this, fixedPhraseStrings[i], nowDate);
            }

            ListDialog fixedPhraseSelectDialog = new ListDialog(this);
            fixedPhraseSelectDialog.showDialog(getResources().getDrawable(R.drawable.fixed_phrase), getResources()
                    .getString(R.string.fixed_phrase_dialog_title), fixedPhraseStrings,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String insertString = fixedPhraseStrings[which];

                            int cStart = EditorActivity.this.memoEditText.getSelectionStart();
                            int cEnd = EditorActivity.this.memoEditText.getSelectionEnd();
                            Editable memoEditable = EditorActivity.this.memoEditText.getText();

                            memoEditable.replace(Math.min(cStart, cEnd), Math.max(cStart, cEnd), insertString);
                        }
                    });
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
     * エディタを閉じる.
     *
     * @param dispCancel キャンセルボタン表示（キャンセルクリック時、変更なし扱い）
     */
    private void close(boolean dispCancel)
    {
        boolean modify = false;

        if (isEditable())
        {
            modify = saveMemoData(new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // YesおよびNoのときエディタ終了
                    setEditable(false);

                    // エディタ終了
                    finish();
                }
            }, dispCancel);
        }

        if (!modify)
        {
            // Kumagusuを表示
            setEditable(false);
            finish();
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
        LinearLayout editSearchToolLayout = (LinearLayout) findViewById(R.id.edit_search_tool);

        if (visible)
        {
            editSearchToolLayout.setVisibility(View.VISIBLE);

            EditText searchWordEditText = (EditText) findViewById(R.id.edit_search_word);
            searchWordEditText.requestFocus();

            if (srchWords != null)
            {
                searchWordEditText.setText(srchWords);
                searchWord(true);
            }
        }
        else
        {
            editSearchToolLayout.setVisibility(View.GONE);
        }
    }

    /**
     * メモデータが変更されていれば、ファイルに保存する.
     *
     * @param postOkNoListener OK,NOの後処理のリスナ
     * @param dispCancel キャンセルを表示するときtrue
     * @return メモデータに変更があるときtrue
     */
    private boolean saveMemoData(final DialogInterface.OnClickListener postOkNoListener, final boolean dispCancel)
    {
        // 保存するか確認し、OKであればファイルに保存
        if (isModifiedMemo())
        {
            DialogInterface.OnClickListener cancelListener = null;
            if (dispCancel)
            {
                cancelListener = new DialogInterface.OnClickListener()
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
            }

            ConfirmDialog.showDialog(this, null, getResources().getString(R.string.memo_edit_dialog_confiem_save),
                    null, ConfirmDialog.PositiveCaptionKind.YES, new DialogInterface.OnClickListener()
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
                                        EditorActivity.this.memoFile.setText(
                                                MainApplication.getInstance(EditorActivity.this)
                                                        .getLastCorrectPassword(), saveMemoData);

                                        // タイトルを設定
                                        setTitle(EditorActivity.this.memoFile.getTitle());

                                        if (postOkNoListener != null)
                                        {
                                            postOkNoListener.onClick(d, which);
                                        }
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
                                        saveMemoData(postOkNoListener, dispCancel);
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

                                if (postOkNoListener != null)
                                {
                                    postOkNoListener.onClick(d, which);
                                }
                            }
                        }
                    }, new DialogInterface.OnClickListener()
                    {
                        /**
                         * Noを処理する.
                         */
                        @Override
                        public void onClick(DialogInterface d, int which)
                        {
                            // メモデータを再設定
                            setMemoData();

                            if (postOkNoListener != null)
                            {
                                postOkNoListener.onClick(d, which);
                            }
                        }
                    }, cancelListener);

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
            this.editable = true;
            this.memoEditText.setText(this.originalMemoString);

            // カーソルを先頭に移動
            this.memoEditText.setSelection(0, this.memoEditText.getText().length());
            this.memoEditText.setSelection(0);

        }
        finally
        {
            this.editable = editableTemp;
        }

        // 自動リンクを再設定
        updateSpan();

        // IMEを消去
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
                        // タイマを破棄
                        EditorActivity.this.autoCloseTimer = null;

                        // エディタ終了
                        finish();
                    }
                });
            }
            else
            {
                // タイマを破棄
                this.autoCloseTimer = null;

                // エディタ終了
                finish();
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
    public boolean isEditable()
    {
        return editable;
    }

    /**
     * 編集中を設定する.
     *
     * @param ed 編集中のときtrue
     */
    private void setEditable(boolean ed)
    {
        this.editable = ed;

        // InputTypeを設定
        EditorCompat.setEditorInputType(this.memoEditText, this.editable);

        // IME制御
        Utilities.setImeVisibility(this, ed, this.memoEditText);

        // オプションメニューを再表示
        ActivityCompat.refreshMenu4ActionBar(this);

        // 自動リンク再生成
        updateSpan();
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
     * 入力部品のIMEの有効無効を設定する.
     */
    public void initialyzeEditorImeVisibility()
    {
        this.memoEditText.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (!isEditable())
                {
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    Utilities.setImeVisibility(EditorActivity.this, false, EditorActivity.this.memoEditText);
                }
            }
        });

        this.memoEditText.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (!isEditable())
                {
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    Utilities.setImeVisibility(EditorActivity.this, false, EditorActivity.this.memoEditText);
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
        this.memoEditText.setOnTouchListener(null);

        AutoLinkClickableSpan[] clickableLink = span.getSpans(0, text.length(), AutoLinkClickableSpan.class);

        for (AutoLinkClickableSpan autoLinkClickableSpan : clickableLink)
        {
            span.removeSpan(autoLinkClickableSpan);
        }

        this.memoEditText.removeTextChangedListener(this.memoEditTextWatcher4AutoLink);

        // 自動リンクを設定
        if ((!isEditable()) && (MainPreferenceActivity.isEnableAutoLink(EditorActivity.this)))
        {
            this.memoEditText.setOnTouchListener(new OnTouchListener()
            {
                // @Override
                public boolean onTouch(View v, MotionEvent event)
                {

                    if (true)
                    {
                        int action = event.getAction();
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
                                if ((action == MotionEvent.ACTION_UP) && (!EditorActivity.this.editorLongClick))
                                {
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

                    return false;
                }
            });

            this.memoEditText.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    EditorActivity.this.editorLongClick = true;

                    return false;
                }
            });

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
}
