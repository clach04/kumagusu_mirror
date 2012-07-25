package jp.gr.java_conf.kumagusu.preference;

import jp.gr.java_conf.kumagusu.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * 定型文入力ダイアログ.
 *
 * @author tarshi
 *
 */
public final class InputFixedPhraseDialogPreference extends DialogPreference
{
    private ViewGroup parentViewGroup = null;

    @Override
    protected View onCreateView(ViewGroup parent)
    {
        this.parentViewGroup = parent;
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
    }

    @Override
    protected void onClick()
    {
        // ダイアログビルダを生成
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(getContext().getResources().getString(R.string.pref_fixed_phrase_dialog_title));

        // 定型文入力のレイアウトからViewを設定
        // alertDialogBuilder.setView(view);

        // OKボタンの処理
        alertDialogBuilder.setPositiveButton(getContext().getString(R.string.ui_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // 設定を保存

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

        // ダイアログ生成
        AlertDialog dialog = alertDialogBuilder.create();

        // カスタムViewを取得
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.pref_input_fixed_phrase_dialog, null);

        // 定型文リストを設定
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, MainPreferenceActivity.getFixedPhraseStrings(getContext()));

        ListView listView = (ListView) view.findViewById(R.id.fixed_phrase_list);

        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

            }
        });

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
}
