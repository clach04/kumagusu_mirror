package jp.gr.java_conf.kumagusu.compat;

import java.util.List;

import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * SDKバージョン間のActionBarサポート状況を吸収する処理.
 *
 * @author tarshi
 */
public final class ActivityCompat
{
    /**
     * インスタンス化させない.
     */
    private ActivityCompat()
    {
    }

    /**
     * Activityを初期化する.
     *
     * @param act アクティビティ
     * @param layoutId レイアウトID
     * @param iconId アイコンID
     * @param titleString タイトル文字
     * @param vilibleTitleBar タイトルバーを表示するときtrue
     * @param enableUpIcon UPアイコンを表示するときtrue
     */
    @SuppressLint("NewApi")
    public static void initActivity(Activity act, int layoutId, int iconId, String titleString,
            boolean vilibleTitleBar, boolean enableUpIcon)
    {
        if (!vilibleTitleBar) // タイトル非表示
        {
            act.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // 3.0以上
        {
            if (vilibleTitleBar)
            {
                // ActionBarを使用可能に
                act.getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);

                // UPアイコン(<)表示
                if (enableUpIcon)
                {
                    act.getActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        }
        else
        {
            if (vilibleTitleBar)
            {
                // 3.0未満
                act.getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
            }
        }

        // タイトル文字を空白
        if (titleString != null)
        {
            act.setTitle(titleString);
        }

        // レイアウト読込
        act.setContentView(layoutId);

        // 3.0以下
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        {
            if (vilibleTitleBar)
            {
                act.getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, iconId);
            }
        }
    }

    /**
     * ActionBarにメニュー項目を設定する.
     *
     * @param menuItem メニュー項目
     */
    @SuppressLint("NewApi")
    public static void setShowAsAction4ActionBar(MenuItem menuItem)
    {
        // 3.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    /**
     * ActionBarのメニュー項目を再表示する.
     *
     * @param act アクティビティ
     */
    @SuppressLint("NewApi")
    public static void refreshMenu4ActionBar(Activity act)
    {
        // 3.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            act.invalidateOptionsMenu();
        }
    }

    /**
     * 上位フォルダへの移動手段を設定する.
     *
     * @param act アクティビティ
     * @param memoList メモリスト（3.0未満のとき使用）
     * @param parentItem 親フォルダのIMemo（3.0未満のとき使用）
     */
    @SuppressLint("NewApi")
    public static void setUpFolderFunction(Activity act, List<IMemo> memoList, IMemo parentItem)
    {
        // 3.0以上かつタイトル表示か？
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                && (MainPreferenceActivity.isEnableEditorTitle(act)))
        {
            // UPアイコンを表示
            act.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            // ファイルリストに..を表示
            memoList.add(parentItem);
        }
    }

    /**
     * 検索結果リストのクローズ手段を設定する.
     *
     * @param act アクティビティ
     * @param closeButton クローズボタン（3.0未満のとき使用）
     */
    @SuppressLint("NewApi")
    public static void setCloseSearchResultFunction(Activity act, Button closeButton)
    {
        // 3.0以上か？
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            // UPアイコンを表示
            act.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            // クローズボタンを表示
            closeButton.setVisibility(View.VISIBLE);
        }
    }
}
