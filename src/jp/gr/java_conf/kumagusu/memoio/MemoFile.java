package jp.gr.java_conf.kumagusu.memoio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

import jp.gr.java_conf.kumagusu.MainApplication;

import jp.gr.java_conf.kumagusu.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

/**
 * メモ.
 *
 * @author tarshi
 */
public final class MemoFile extends AbstractMemo
{
    /**
     * メモパスワード.
     */
    private String memoPassword = null;

    /**
     * 復号化済み.
     */
    private boolean isDecryptFg = false;

    /**
     * 読込ファイルの最終更新日時.
     */
    private long lastModifyTime = 0;

    /**
     * メモタイトル.
     */
    private String title = null;

    /**
     * 既存ファイルを使用する場合（表示時）のコンストラクタ.
     *
     * @param context コンテキスト
     * @param memoFile メモのFileオブジェクト
     * @param encodingName エンコーディング名
     * @param titleLinkFg ファイルの一行目とタイトルを連動するか
     * @param type メモの種別
     */
    MemoFile(Context context, File memoFile, String encodingName, boolean titleLinkFg, MemoType type)
    {
        super(context, memoFile, encodingName, titleLinkFg, type);

        if ((type != MemoType.Text) && (type != MemoType.Secret1) && (type != MemoType.Secret2)
                && (type != MemoType.None))
        {
            throw new IllegalArgumentException("no memo file type:" + type.toString());
        }
    }

