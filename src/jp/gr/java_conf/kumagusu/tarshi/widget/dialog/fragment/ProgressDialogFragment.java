package jp.gr.java_conf.kumagusu.tarshi.widget.dialog.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public final class ProgressDialogFragment extends DialogFragment {
    private boolean goDismiss = false;
    private CharSequence message = null;
    private ProgressDialog progressDialog;
    private int progressDialogId = -1;
    private int progressDialogSid = -1;

    public interface ProgressDialogManagerInterface {
        ProgressDialogRegsterInterface getProgressDialogRegster();
    }

    public interface ProgressDialogRegsterInterface {
        ProgressDialogFragment clearProgressDialog(int i);

        int getProgresDialogId();

        ProgressDialogFragment getProgressDialog(int i);

        int registProgressDialog(ProgressDialogFragment progressDialogFragment);
    }

    public int getProgressDialogId() {
        return this.progressDialogId;
    }

    public int getProgressDialogSid() {
        return this.progressDialogSid;
    }

    public void setProgressDialogSid(int sid) {
        this.progressDialogSid = sid;
    }

    private static ProgressDialogFragment newInstance(int iconId, int titleId, int messageId, boolean cancelable) {
        ProgressDialogFragment frag = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putInt("iconId", iconId);
        args.putInt("titleId", titleId);
        args.putInt("messageId", messageId);
        args.putBoolean("cancelable", cancelable);
        frag.setArguments(args);
        return frag;
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d("ProgressDialogFragment", "*** Start onCreate()");
        super.onCreate(savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("ProgressDialogFragment", "*** Start onCreateDialog()");
        int iconId = getArguments().getInt("iconId");
        int titleId = getArguments().getInt("titleId");
        int messageId = getArguments().getInt("messageId");
        boolean cancelable = getArguments().getBoolean("cancelable");
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setProgressStyle(0);
        this.progressDialog.setCancelable(cancelable);
        setCancelable(cancelable);
        if (iconId != 0) {
            this.progressDialog.setIcon(iconId);
        }
        if (titleId != 0) {
            this.progressDialog.setTitle(titleId);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("progressDialogId")) {
            this.progressDialogId = savedInstanceState.getInt("progressDialogId");
            Log.d("ProgressDialogFragment", " id:" + this.progressDialogId);
            if (this.progressDialogId >= 0 && !getProgressDialogRegster(getActivity()).registProgressDialog(this.progressDialogId, this)) {
                this.goDismiss = true;
            }
        }
        CharSequence msg = null;
        if (this.message == null) {
            if (savedInstanceState != null && savedInstanceState.containsKey("message")) {
                msg = savedInstanceState.getCharSequence("message");
            }
            if (msg == null && messageId != 0) {
                msg = getString(messageId);
            }
        } else {
            msg = this.message;
        }
        if (msg != null) {
            setMessage(msg);
        }
        return this.progressDialog;
    }

    public void onResume() {
        Log.d("ProgressDialogFragment", "*** Start onResume() id:" + this.progressDialogId);
        super.onResume();
        if (this.goDismiss) {
            Log.d("ProgressDialogFragment", "Destroy progress dialog!! id:" + this.progressDialogId);
            dismiss();
        }
    }

    public void onPause() {
        Log.d("ProgressDialogFragment", "*** Start onPause() id:" + this.progressDialogId);
        super.onPause();
    }

    public void onStop() {
        Log.d("ProgressDialogFragment", "*** Start onStop() id:" + this.progressDialogId);
        super.onStop();
    }

    public void onDestroy() {
        Log.d("ProgressDialogFragment", "*** Start onDestroy() id:" + this.progressDialogId);
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle args) {
        Log.d("ProgressDialogFragment", "*** Start onSaveInstanceState() id:" + this.progressDialogId);
        args.putInt("progressDialogId", this.progressDialogId);
        if (this.message != null) {
            args.putCharSequence("message", this.message);
        }
        super.onSaveInstanceState(args);
    }

    public void setMessage(CharSequence msg) {
        Log.d("ProgressDialogFragment", "*** Start setMessage() id:" + this.progressDialogId);
        this.message = msg;
        if (this.progressDialog != null) {
            this.progressDialog.setMessage(msg);
        }
    }

    public static int showProgressDialog(FragmentActivity act, int iconId, int titleId, int messageId, boolean cancelable) {
        int id;
        synchronized (getProgressDialogRegster(act).lockObject) {
            Log.d("ProgressDialogFragment", "*** Start showProgressDialog()");
            id = -1;
            try {
                ProgressDialogFragment dialog = newInstance(iconId, titleId, messageId, cancelable);
                id = getProgressDialogRegster(act).registProgressDialog(dialog);
                dialog.progressDialogId = id;
                dialog.show(act.getSupportFragmentManager(), "");
            } catch (Exception e) {
                Log.d("MainApplication", "Progress dialog show error", e);
            }
        }
        return id;
    }

    public static void dismissProgressDialog(FragmentActivity act, int id) {
        synchronized (getProgressDialogRegster(act).lockObject) {
            try {
                Log.d("ProgressDialogFragment", "*** Start dismissProgressDialog() id:" + id);
                ProgressDialogFragment dialog = getProgressDialogRegster(act).clearProgressDialog(id);
                if (dialog != null) {
                    dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.d("MainApplication", "Dialog dismiss error.", ex);
            }
        }
    }

    private static ProgressDialogRegster getProgressDialogRegster(Activity act) {
        if (act.getApplication() instanceof ProgressDialogManagerInterface) {
            return (ProgressDialogRegster) ((ProgressDialogManagerInterface) act.getApplication()).getProgressDialogRegster();
        }
        return null;
    }

    public static ProgressDialogRegsterInterface newInstanceOfProgressDialogRegster() {
        return new ProgressDialogRegster((ProgressDialogRegster) null);
    }

    private static class ProgressDialogRegster implements ProgressDialogRegsterInterface {
        /* access modifiers changed from: private */
        public Object lockObject;
        private int progresDialogIdBase;
        private ProgressDialogFragment progressDialog;
        private int progressDialogId;

        private ProgressDialogRegster() {
            this.progresDialogIdBase = 0;
            this.progressDialogId = -1;
            this.lockObject = new Object();
            this.progressDialog = null;
        }

        /* synthetic */ ProgressDialogRegster(ProgressDialogRegster progressDialogRegster) {
            this();
        }

        public int getProgresDialogId() {
            return this.progressDialogId;
        }

        public ProgressDialogFragment getProgressDialog(int id) {
            synchronized (this.lockObject) {
                Log.d("MainApplication", "*** Start getProgressDialog() id:" + id);
                if (id != this.progressDialogId) {
                    return null;
                }
                ProgressDialogFragment progressDialogFragment = this.progressDialog;
                return progressDialogFragment;
            }
        }

        public ProgressDialogFragment clearProgressDialog(int id) {
            ProgressDialogFragment result = null;
            synchronized (this.lockObject) {
                Log.d("MainApplication", "*** Start clearProgressDialog() id:" + id);
                if (id == this.progressDialogId) {
                    result = this.progressDialog;
                    this.progressDialog = null;
                    this.progressDialogId = -1;
                }
            }
            return result;
        }

        public int registProgressDialog(ProgressDialogFragment dialog) {
            int i;
            synchronized (this.lockObject) {
                Log.d("MainApplication", "*** Start registProgressDialog()");
                if (this.progressDialog != null) {
                    Log.d("MainApplication", "Destroy old progress dialog!! id:" + this.progressDialogId);
                    try {
                        this.progressDialog.dismiss();
                    } catch (Exception ex) {
                        Log.d("MainApplication", "Dismiss progress dialog error. id:" + this.progressDialogId, ex);
                    }
                }
                this.progresDialogIdBase++;
                this.progressDialog = dialog;
                this.progressDialogId = this.progresDialogIdBase;
                i = this.progressDialogId;
            }
            return i;
        }

        public boolean registProgressDialog(int id, ProgressDialogFragment dialog) {
            synchronized (this.lockObject) {
                Log.d("MainApplication", "*** Start registProgressDialog() id:" + id);
                if (this.progressDialogId != id) {
                    return false;
                }
                this.progressDialog = dialog;
                return true;
            }
        }
    }
}
