package jp.gr.java_conf.kumagusu.preference;

import jp.gr.java_conf.kumagusu.R;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * バージョン情報.
 *
 * @author tarshi
 *
 */
public final class VersionPreference extends Preference
{
    /**
     * 設定のメモフォルダ選択処理を初期化する.
     *
     * @param context コンテキスト
     * @param attrs 属性
     */
    public VersionPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view)
    {
        setSummary(getVersionInfo());

        super.onBindView(view);
    }

    /**
     * バージョン情報を返す.
     *
     * @return バージョン情報
     */
    private String getVersionInfo()
    {
        String appName = getContext().getResources().getString(R.string.app_name);

        String versionName;

        try
        {
            PackageInfo info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(),
                    PackageManager.GET_META_DATA);
            versionName = info.versionName;
        }
        catch (NameNotFoundException e)
        {
            versionName = "";
        }

        return new StringBuilder(appName).append(" ").append(versionName).toString();
    }
}