    /**
     * メモのデータを取得する.
     *
     * @return メモのデータ
     */
    public String getText()
    {
        this.memoPassword = null;

        if ((this.getMemoType() != MemoType.Text) && (this.getMemoType() != MemoType.Secret1)
                && (this.getMemoType() != MemoType.Secret2))
        {
            return "";
        }

        if (this.getMemoFile() == null)
        {
            return "";
        }

        // ファイルを読み込む
        BufferedInputStream bis = null;
        byte[] buffer = new byte[(int) this.getMemoFile().length()];

        try
        {
            bis = new BufferedInputStream(new FileInputStream(this.getMemoFile()));

            int length = bis.read(buffer);

            if (length != this.getMemoFile().length())
            {
                return "";
            }
        }
        catch (FileNotFoundException e)
        {
            return "";
        }
        catch (IOException e)
        {
            return "";
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    Log.w("MemoFile", "memo file close error.", e);
                }
            }
        }

        // エンコーディングに従い、String化
        String strData = null;

        if ((this.getMemoType() == MemoType.Secret1) || (this.getMemoType() == MemoType.Secret2))
        {
            // 暗号化ファイルなら復号
            for (String password : MainApplication.getInstance((Activity) getContext()).getPasswordList())
            {
                strData = decode(password, buffer);

                if (strData != null)
                {
                    // 解読OK
                    this.isDecryptFg = true;
                    this.memoPassword = password;
                    break;
                }
            }

            // 正しいパスワードが存在しない
            if (strData == null)
            {
                return null;
            }
        }
        else
        {
            // 通常テキスト
            this.isDecryptFg = true;

            try
            {
                strData = new String(buffer, this.getEncodingName());
            }
            catch (UnsupportedEncodingException e)
            {
                return "";
            }
        }

        return strData;
    }

    /**
     * メモのデータを設定（保存）する.
     *
     * @param memoData メモデータ
     * @return 保存成功時true
     */
    public boolean setText(String memoData)
    {
        return setText(null, memoData);
    }

    /**
     * メモのデータを設定（保存）する.
     *
     * @param password パスワード
     * @param memoData メモデータ
     * @return 保存成功時true
     */
    public boolean setText(String password, String memoData)
    {
        if ((this.getMemoType() != MemoType.Text) && (this.getMemoType() != MemoType.Secret1)
                && (this.getMemoType() != MemoType.Secret2))
        {
            return false;
        }

        // バイトデータ生成
        byte[] buffer = null;

        if ((this.getMemoType() == MemoType.Secret1) || (this.getMemoType() == MemoType.Secret2))
        {
            // 暗号化ファイルなら暗号化
            if (password == null)
            {
                password = this.memoPassword;
            }

            if (password == null)
            {
                throw new IllegalArgumentException("no password");
            }

            try
            {
                TomboBlowfishCBC cbc = new TomboBlowfishCBC(this.getEncodingName());
                buffer = cbc.encrypt(password, memoData);
                this.memoPassword = password;
            }
            catch (IOException e)
            {
                return false;
            }
        }
        else
        {
            // 通常テキスト
            try
            {
                buffer = memoData.getBytes(this.getEncodingName());
            }
            catch (UnsupportedEncodingException e)
            {
                return false;
            }
        }

        if (buffer == null)
        {
            return false;
        }

        // バイトデータを書き込む
        BufferedOutputStream bos = null;
        try
        {
            String memoFileNewPath = createPath(memoData);

            if ((this.getPath() == null) || ((this.getPath() != null) && (!memoFileNewPath.equals(this.getPath()))))
            {
                // 元のファイル名から変わったときは元のファイルを削除
                if (this.getPath() != null)
                {
                    this.getMemoFile().delete();
                }

                // Fileオブジェクトを再生成
                this.setMemoFile(new File(memoFileNewPath));
            }

            bos = new BufferedOutputStream(new FileOutputStream(memoFileNewPath, false));

            bos.write(buffer);
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    Log.w("MemoFile", "memo file close error.", e);
                }
            }
        }

        return true;
    }

    /**
     * メモパスワードを返す.
     *
     * @return メモパスワード
     */
    public String getMemoPassword()
    {
        return this.memoPassword;
    }

    /**
     * 暗号化データをデコードする.
     *
     * @param password パスワード
     * @param buffer 暗号化データ
     * @return 解読データ（成功時null以外）
     */
    private String decode(String password, byte[] buffer)
    {
        String strData = null;

        try
        {
            TomboBlowfishCBC cbc = new TomboBlowfishCBC(this.getEncodingName());
            strData = cbc.decrypt(password, buffer);
        }
        catch (IOException e)
        {
            // パスワードの誤り（？）
            Log.w("MemoFile", "memo file decode error.", e);
        }

        return strData;
    }

    /**
     * メモのパスを生成する.
     *
     * @param memoData メモデータ
     * @return メモのパス
     */
    private String createPath(String memoData)
    {
        // 新規の場合は、ファイル名を生成
        // 既存の場合は、連動する場合のみファイル名を生成
        if ((this.getMemoFile() == null) || ((this.isTitleLinkFg()) && (this.getMemoType() != MemoType.Secret2)))
        {
            String fileNameBody;

            for (int i = 0;; i++)
            {
                StringBuilder sb = new StringBuilder();

                // ファイル名をランダムにする
                if (this.getMemoType() == MemoType.Secret2)
                {
                    Random random = new Random();
                    for (int j = 0; j < 16; j++)
                    {
                        int rv = random.nextInt(10);
                        sb.append(rv);
                    }
                }
                else
                {
                    // データの一行目を取得
                    String firstLine = getFirstLineOfText(memoData);

                    // タイトル文字をファイル名で使用可能な文字のみに修正
                    firstLine = MemoUtilities.sanitizeFileNameString(firstLine);

                    sb.append(firstLine);

                    if (sb.length() == 0)
                    {
                        sb.append(this.getContext().getResources()
                                .getString(R.string.etc_memo_file_untitled_4_filename));
                    }

                    if (i > 0)
                    {
                        sb.append("(");
                        sb.append(String.valueOf(i));
                        sb.append(")");
                    }
                }

                sb.append(".");
                sb.append(MemoUtilities.type2Ext(this.getMemoType()));

                fileNameBody = sb.toString();

                File file = new File(this.getFolderFile().getAbsolutePath(), fileNameBody);

                // 既存ファイルで元のファイル名と同じ、
                // または生成したファイル名と同じファイルが存在しない場合、
                // ファイル名として採用
                if (((this.getMemoFile() != null) && (this.getMemoFile().getAbsolutePath().equals(file
                        .getAbsolutePath()))) || (!file.exists()))
                {
                    return file.getAbsolutePath();
                }
            }
        }
        else
        {
            return getPath();
        }
    }

    /**
     * 文字列データの一行目を取得する.
     *
     * @param text 文字列データ
     * @return 文字列データの一行目
     */
    private String getFirstLineOfText(String text)
    {
        if (text != null)
        {
            int crPosition = text.indexOf('\r');
            if (crPosition < 0)
            {
                crPosition = text.indexOf('\n');
            }

            String firstLine;

            if (crPosition >= 0)
            {
                firstLine = text.substring(0, crPosition).trim();
            }
            else
            {
                firstLine = text;
            }

            return firstLine;
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getTitle()
    {
        long nowLastModifyTime = (this.getMemoFile() != null) ? this.getMemoFile().lastModified() : 0;

        if ((this.title == null) || (this.lastModifyTime != nowLastModifyTime))
        {
            if ((this.getMemoType() == MemoType.Text) || (this.getMemoType() == MemoType.Secret1)
                    || (this.getMemoType() == MemoType.Secret2))
            {
                this.title = getFirstLineOfText(getText());
            }
        }

        this.lastModifyTime = nowLastModifyTime;

        if ((this.title == null) || (this.title.length() == 0))
        {
            return this.getContext().getResources().getString(R.string.etc_memo_file_untitled_4_title);
        }
        else
        {
            return this.title;
        }
    }

    @Override
    public String getPath()
    {
        return (this.getMemoFile() != null) ? this.getMemoFile().getAbsolutePath() : null;
    }

    @Override
    public String getName()
    {
        return (this.getMemoFile() != null) ? this.getMemoFile().getName() : null;
    }

    @Override
    public String getDetails()
    {
        StringBuilder sb = new StringBuilder();

        Configuration conf = getContext().getResources().getConfiguration();

        if (conf != null)
        {
            long modifyTime = (this.getMemoFile() != null) ? this.getMemoFile().lastModified() : 0;

            if (modifyTime != 0)
            {
                Date lastModifyDate = new Date(modifyTime);

                sb.append(MemoUtilities.formatDateTime(getContext(), lastModifyDate, true));
                sb.append(", ");
            }
        }

        sb.append(getName());

        if (conf != null)
        {
            if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                long fileSize = (this.getMemoFile() != null) ? this.getMemoFile().length() : -1;

                if (fileSize >= 0)
                {
                    sb.append(", ");
                    sb.append(MemoUtilities.formatNumber(fileSize));
                }

                sb.append(", ");
                sb.append(MemoUtilities.type2Name(getContext(), getMemoType()));
            }
        }

        return sb.toString();
    }

    @Override
    public String getParent()
    {
        return (this.getMemoFile() != null) ? this.getMemoFile().getParent() : null;
    }

    /**
     * 復号化済みフラグを返す.
     *
     * @return 復号化済みのときtrue（暗号化メモ以外では常にtrue）
     */
    public boolean isDecryptFg()
    {
        if (!this.isDecryptFg)
        {
            // 復号化を試行
            getText();
        }

        return this.isDecryptFg;
    }
}
