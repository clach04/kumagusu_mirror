package jp.gr.java_conf.kumagusu.memoio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.gr.java_conf.kumagusu.R;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * メモユーティリティー.
 *
 * @author tarshi
 *
 */
public final class MemoUtilities
{
    /**
     * インスタンス化させない.
     */
    private MemoUtilities()
    {
    }

    /**
     * ファイル名またはフォルダ名に使用不可の文字を削除する.
     *
     * @param srcName 元のファイル・フォルダ名
     * @return 修正後のファイル名・フォルダ名
     */
    public static String sanitizeFileNameString(String srcName)
    {
        return srcName.replaceAll("[\"|;:<>/*?\\\\\u00a5]", "");
    }

    /**
     * SDカードのパスを取得する.
     *
     * @return SDカードのパス
     */
    private static String getExternalStoragePath()
    {
        File file = Environment.getExternalStorageDirectory();
        String sdcard = file.getAbsolutePath();

        return sdcard;
    }

    /**
     * デフォルトのメモフォルダを取得する.
     *
     * @return デフォルトのメモフォルダ
     */
    public static String getDefaultMemoFolderPath()
    {
        // デフォルトのメモフォルダを生成
        for (int i = 0;; i++)
        {
            StringBuilder builder = new StringBuilder("Kumagusu");

            if (i > 0)
            {
                builder.append("(").append(String.valueOf(i)).append(")");
            }

            String sdCardPath = getExternalStoragePath();

            if (sdCardPath == null)
            {
                sdCardPath = "/";
            }

            File defaultFile = new File(sdCardPath, builder.toString());

            if ((!defaultFile.exists()) || (!defaultFile.isFile()))
            {
                return defaultFile.getAbsolutePath();
            }
        }
    }

    /**
     * ファイル情報からメモ種別を生成する.
     *
     * @param file ファイル情報
     * @return メモ種別
     */
    public static MemoType getMemoType(File file)
    {
        String name = file.getName();

        MemoType type = MemoType.None;

        if (file.isDirectory())
        {
            type = MemoType.Folder;
        }
        else if (file.isFile())
        {
            if (name.endsWith(".txt"))
            {
                type = MemoType.Text;
            }
            else if (name.endsWith(".chi"))
            {
                type = MemoType.Secret1;
            }
            else if (name.endsWith(".chs"))
            {
                type = MemoType.Secret2;
            }
            else
            {
                type = MemoType.None;
            }
        }

        return type;
    }

    /**
     * メモ種別を名称に変換する.
     *
     * @param con コンテキスト
     * @param type メモ種別
     * @return タイプ名称
     */
    public static String type2Name(Context con, MemoType type)
    {
        String name;

        switch (type)
        {
        case Text:
            name = con.getResources().getString(R.string.etc_memo_type_text);
            break;
        case Secret1:
            name = con.getResources().getString(R.string.etc_memo_type_secret1);
            break;
        case Secret2:
            name = con.getResources().getString(R.string.etc_memo_type_secret2);
            break;
        case Folder:
            name = con.getResources().getString(R.string.etc_memo_type_folder);
            break;
        case ParentFolder:
            name = con.getResources().getString(R.string.etc_memo_type_parent_folder);
            break;
        default:
            name = con.getResources().getString(R.string.etc_memo_type_none);
        }

        return name;
    }

    /**
     * メモ種別を拡張子に変換する.
     *
     * @param type メモ種別
     * @return 拡張子
     */
    public static String type2Ext(MemoType type)
    {
        String ext;

        switch (type)
        {
        case Text:
            ext = "txt";
            break;
        case Secret1:
            ext = "chi";
            break;
        case Secret2:
            ext = "chs";
            break;
        default:
            // メモ以外
            ext = null;
        }

        return ext;
    }

