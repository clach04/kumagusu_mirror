package jp.gr.java_conf.kumagusu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * ユーティリティー.
 *
 * @author tarshi
 *
 */
public final class Utilities
{
    /**
     * ユーティリティーをインスタンス化させない.
     */
    private Utilities()
    {
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
}
