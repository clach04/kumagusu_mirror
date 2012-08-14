package jp.gr.java_conf.kumagusu.control;

import java.util.ArrayList;

import jp.gr.java_conf.kumagusu.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * リストダイアログ.
 *
 * @author tarshi
 */
public class ListDialogFragment extends DialogFragment
{
    /**
     * リストのアダプター.
     */
    private ArrayAdapter<String> listAdapter;

    /**
     * メッセージ表示View.
     */
    private TextView messageTextView = null;

    /**
     * リスト表示View.
     */
    private ListView listView = null;

    /**
     * 選択行.
     */
    private int checkedItem = 0;

    /**
     * リストダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param conditionNames 選択行
     * @return ダイアログ
     */
    public static ListDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId,
            String[] conditionNames)
    {
        return newInstance(listenerId, iconId, titleId, messageId, conditionNames, -1);
    }

    /**
     * リストダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param conditionNames 選択行
     * @param checkedItem 選択インデックス（０以上のとき単一選択モード）
     * @return ダイアログ
     */
    public static ListDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId,
            String[] conditionNames, int checkedItem)
    {
        ListDialogFragment frag = new ListDialogFragment();

        Bundle args = new Bundle();

        args.putInt("listenerId", listenerId);
        args.putInt("titleId", titleId);
        args.putInt("iconId", iconId);
        args.putInt("messageId", messageId);
        args.putStringArray("conditionNames", conditionNames);
        args.putInt("checkedItem", checkedItem);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int listenerId = getArguments().getInt("listenerId");
        int titleId = getArguments().getInt("titleId");
        int iconId = getArguments().getInt("iconId");
        int messageId = getArguments().getInt("messageId");
        String[] conditionNames = getArguments().getStringArray("conditionNames");

        this.checkedItem = getArguments().getInt("checkedItem");

        // アクティビティーからリスナ取得
        Activity activity = getActivity();

        DialogListeners listeners = null;

        if (activity instanceof ListDialogListenerFolder)
        {
            ListDialogListenerFolder listenerFolder = (ListDialogListenerFolder) activity;
            listeners = listenerFolder.getListDialogListeners(listenerId);
        }

        final DialogInterface.OnClickListener okListener;
        DialogInterface.OnClickListener cancelListener;

        if (listeners != null)
        {
            okListener = listeners.getOkOnClickListener();
            cancelListener = listeners.getCancelOnClickListener();
        }
        else
        {
            okListener = null;
            cancelListener = null;
        }

        // View取得
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_view_dialog, null);

        if (messageId > 0)
        {
            this.messageTextView = (TextView) view.findViewById(messageId);
            this.messageTextView.setVisibility(TextView.VISIBLE);
        }

        this.listView = (ListView) view.findViewById(R.id.list_view);

        // ダイアログ生成
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());

        if (iconId > 0)
        {
            db.setIcon(iconId);
        }

        db.setTitle(titleId);
        db.setView(view);

        // アダプタ設定
        ArrayList<String> currentFolderList = new ArrayList<String>();
        this.listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                currentFolderList);
        this.listView.setAdapter(this.listAdapter);

        for (String name : conditionNames)
        {
            this.listAdapter.add(name);
        }

        // OK、キャンセルボタン設定
        if (cancelListener == null)
        {
            cancelListener = new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // キャンセル処理なし
                }
            };
        }

        if (this.checkedItem >= 0)
        {
            this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            this.listView.setItemChecked(this.checkedItem, true);
            db.setPositiveButton(R.string.ui_ok, okListener);
        }

        db.setNegativeButton(R.string.ui_cancel, cancelListener);

        // ダイアログ生成
        final Dialog dialog = db.create();

        // イベント設定
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (ListDialogFragment.this.checkedItem >= 0)
                {
                    ListDialogFragment.this.checkedItem = position;
                }
                else if (okListener != null)
                {
                    okListener.onClick(dialog, position);
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }
}