    /**
     * ファイルをコピーする.
     *
     * @param in コピー元ファイル
     * @param out コピー先ファイル
     * @return 成功:true
     */
    public static boolean copyFile(File in, File out)
    {

        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;

        try
        {
            sourceChannel = new FileInputStream(in).getChannel();
            destinationChannel = new FileOutputStream(out).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);

            return true;
        }
        catch (Exception ex)
        {
            Log.w("MemoUtilities", "file copy error.", ex);
            return false;
        }
        finally
        {
            if (sourceChannel != null)
            {
                try
                {
                    sourceChannel.close();
                }
                catch (IOException e)
                {
                    Log.w("MemoUtilities", "source file close error.", e);
                }
            }
            if (destinationChannel != null)
            {
                try
                {
                    destinationChannel.close();
                }
                catch (IOException e)
                {
                    Log.w("MemoUtilities", "destination file close error.", e);
                }

                // コピー元とコピー先の更新日時をあわせる
                out.setLastModified(in.lastModified());
            }
        }
    }

    /**
     * ファイルを削除する.
     *
     * @param deleteFilePath 削除対象ファイルのパス（絶対パス）
     * @return 成功:true
     */
    public static boolean deleteFile(String deleteFilePath)
    {
        return deleteFile(new File(deleteFilePath));
    }

    /**
     * ファイルを削除する.
     *
     * @param deleteFile 削除対象ファイル（ファイル情報）
     * @return 成功:true
     */
    private static boolean deleteFile(File deleteFile)
    {
        if (deleteFile.exists())
        {
            return deleteFile.delete();
        }

        return false;
    }

    /**
     * 新しいファイルのFileインスタンスを生成する.
     *
     * @param folderPath フォルダパス（絶対パス）
     * @param fileName ファイル名
     * @return 新しいファイルのFileインスタンス
     */
    public static File createNewFileName(String folderPath, String fileName)
    {
        int cammaIndex = fileName.lastIndexOf('.');

        String fileNameBaseBody;
        String fileNameExt;

        if (cammaIndex >= 0)
        {
            fileNameBaseBody = fileName.substring(0, cammaIndex);
            fileNameExt = fileName.substring(cammaIndex + 1);
        }
        else
        {
            fileNameBaseBody = fileName;
            fileNameExt = null;
        }

        for (int i = 0;; i++)
        {
            StringBuilder sb = new StringBuilder(fileNameBaseBody);

            if (i > 0)
            {
                sb.append("(");
                sb.append(String.valueOf(i));
                sb.append(")");
            }

            if (fileNameExt != null)
            {
                sb.append(".");
                sb.append(fileNameExt);
            }

            File resultFile = new File(folderPath, sb.toString());

            if (!resultFile.exists())
            {
                return resultFile;
            }
        }
    }

    /**
     * メモをコピーする.
     *
     * @param srcMemoFile コピー元メモ
     * @param path コピー先フォルダパス（絶対パス）
     * @return コピー成功の時true
     */
    public static boolean copyMemoFile(MemoFile srcMemoFile, String path)
    {
        // コピー先フォルダが存在するか？
        File destPathFile = new File(path);

        if ((!destPathFile.exists()) || (!destPathFile.isDirectory()))
        {
            Log.w("MemoUtilities", "dest folder not exists.");
            return false;
        }

        File destFile = createNewFileName(path, srcMemoFile.getName());

        return copyFile(new File(srcMemoFile.getPath()), destFile);
    }

    /**
     * メモを移動する.
     *
     * @param srcMemoFile 移動元メモ
     * @param path 移動先フォルダ
     * @return 移動成功ならtrue
     */
    public static boolean moveMemoFile(MemoFile srcMemoFile, String path)
    {
        File dstFolderFile = new File(path);

        // コピー先フォルダが存在するか？
        if ((!dstFolderFile.exists()) || (!dstFolderFile.isDirectory()))
        {
            Log.w("MemoUtilities", "dest folder not exists.");
            return false;
        }

        if (!dstFolderFile.getAbsolutePath().equals(srcMemoFile.getParent()))
        {
            if (copyMemoFile(srcMemoFile, path))
            {
                // 移動成功した場合、元のファイルを削除
                deleteFile(srcMemoFile.getPath());
            }
        }

        return true;
    }

    /**
     * フォルダをコピーする.
     *
     * @param srcMemoFolder コピー元メモフォルダ
     * @param destPath コピー先フォルダパス（絶対パス）
     * @return コピー成功の時true
     */
    public static boolean copyMemoFolder(MemoFolder srcMemoFolder, String destPath)
    {
        return copyMemoFolder(new File(srcMemoFolder.getPath()), new File(destPath, srcMemoFolder.getName()), false);
    }

    /**
     * フォルダを移動する.
     *
     * @param srcMemoFolder 移動元メモフォルダ
     * @param destPath 移動先フォルダパス（絶対パス）
     * @return 移動成功の時true
     */
    public static boolean moveMemoFolder(MemoFolder srcMemoFolder, String destPath)
    {
        return copyMemoFolder(new File(srcMemoFolder.getPath()), new File(destPath, srcMemoFolder.getName()), true);
    }

    /**
     * フォルダをコピー（移動）する.
     *
     * @param src コピー元フォルダ
     * @param dest コピー先フォルダ
     * @param moveFg 移動処理フラグ（コピー元を削除）
     * @return コピー成功の時true
     */
    private static boolean copyMemoFolder(File src, File dest, boolean moveFg)
    {
        if (src.isDirectory())
        {
            // コピー先に同名のファイルがある場合、新たな名前を作成
            if ((dest.exists()) && (dest.isFile()))
            {
                dest = createNewFileName(dest.getParent(), dest.getName());
            }

            // コピー先フォルダがなければ作成
            if (!dest.exists())
            {
                dest.mkdir();
            }

            // フォルダ内の全ファイルを取得
            File[] srcFiles = src.listFiles();

            for (File srcFile : srcFiles)
            {
                // コピー先フォルダがコピー元フォルダに含まれている場合コピー中止
                if (dest.getAbsolutePath().equals(srcFile.getAbsolutePath()))
                {
                    continue;
                }

                // コピー先ファイルのFileインスタンス生成
                File destFile = createNewFileName(dest.getAbsolutePath(), srcFile.getName());

                // フォルダコピーを再帰呼び出し
                copyMemoFolder(srcFile, destFile, moveFg);
            }

            // 移動処理のとき元フォルダを削除
            if (moveFg)
            {
                deleteFile(src);
            }

            return true;
        }
        else
        {
            // ファイルコピー
            boolean result = copyFile(src, dest);

            // 移動処理のとき元ファイルを削除
            if ((result) && (moveFg))
            {
                deleteFile(src);
            }

            return result;
        }
    }

    /**
     * Int32値をバイトデータに変換する.
     *
     * @param value int32値
     * @param byteArray バイトデータ書込先バイト配列
     * @param startPos 書込開始位置
     * @throws IOException IO例外
     */
    public static void int2ByteArray(int value, byte[] byteArray, int startPos) throws IOException
    {
        DataOutputStream out = null;

        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            out = new DataOutputStream(bout);

            value = Integer.reverseBytes(value);
            out.writeInt(value);

            byte[] outBytes = bout.toByteArray();

            for (int i = 0; i < 4; i++)
            {
                byteArray[i + startPos] = outBytes[i];

            }

        }
        finally
        {
            if (out != null)
            {
                out.close();
                out = null;
            }
        }
    }

    /**
     * バイトデータをInt32値に変換する.
     *
     * @param byteArray バイトデータが書き込まれたバイト配列
     * @param startPos バイトデータの開始位置
     * @return Int32値
     * @throws IOException IO例外
     */
    public static int byteArray2int(byte[] byteArray, int startPos) throws IOException
    {
        DataInputStream in = null;

        try
        {
            ByteArrayInputStream bin = new ByteArrayInputStream(byteArray, startPos, 4);

            in = new DataInputStream(bin);

            int value = in.readInt();

            value = Integer.reverseBytes(value);

            return value;
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * バイトオーダを変換する.
     *
     * @param byteArray バイト配列
     * @param length 変換するバイト数
     * @param startPos 開始位置
     * @return 変換結果
     */
    public static boolean changeByteOrder(byte[] byteArray, int length, int startPos)
    {
        if ((byteArray.length - startPos) < length)
        {
            return false;
        }

        for (int i = startPos + 3; i < startPos + length; i += 4)
        {
            byte[] tmp = new byte[4];

            tmp[0] = byteArray[i - 3];
            tmp[1] = byteArray[i - 2];
            tmp[2] = byteArray[i - 1];
            tmp[3] = byteArray[i - 0];

            byteArray[i - 3] = tmp[3];
            byteArray[i - 2] = tmp[2];
            byteArray[i - 1] = tmp[1];
            byteArray[i - 0] = tmp[0];
        }
        return true;
    }

    /**
     * 端末設定に従った「日付」または「日付＋時刻」文字列を返す.
     *
     * @param con コンテキスト
     * @param date 時間情報
     * @param appendTime 時刻を付加する場合true
     * @return 「日付」または「日付＋時刻」文字列
     */
    public static String formatDateTime(Context con, Date date, boolean appendTime)
    {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(con);

        StringBuilder sb = new StringBuilder(dateFormat.format(date));

        if (appendTime)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sb.append(" ").append(sdf.format(date));
        }

        return sb.toString();
    }

    /**
     * 数値を省略表記に変更する.
     *
     * @param baseNum 元の値
     * @return 省略表記
     */
    public static String formatNumber(long baseNum)
    {

        long absNum = Math.abs(baseNum);
        String unit;

        if (absNum < 1024L)
        {
            // バイト表示
            unit = " B";
        }
        else if (absNum < 1048576L)
        {
            // Kバイト表示
            unit = " KB";
            baseNum /= 1024L;
        }
        else if (absNum < 1073741824L)
        {
            // Mバイト表示
            unit = " MB";
            baseNum /= 1048576L;
        }
        else if (absNum < 1099511627776L)
        {
            // Gバイト表示
            unit = " GB";
            baseNum /= 1073741824L;
        }
        else
        {
            // Tバイト表示
            unit = " TB";
            baseNum /= 1099511627776L;
        }

        DecimalFormat df = new DecimalFormat("###,###,###;-###,###,###");

        StringBuilder sb = new StringBuilder();
        sb.append(df.format(baseNum)).append(unit);

        return sb.toString();
    }
}
