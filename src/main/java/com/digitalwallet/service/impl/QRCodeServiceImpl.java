package com.digitalwallet.service.impl;

import com.digitalwallet.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class QRCodeServiceImpl implements QRCodeService {


    @Override
    public String generatePaymentQR(String walletNumber, BigDecimal amount, String description) {
//        Create payment QR data in JSON format
        String qrData;
        qrData = String.format(
                "{\"type\":\"PAYMENT\",\"walletNumber\":\"%s\", \"amount\":\"%s\",\"description\":\"%s\",\"timestamp\":%d}",
                walletNumber, amount.toString(), description != null ? description : "", System.currentTimeMillis()
        );
        return qrData;
    }

    @Override
    public String generateWalletQR(String walletNumber) {
//        Create wallet QR data in JSON format
        String qrData = String.format(
                "{\"type\":\"WALLET\",\"walletNumber\":\"%S\",\"timestamp\":%d",
                walletNumber, System.currentTimeMillis()
        );
        return qrData;
    }

    @Override
    public byte[] generateQRCodeImage(String qrData, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }


}
