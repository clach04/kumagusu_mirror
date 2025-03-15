package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import jp.gr.java_conf.kumagusu.R;

public final class SelectFolderDialogFragment extends DialogFragment {
    /* access modifiers changed from: private */
    public ArrayList<File> currentFolderFileList = new ArrayList<>();
    private ArrayAdapter<String> currentFolderListAdapter;
    /* access modifiers changed from: private */
    public String currentFolderPath = null;
    /* access modifiers changed from: private */
    public ListView listView = null;
    private TextView messageTextView = null;
    private File rootFolderFile = null;

    public interface OnSelectFolderListener {
        void onSelect(String str);
    }

    public static SelectFolderDialogFragment newInstance(int listenerId, int titleId, String chrootPath, String path) {
        return newInstance(listenerId, 0, titleId, chrootPath, path);
    }

    public static SelectFolderDialogFragment newInstance(int listenerId, int iconId, int titleId, String chrootPath, String path) {
        SelectFolderDialogFragment frag = new SelectFolderDialogFragment();
        Bundle args = new Bundle();
        args.putInt("listenerId", listenerId);
        args.putInt("iconId", iconId);
        args.putInt("titleId", titleId);
        args.putString("chrootPath", chrootPath);
        args.putString("currentFolderPath", new File(path).getAbsolutePath());
        frag.setArguments(args);
        return frag;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final OnSelectFolderListener okListener;
        final OnSelectFolderListener cancelListener;
        int listenerId = getArguments().getInt("listenerId");
        int iconId = getArguments().getInt("iconId");
        int titleId = getArguments().getInt("titleId");
        String chrootPath = getArguments().getString("chrootPath");
        if (savedInstanceState == null || !savedInstanceState.containsKey("currentFolderPath")) {
            this.currentFolderPath = getArguments().getString("currentFolderPath");
        } else {
            this.currentFolderPath = savedInstanceState.getString("currentFolderPath");
        }
        if (chrootPath == null) {
            chrootPath = "/";
        }
        this.rootFolderFile = new File(chrootPath);
        FragmentActivity activity = getActivity();
        SelectFolderDialogListeners listeners = null;
        if (activity instanceof SelectFolderDialogListenerFolder) {
            listeners = ((SelectFolderDialogListenerFolder) activity).getSelectFolderDialogListeners(listenerId);
        }
        if (listeners != null) {
            okListener = listeners.getOkOnClickListener();
            cancelListener = listeners.getCancelOnClickListener();
        } else {
            okListener = null;
            cancelListener = null;
        }
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.list_view_dialog, (ViewGroup) null);
        this.listView = (ListView) view.findViewById(R.id.list_view);
        this.messageTextView = (TextView) view.findViewById(R.id.message);
        this.messageTextView.setVisibility(0);
        this.currentFolderListAdapter = new ArrayAdapter<>(getActivity(), 17367043, new ArrayList<>());
        this.listView.setAdapter(this.currentFolderListAdapter);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SelectFolderDialogFragment.this.currentFolderPath = ((File) SelectFolderDialogFragment.this.currentFolderFileList.get(position)).getAbsolutePath();
                SelectFolderDialogFragment.this.createCurrentFolderList();
                SelectFolderDialogFragment.this.listView.setSelectionFromTop(0, 0);
            }
        });
        createCurrentFolderList();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        if (iconId > 0) {
            alertDialogBuilder.setIcon(iconId);
        }
        alertDialogBuilder.setTitle(getString(titleId));
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(getActivity().getString(R.string.ui_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d("DirectorySelectDialog", SelectFolderDialogFragment.this.currentFolderPath);
                if (okListener != null) {
                    okListener.onSelect(SelectFolderDialogFragment.this.currentFolderPath);
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getActivity().getString(R.string.ui_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (cancelListener != null) {
                    cancelListener.onSelect((String) null);
                }
            }
        });
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (cancelListener != null) {
                    cancelListener.onSelect((String) null);
                }
            }
        });
        return alertDialogBuilder.create();
    }

    public void onSaveInstanceState(Bundle args) {
        args.putString("currentFolderPath", this.currentFolderPath);
        super.onSaveInstanceState(args);
    }

    /* access modifiers changed from: private */
    public void createCurrentFolderList() {
        if (this.currentFolderPath == null || this.currentFolderPath.length() == 0) {
            this.currentFolderPath = "/";
        }
        File currentFolder = new File(this.currentFolderPath);
        File[] mDirectories = currentFolder.listFiles();
        if (mDirectories != null) {
            Arrays.sort(mDirectories);
        } else {
            mDirectories = new File[0];
        }
        String showTitle = currentFolder.getAbsolutePath();
        if (!this.rootFolderFile.getAbsolutePath().equals("/")) {
            showTitle = showTitle.substring(this.rootFolderFile.getAbsolutePath().length());
            if (showTitle.length() == 0) {
                showTitle = "/";
            }
        }
        this.messageTextView.setText(showTitle);
        this.currentFolderFileList.clear();
        this.currentFolderListAdapter.clear();
        if (!currentFolder.getPath().equals(this.rootFolderFile.getPath())) {
            this.currentFolderListAdapter.add("..");
            this.currentFolderFileList.add(currentFolder.getParentFile());
        }
        for (File file : mDirectories) {
            if (file.isDirectory()) {
                this.currentFolderListAdapter.add(String.valueOf(file.getName()) + "/");
                this.currentFolderFileList.add(file);
            }
        }
    }
}
