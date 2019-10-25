package me.ele.napos.evalon.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.NotFoundException
import com.google.zxing.Result
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

import javax.imageio.ImageIO

class QRCodeUtil {
    static String getQr(String text) {
        String s = "生成二维码失败"
        int width = 40
        int height = 40
        // 用于设置QR二维码参数
        Hashtable<EncodeHintType, Object> qrParam = new Hashtable<>()
        // 设置QR二维码的纠错级别——这里选择最低L级别
        qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
        qrParam.put(EncodeHintType.CHARACTER_SET, "utf-8")

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, qrParam)
            s = toAscii(bitMatrix)
        } catch (WriterException e) {
            e.printStackTrace()
        }
        return s
    }

    static String readQRCode(File image) throws IOException, NotFoundException {
        return readQRCode(new FileInputStream(image))
    }

    static String readQRCode(String base64) throws IOException, NotFoundException {
        return readQRCode(new ByteArrayInputStream(Base64.getDecoder().decode(base64)))
    }


    static String readQRCode(InputStream stream)
        throws IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(
            new HybridBinarizer(
                new BufferedImageLuminanceSource(
                    ImageIO.read(stream))))
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap)
        return qrCodeResult.getText()
    }

    private static String toAscii(BitMatrix bitMatrix) {
        StringBuilder sb = new StringBuilder()
        String black = "\033[40m  \033[0m"
        String white = "\033[50m  \033[0m"
        for (int rows = 0; rows < bitMatrix.getHeight(); rows++) {
            for (int cols = 0; cols < bitMatrix.getWidth(); cols++) {
                boolean x = bitMatrix.get(rows, cols)
                if (!x) {
                    // white
                    sb.append(black)
                } else {
                    sb.append(white)
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
