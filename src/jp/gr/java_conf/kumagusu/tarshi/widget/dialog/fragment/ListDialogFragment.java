package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import jp.gr.java_conf.kumagusu.tarshi.widget.dialog.DialogListeners;
import jp.gr.java_conf.kumagusu.R;

public class ListDialogFragment extends DialogFragment {
    /* access modifiers changed from: private */
    public int checkedItem = 0;
    private ArrayAdapter<String> listAdapter;
    private ListView listView = null;
    private TextView messageTextView = null;

    public static ListDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId, String[] conditionNames) {
        return newInstance(listenerId, iconId, titleId, messageId, conditionNames, -1);
    }

    public static ListDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId, String[] conditionNames, int checkedItem2) {
        ListDialogFragment frag = new ListDialogFragment();
        Bundle args = new Bundle();
        args.putInt("listenerId", listenerId);
        args.putInt("titleId", titleId);
        args.putInt("iconId", iconId);
        args.putInt("messageId", messageId);
        args.putStringArray("conditionNames", conditionNames);
        args.putInt("checkedItem", checkedItem2);
        args.putBoolean("positiveButton", checkedItem2 >= 0);
        frag.setArguments(args);
        return frag;
    }

    public static ListDialogFragment newInstance(int listenerId, int iconId, int titleId, int messageId, String[] conditionNames, int checkedItem2, boolean positiveButton) {
        ListDialogFragment frag = new ListDialogFragment();
        Bundle args = new Bundle();
        args.putInt("listenerId", listenerId);
        args.putInt("titleId", titleId);
        args.putInt("iconId", iconId);
        args.putInt("messageId", messageId);
        args.putStringArray("conditionNames", conditionNames);
        args.putInt("checkedItem", checkedItem2);
        args.putBoolean("positiveButton", positiveButton);
        frag.setArguments(args);
        return frag;
    }

    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogInterface.OnClickListener okListener;
        DialogInterface.OnClickListener cancelListener;
        int listStyle;
        int listenerId = getArguments().getInt("listenerId");
        int titleId = getArguments().getInt("titleId");
        int iconId = getArguments().getInt("iconId");
        int messageId = getArguments().getInt("messageId");
        String[] conditionNames = getArguments().getStringArray("conditionNames");
        this.checkedItem = getArguments().getInt("checkedItem");
        boolean positiveButton = getArguments().getBoolean("positiveButton");
        FragmentActivity activity = getActivity();
        DialogListeners listeners = null;
        if (activity instanceof ListDialogListenerFolder) {
            listeners = ((ListDialogListenerFolder) activity).getListDialogListeners(listenerId);
        }
        if (listeners != null) {
            okListener = listeners.getOkOnClickListener();
            cancelListener = listeners.getCancelOnClickListener();
        } else {
            okListener = null;
            cancelListener = null;
        }
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.list_view_dialog, (ViewGroup) null);
        if (messageId > 0) {
            this.messageTextView = (TextView) view.findViewById(messageId);
            this.messageTextView.setVisibility(0);
        }
        this.listView = (ListView) view.findViewById(R.id.list_view);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        if (iconId > 0) {
            db.setIcon(iconId);
        }
        db.setTitle(titleId);
        if (this.checkedItem >= 0) {
            listStyle = 17367055;
        } else {
            listStyle = 17367043;
        }
        this.listAdapter = new ArrayAdapter<>(getActivity(), listStyle, new ArrayList<>());
        this.listView.setAdapter(this.listAdapter);
        for (String name : conditionNames) {
            this.listAdapter.add(name);
        }
        if (this.checkedItem >= 0) {
            this.listView.setChoiceMode(1);
            if (positiveButton) {
                final DialogInterface.OnClickListener onClickListener = okListener;
                db.setPositiveButton(R.string.ui_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (onClickListener != null) {
                            onClickListener.onClick(dialog, ListDialogFragment.this.checkedItem);
                        }
                    }
                });
            }
            this.listView.setItemChecked(this.checkedItem, true);
            this.listView.setSelection(this.checkedItem);
        }
        if (cancelListener == null) {
            cancelListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            };
        }
        db.setNegativeButton(R.string.ui_cancel, cancelListener);
        final AlertDialog dialog = db.create();
        dialog.setView(view, 0, 0, 0, 0);
        final boolean z = positiveButton;
        final DialogInterface.OnClickListener onClickListener2 = okListener;
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (z) {
                    ListDialogFragment.this.checkedItem = position;
                } else if (onClickListener2 != null) {
                    onClickListener2.onClick(dialog, position);
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }
}
