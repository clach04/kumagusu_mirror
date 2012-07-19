package som.kumagusu.memoio;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import som.kumagusu.Utilities;

import de.mud.ssh.Cipher;

/**
 * BlowfishCBC関連の処理.
 *
 * @author tarshi
 *
 */
public final class TomboBlowfishCBC
{
    /**
     * 文字セット名称.
     */
    private String charsetName;

    /**
     * chipher.
     */
    private Cipher cipher;

    /**
     * MD5ダイジェスト.
     */
    private MessageDigest md5Digest;

    /**
     * BlowfishCBC関連の処理を初期化する.
     *
     * @param csName 文字セット名称
     * @throws IOException IO例外
     */
    public TomboBlowfishCBC(String csName) throws IOException
    {
        this.charsetName = csName;

        this.cipher = Cipher.getInstance("Blowfish");

        try
        {
            this.md5Digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException("password digest no sach algorithm.");
        }
    }

    /**
     * MD5ダイジェストを再生成する.
     *
     * @param planeKey パスワード
     * @throws IOException IO例外
     */
    private void resetBlowfishCBC(String planeKey) throws IOException
    {
        this.md5Digest.reset();
        this.md5Digest.update(planeKey.getBytes(this.charsetName));
        byte[] keyDigest = this.md5Digest.digest();

        this.cipher.setKey(keyDigest);
    }

    /**
     * 平文文字列を暗号化バイト配列に暗号化する.
     *
     * @param planeKey パスワード
     * @param strData 平文文字列
     * @return 暗号化バイト配列
     * @throws IOException IO例外
     */
    public byte[] encrypt(String planeKey, String strData) throws IOException
    {
        // ----------------------------------------------------------
        // 元のデータを生成
        // ----------------------------------------------------------
        byte[] byteData = strData.getBytes(this.charsetName);
        int strDataSize = byteData.length;
        int dataSize = strDataSize;
        dataSize += (8 - (dataSize % 8));
        byte[] enc = new byte[dataSize + 32];

        // ----------------------------------------------------------
        // ヘッダ部を設定
        // ----------------------------------------------------------
        // "BF01"
        System.arraycopy("BF01".getBytes(this.charsetName), 0, enc, 0, 4);

        // データサイズ
        Utilities.int2ByteArray(strDataSize, enc, 4);

        // エンコード対象データ・テンポラリ生成
        byte[] data2enc = new byte[enc.length - 8];

        // ランダム値
        Random random = new Random();
        for (int i = 0; i < 8; i++)
        {
            data2enc[i] = (byte) random.nextInt(Byte.MAX_VALUE);
        }

        // データのMD5ハッシュ値
        this.md5Digest.reset();
        byte[] md5sumFromData = this.md5Digest.digest(byteData);
        System.arraycopy(md5sumFromData, 0, data2enc, 8, 16);

        // データ
        System.arraycopy(byteData, 0, data2enc, 24, byteData.length);

        // ----------------------------------------------------------
        // 暗号化
        // ----------------------------------------------------------

        // リトルエンディアンに変更
        Utilities.changeByteOrder(data2enc, data2enc.length, 0);

        // パスワード設定
        resetBlowfishCBC(planeKey);

        // 暗号化
        this.cipher.encrypt(data2enc, 0, enc, 8, data2enc.length);

        // ビッグエンディアンに戻す
        Utilities.changeByteOrder(enc, data2enc.length, 8);

        return enc;
    }

    /**
     * 暗号化バイト配列を平文文字列に復号する.
     *
     * @param planeKey パスワード
     * @param enc エンコードデータ
     * @return デコード後のデータ
     * @throws IOException IO例外
     */
    public String decrypt(String planeKey, byte[] enc) throws IOException
    {
        byte[] orgEnc = new byte[enc.length];
        System.arraycopy(enc, 0, orgEnc, 0, enc.length);

        // --------------------------------------------------------------
        // ヘッダ部取得
        // --------------------------------------------------------------
        int dataLength = Utilities.byteArray2int(enc, 4);

        // --------------------------------------------------------------
        // 暗号化部を復号
        // --------------------------------------------------------------
        // 暗号化部のバイトオーダ変更（リトルエンディアンへ）
        Utilities.changeByteOrder(enc, enc.length - 8, 8);

        // 復号
        resetBlowfishCBC(planeKey);

        byte[] data = new byte[enc.length - 8];

        this.cipher.decrypt(enc, 8, data, 0, data.length);

        // バイトオーダー変換。元に戻す
        Utilities.changeByteOrder(data, data.length, 0);

        // ハッシュを用いデータが正しく復号出来たかチェック
        this.md5Digest.reset();
        this.md5Digest.update(data, 24, dataLength);
        byte[] md5sumFromData = this.md5Digest.digest();

        for (int i = 0; i < 16; i++)
        {
            if (data[i + 8] != md5sumFromData[i])
            {
                System.arraycopy(orgEnc, 0, enc, 0, orgEnc.length);
                return null;
            }
        }

        String strData = new String(data, 24, dataLength, this.charsetName);

        return strData;
    }
}
