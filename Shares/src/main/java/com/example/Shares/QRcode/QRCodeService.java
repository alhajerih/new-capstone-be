package com.example.Shares.QRcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class QRCodeService {

    @Autowired
    private QRCodeRepository qrCodeRepository;

    // Save QR code transaction
    public QRCodeEntity saveQRCode(QRCodeEntity qrCodeEntity) {
        return qrCodeRepository.save(qrCodeEntity);
    }

    // Find QR code by transaction ID
    public Optional<QRCodeEntity> findByTransactionId(String transactionId) {
        return qrCodeRepository.findByTransactionId(transactionId);
    }
}
