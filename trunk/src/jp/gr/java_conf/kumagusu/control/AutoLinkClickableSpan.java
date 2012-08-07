package jp.gr.java_conf.kumagusu.control;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * 自動リンク用のSpan処理.
 *
 * @author tarshi
 *
 */
public final class AutoLinkClickableSpan extends ClickableSpan
{
    /**
     * 自動リンクパターン.
     */
    private Pattern pattern;

    /**
     * 自動リンクのクリックイベントリスナ.
     */
    private AutoLinkOnClickListener onClickListener;

    /**
     * コンストラクタ.
     *
     * @param patt 自動リンクパターン
     * @param listener 自動リンクのクリックイベントリスナ
     */
    public AutoLinkClickableSpan(Pattern patt, AutoLinkOnClickListener listener)
    {
        super();

        this.pattern = patt;
        this.onClickListener = listener;
    }

    @Override
    public void onClick(View view)
    {
        if (!(view instanceof TextView))
        {
            return;
        }

        TextView textView = (TextView) view;

        int selStart = textView.getSelectionStart();
        int selEnd = textView.getSelectionEnd();
        CharSequence text = textView.getText();
        String selString = text.subSequence(selStart, selEnd).toString();

        Matcher matcher = pattern.matcher(selString);

        if (matcher.find())
        {
            if (this.onClickListener != null)
            {
                try
                {
                    this.onClickListener.onClick(matcher.group());
                }
                catch (Exception ex)
                {
                    Log.w("AutoLinkClickableSpan", "link click error.", ex);
                }
            }
        }
    }

    /**
     * 自動リンクのクリックイベントリスナ.
     *
     * @author tarshi
     *
     */
    public interface AutoLinkOnClickListener
    {
        /**
         * 自動リンクのクリックイベントを処理する.
         *
         * @param matchString リンク文字列
         */
        void onClick(String matchString);
    }
}
