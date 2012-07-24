package jp.gr.java_conf.kumagusu;

import java.io.FileNotFoundException;

import jp.gr.java_conf.kumagusu.compat.ActivityCompat;
import jp.gr.java_conf.kumagusu.control.ConfirmDialog;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
     * 開いた時点でのメモ内容.
     */
    private String originalMemoString = "";

    /**
     * 編集可否.
     */
    private boolean editable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Activity初期設定
        // ※タイトル文字を空白
        ActivityCompat.initActivity(this, R.layout.editor, R.drawable.icon, "", true);

        Log.d("EditorActivity", "*** START onCreate()");

        // EditTextの編集可否処理を登録
        final EditText editorEditText = (EditText) findViewById(R.id.editor);
        editorEditText.setFilters(new InputFilter[]
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

        // intentからパラメータを取得
        String fullPath = getIntent().getStringExtra("FULL_PATH");
        String currentFolderPath = getIntent().getStringExtra("CURRENT_FOLDER");

        // 検索ツールクローズイベント登録
        ImageButton searchCloseButton = (ImageButton) findViewById(R.id.edit_search_close);
        searchCloseButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LinearLayout editSearchToolLayout = (LinearLayout) findViewById(R.id.edit_search_tool);
                editSearchToolLayout.setVisibility(View.GONE);
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
                this.memoFile = (MemoFile) builder.buildFromFile(fullPath);
            }
            else
            {
                // 新規
                this.memoFile = (MemoFile) builder.build(currentFolderPath, MemoType.Text);
                setEditable(true);
            }
        }
        catch (FileNotFoundException ex)
        {
            // 無視
            Log.e("EditorActivity", "*** START onCreate()", ex);
            return;
        }

        String memoData = this.memoFile.getText();

        if ((memoData == null)
                && ((this.memoFile.getMemoType() == MemoType.Secret1) || (this.memoFile.getMemoType() == MemoType.Secret2)))
        {
            // パスワードを入力
            final InputDialog dialog = new InputDialog();
            dialog.showDialog(this, getResources().getString(R.string.ui_td_input_password), InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // OK処理
                    String tryPassword = dialog.getText();

                    MainApplication.getInstance(EditorActivity.this).addPassword(tryPassword);

                    setMemoData();
                }
            }, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface d, int which)
                {
                    // キャンセル処理
                    setMemoData();
                }
            });
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
        EditText editorEditText = (EditText) findViewById(R.id.editor);

        // IMEを消去
        setImeVisibility(false, searchWordEditText);

        // フォーカスをエディタに移す
        editorEditText.requestFocus();

        // 検索および文字列を選択
        if (searchWord.length() > 0)
        {
            String editingText = editorEditText.getText().toString();

            int searchStartIndex;
            if (nextFg)
            {
                searchStartIndex = editorEditText.getSelectionEnd();
            }
            else
            {
                searchStartIndex = editorEditText.getSelectionStart() - 1;
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
                editorEditText.setSelection(searchIndex, searchIndex + searchWord.length());
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

        // 自動リンクを設定
        EditText editorEditText = (EditText) findViewById(R.id.editor);
        if (MainPreferenceActivity.isEnableAutoLink(this))
        {
            editorEditText.setAutoLinkMask(Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
        }
        else
        {
            editorEditText.setAutoLinkMask(0);
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
            boolean modify = false;

            if (isEditable())
            {
                modify = saveMemoData(new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // エディタ終了
                        setEditable(false);
                        finish();
                    }
                }, false);
            }

            if (!modify)
            {
                // エディタ終了
                setEditable(false);
                finish();
            }

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
        MenuItem actionItemEdit = menu.add(Menu.NONE, MENU_ID_EDIT, Menu.NONE, R.string.ui_edit);
        actionItemEdit.setIcon(R.drawable.memo_compose);
        ActivityCompat.setShowAsAction4ActionBar(actionItemEdit);

        // メニュー項目「編集終了」
        MenuItem actionItemEditEnd = menu.add(Menu.NONE, MENU_ID_EDIT_END, Menu.NONE, R.string.ui_edit_end);
        actionItemEditEnd.setIcon(R.drawable.save);
        ActivityCompat.setShowAsAction4ActionBar(actionItemEditEnd);

        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // 編集中フラグによりメニューの項目を切り替え
        menu.findItem(MENU_ID_EDIT).setVisible(!editable);
        menu.findItem(MENU_ID_EDIT_END).setVisible(editable);

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
            LinearLayout editSearchToolLayout = (LinearLayout) findViewById(R.id.edit_search_tool);
            editSearchToolLayout.setVisibility(View.VISIBLE);

            EditText searchWordEditText = (EditText) findViewById(R.id.edit_search_word);
            searchWordEditText.requestFocus();
            break;

        case MENU_ID_EDIT: // 編集
            setEditable(true);
            // メニュー項目を再表示
            ActivityCompat.refreshMenu4ActionBar(this);
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

                    // メニュー項目を再表示
                    ActivityCompat.refreshMenu4ActionBar(EditorActivity.this);
                }
            }, true);

            // 変更がないならそのまま編集終了
            if (!modified)
            {
                setEditable(false);

                // メニュー項目を再表示
                ActivityCompat.refreshMenu4ActionBar(EditorActivity.this);
            }
            break;

        case android.R.id.home: // UPアイコン

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
                }, true);
            }

            if (!modify)
            {
                // Kumagusuを表示
                finish();
            }
            break;

        default:
            break;
        }

        return ret;
    }

    /**
     * メモデータが変更されていれば、ファイルに保存する.
     *
     * @param postOkNoListener OK,NOの後処理のリスナ
     * @param dispCancel キャンセルを表示するときtrue
     * @return メモデータに変更があるときtrue
     */
    private boolean saveMemoData(final DialogInterface.OnClickListener postOkNoListener, boolean dispCancel)
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
                            EditText memoEditText = (EditText) findViewById(R.id.editor);
                            String memoData = memoEditText.getText().toString();

                            // 編集元テキストを更新
                            EditorActivity.this.originalMemoString = memoData;

                            // 改行コードをDOS形式に
                            memoData = memoData.replaceAll("\n", "\r\n");

                            // メモを保存
                            EditorActivity.this.memoFile.setText(memoData);

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

        EditText memoEditText = (EditText) findViewById(R.id.editor);

        this.originalMemoString = memoData.replaceAll("\r", "");
        boolean editableTemp = this.editable;
        try
        {
            this.editable = true;
            memoEditText.setText(this.originalMemoString);
        }
        finally
        {
            this.editable = editableTemp;
        }

        // IMEを消去
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // ファイルが開けない場合リストに戻る
        if (!openedFg)
        {
            // タイマを破棄
            this.autoCloseTimer = null;

            // エディタ終了
            finish();
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

        // IME制御
        EditText memoEditText = (EditText) findViewById(R.id.editor);

        setImeVisibility(ed, memoEditText);
    }

    /**
     * IME表示状態を変更する.
     *
     * @param ed 表示するときtrue
     * @param textView view
     */
    private void setImeVisibility(boolean ed, TextView textView)
    {
        if (ed)
        {
            // IME表示
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
            {
                imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }
        }
        else
        {
            // IME非表示
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
            {
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
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
        EditText memoEditText = (EditText) findViewById(R.id.editor);
        String currentMemoString = memoEditText.getText().toString();

        return (!currentMemoString.equals(this.originalMemoString));
    }

    /**
     * 入力部品のIMEの有効無効を設定する.
     */
    public void initialyzeEditorImeVisibility()
    {
        final EditText view = (EditText) findViewById(R.id.editor);

        view.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (!isEditable())
                {
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    setImeVisibility(false, view);
                }
            }
        });

        view.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (!isEditable())
                {
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    setImeVisibility(false, view);
                }

                return false;
            }
        });
    }

}
