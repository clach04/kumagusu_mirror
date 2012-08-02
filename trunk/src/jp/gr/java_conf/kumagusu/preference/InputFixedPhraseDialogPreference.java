package jp.gr.java_conf.kumagusu.preference;

import java.util.List;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.control.ConfirmDialog;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.control.ListDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * 定型文入力ダイアログ.
 *
 * @author tarshi
 *
 */
public final class InputFixedPhraseDialogPreference extends DialogPreference
{
    /**
     * 削除.
     */
    private static final int FIXED_PHRASE_ENTRIES_CONTROL_ID_DELETE = 0;

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
    }

    @Override
    protected void onClick()
    {
        // アダプタ生成
        final List<String> fixedPhraseStrings = MainPreferenceActivity.getFixedPhraseStrings(getContext());

        final ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, fixedPhraseStrings);

        // ダイアログビルダを生成
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(getContext().getResources().getString(R.string.pref_fixed_phrase_dialog_title));

        // OKボタンの処理
        alertDialogBuilder.setPositiveButton(getContext().getString(R.string.ui_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // 設定を保存
                        MainPreferenceActivity.setFixedPhraseStrings(getContext(), fixedPhraseStrings);
                    }
                });

        // Cancelボタンの処理
        alertDialogBuilder.setNegativeButton(getContext().getString(R.string.ui_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // キャンセル処理なし
                    }
                });

        // カスタムViewを取得
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.pref_input_fixed_phrase_dialog, null);

        // 定型文リストを設定
        final ListView listView = (ListView) view.findViewById(R.id.fixed_phrase_list);

        listView.setAdapter(listViewAdapter);

        // リストのクリック処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            /**
             * 既存の定型文を編集する.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                editFixedPhraseString(fixedPhraseStrings.get(position), new FixedPhraseEditorOnTextInputListener()
                {
                    /**
                     * Okを処理する.
                     */
                    @Override
                    public void onTextInput(String inputText)
                    {
                        fixedPhraseStrings.set(position, inputText);
                        listViewAdapter.notifyDataSetChanged();
                    }
                });

                final InputDialog fixedPhraseEditor = new InputDialog(getContext());
                fixedPhraseEditor.setText(fixedPhraseStrings.get(position));

                fixedPhraseEditor.showDialog(null,
                        getContext().getResources().getString(R.string.fixed_phrase_dialog_title),
                        InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                        {
                            /**
                             * Okを処理する.
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                fixedPhraseStrings.set(position, fixedPhraseEditor.getText());
                                listViewAdapter.notifyDataSetChanged();
                            }
                        }, new DialogInterface.OnClickListener()
                        {
                            /**
                             * キャンセルを処理する.
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // キャンセルは無視
                            }
                        }, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                String[] phraseNames = getContext().getResources().getStringArray(
                                        R.array.fixed_phrase_escape_item_entries);
                                final String[] patternLetters = getContext().getResources().getStringArray(
                                        R.array.fixed_phrase_escape_item_values);

                                assert patternLetters.length == phraseNames.length;

                                for (int i = 0; i < patternLetters.length; i++)
                                {
                                    phraseNames[i] = new StringBuilder(phraseNames[i]).append(" (")
                                            .append(patternLetters[i]).append(")").toString();
                                }

                                ListDialog fixedPhrasePatternDialog = new ListDialog(getContext());
                                fixedPhrasePatternDialog.showDialog(
                                        getContext().getResources().getDrawable(R.drawable.fixed_phrase), getContext()
                                                .getResources().getString(R.string.fixed_phrase_pattern_letters),
                                        phraseNames, new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                String pasteString = patternLetters[which];

                                                fixedPhraseEditor.pasteText(pasteString);
                                            }
                                        });
                            }
                        }, getContext().getResources().getString(R.string.fixed_phrase_pattern_letters));
            }
        });

        // リストの長押しイベント
        listView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                ListDialog fixedPhraseEntryesControlDialog = new ListDialog(getContext());
                fixedPhraseEntryesControlDialog.showDialog(
                        null,
                        getContext().getResources().getString(R.string.pref_fixed_phrase_entries_control_dialog_title),
                        getContext().getResources().getStringArray(
                                R.array.pref_fixed_phrase_entries_control_dialog_entries), new OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                switch (which)
                                {
                                case FIXED_PHRASE_ENTRIES_CONTROL_ID_DELETE: // 削除
                                    // 削除の確認ダイアログを表示
                                    ConfirmDialog.showDialog(
                                            getContext(),
                                            getContext().getResources().getDrawable(android.R.drawable.ic_menu_delete),
                                            getContext().getResources().getString(
                                                    R.string.pref_fixed_phrase_entries_control_dialog_delete_title),
                                            getContext().getResources().getString(
                                                    R.string.pref_fixed_phrase_entries_control_dialog_delete_message),
                                            ConfirmDialog.PositiveCaptionKind.YES, new OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    // 削除
                                                    fixedPhraseStrings.remove(position);
                                                    listViewAdapter.notifyDataSetChanged();
                                                }
                                            }, new OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    // キャンセル
                                                }
                                            });
                                    break;

                                default:
                                    break;
                                }
                            }
                        });

                return true;
            }
        });

        Button button = (Button) view.findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener()
        {
            /**
             * 定型文を追加する.
             */
            @Override
            public void onClick(View v)
            {
                editFixedPhraseString("", new FixedPhraseEditorOnTextInputListener()
                {
                    /**
                     * Okを処理する.
                     */
                    @Override
                    public void onTextInput(String inputText)
                    {
                        fixedPhraseStrings.add(inputText);
                        listViewAdapter.notifyDataSetChanged();

                        // 最終行（追加行）を表示
                        listView.setSelection(listView.getCount());
                    }
                });
            }
        });

        // 定型文入力のレイアウトからViewを設定
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.setView(view, 0, 0, 0, 0);

        // ダイアログ表示
        dialog.show();
    }

    /**
     * 定型文入力ダイアログを初期化する.
     *
     * @param context コンテキスト
     * @param attrs 属性
     */
    public InputFixedPhraseDialogPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * 定型文の編集ダイアログを表示する.
     *
     * @param fixedPhraseString 編集する定型文の初期値
     * @param okListener OK（保存）の処理
     */
    private void editFixedPhraseString(String fixedPhraseString, final FixedPhraseEditorOnTextInputListener okListener)
    {
        final InputDialog fixedPhraseEditor = new InputDialog(getContext());
        fixedPhraseEditor.setText(fixedPhraseString);

        fixedPhraseEditor.showDialog(null, getContext().getResources().getString(R.string.fixed_phrase_dialog_title),
                InputType.TYPE_CLASS_TEXT, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (okListener != null)
                        {
                            okListener.onTextInput(fixedPhraseEditor.getText());
                        }
                    }
                }, new DialogInterface.OnClickListener()
                {
                    /**
                     * キャンセルを処理する.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // キャンセルは無視
                    }
                }, new View.OnClickListener()
                {
                    /**
                     * パターン入力ダイアログを表示する.
                     */
                    @Override
                    public void onClick(View v)
                    {
                        String[] phraseNames = getContext().getResources().getStringArray(
                                R.array.fixed_phrase_escape_item_entries);
                        final String[] patternLetters = getContext().getResources().getStringArray(
                                R.array.fixed_phrase_escape_item_values);

                        assert patternLetters.length == phraseNames.length;

                        for (int i = 0; i < patternLetters.length; i++)
                        {
                            phraseNames[i] = new StringBuilder(phraseNames[i]).append(" (").append(patternLetters[i])
                                    .append(")").toString();
                        }

                        ListDialog fixedPhrasePatternLettersDialog = new ListDialog(getContext());
                        fixedPhrasePatternLettersDialog.showDialog(
                                getContext().getResources().getDrawable(R.drawable.fixed_phrase), getContext()
                                        .getResources().getString(R.string.fixed_phrase_pattern_letters), phraseNames,
                                new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        String pasteString = patternLetters[which];

                                        fixedPhraseEditor.pasteText(pasteString);
                                    }
                                });
                    }
                }, getContext().getResources().getString(R.string.fixed_phrase_pattern_letters));
    }

    /**
     * 入力ダイアログから値を返すリスナ.
     *
     * @author tarshi
     *
     */
    private interface FixedPhraseEditorOnTextInputListener
    {
        /**
         * テキスト入力イベントリスナ.
         *
         * @param inputText 入力されたテキスト
         */
        void onTextInput(String inputText);
    }
}
