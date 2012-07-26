package jp.gr.java_conf.kumagusu.preference;

import java.util.List;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import jp.gr.java_conf.kumagusu.control.ListDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
                        setFixedPhraseStrings(fixedPhraseStrings);
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
        ListView listView = (ListView) view.findViewById(R.id.fixed_phrase_list);

        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            /**
             * 既存の定型文を編集する.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                final InputDialog fixedPhraseEditor = new InputDialog();
                fixedPhraseEditor.setText(fixedPhraseStrings.get(position));

                fixedPhraseEditor.showDialog(getContext(), null,
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

                                ListDialog.showDialog(getContext(),
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

        Button button = (Button) view.findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener()
        {
            /**
             * 定型文を追加する.
             */
            @Override
            public void onClick(View v)
            {
                fixedPhraseStrings.add("");
                listViewAdapter.notifyDataSetChanged();
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
     * 定型文を保存する.
     *
     * @param fixedPhraseStrings 定型文
     */
    private void setFixedPhraseStrings(List<String> fixedPhraseStrings)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        Editor editor = sp.edit();

        int fixedPhraseStringsCount = sp.getInt("list_fixed_phrase_strings_count", 0);

        // 一旦すべて削除
        for (int i = 0; i < fixedPhraseStringsCount; i++)
        {
            editor.remove("list_fixed_phrase_strings_" + i);
        }

        // 保存
        for (int i = 0; i < fixedPhraseStrings.size(); i++)
        {
            editor.putString("list_fixed_phrase_strings_" + i, fixedPhraseStrings.get(i));
        }

        editor.putInt("list_fixed_phrase_strings_count", fixedPhraseStrings.size());

        editor.commit();
    }
}
