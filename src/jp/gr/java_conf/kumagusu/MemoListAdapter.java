package jp.gr.java_conf.kumagusu;

import java.util.List;

import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * メモリストにメモ情報を表示するアダプタ.
 *
 * @author tarshi
 *
 */
public final class MemoListAdapter extends ArrayAdapter<IMemo>
{
    /**
     * レイアウトのID.
     */
    private LayoutInflater layoutInflater;

    /**
     * メモリストにメモ情報を表示するアダプタを初期化する.
     *
     * @param context コンテキスト
     * @param memoList メモ
     */
    public MemoListAdapter(Context context, List<IMemo> memoList)
    {
        super(context, R.layout.list_item, R.id.title, memoList);

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        View view;

        if (convertView == null)
        {
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }
        else
        {
            view = convertView;
        }

        View fileDetailAreaView;

        ImageView iconImageView;
        TextView titleTextView;
        TextView detailsTextView;

        try
        {
            fileDetailAreaView = (View) view.findViewById(R.id.file_detail_area);

            iconImageView = (ImageView) view.findViewById(R.id.memo_type_image);
            titleTextView = (TextView) view.findViewById(R.id.title);
            detailsTextView = (TextView) view.findViewById(R.id.details);
        }
        catch (ClassCastException e)
        {
            Log.e("FileListAdapter", "TextView get error");
            throw new IllegalStateException("TextView get error", e);
        }

        // メモデータからウィジェットへ表示項目を設定
        IMemo item = this.getItem(position);

        Drawable iconDrawable;
        switch (item.getMemoType())
        {
        case Text:
            iconDrawable = getContext().getResources().getDrawable(R.drawable.memo_text);
            break;

        case Secret1:
        case Secret2:
            iconDrawable = getContext().getResources().getDrawable(R.drawable.memo_secret);
            break;

        case Folder:
            iconDrawable = getContext().getResources().getDrawable(R.drawable.folder);
            break;

        case ParentFolder:
            iconDrawable = getContext().getResources().getDrawable(R.drawable.folder_up);
            break;

        default:
            iconDrawable = getContext().getResources().getDrawable(R.drawable.memo_unknown);
            break;
        }

        // リストの詳細表示を設定
        if (MainPreferenceActivity.isListDetailVisibility(getContext()))
        {
            fileDetailAreaView.setVisibility(View.VISIBLE);
        }
        else
        {
            fileDetailAreaView.setVisibility(View.GONE);
        }

        iconImageView.setImageDrawable(iconDrawable);
        titleTextView.setText(item.getTitle());
        detailsTextView.setText(item.getDetails());

        return view;
    }
}
