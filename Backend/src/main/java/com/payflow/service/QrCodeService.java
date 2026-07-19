package com.payflow.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QrCodeService {

	private static final int SIZE = 300;

	/**
	 * Encodes a UPI-style payment payload as a QR code and returns it as a base64
	 * PNG string, ready to be embedded directly in an
	 * <img src="data:image/png;base64,..."> tag.
	 */
	public String generateQrCodeBase64(String vpa, String payeeName) {
		String payload = buildUpiPayload(vpa, payeeName);
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, SIZE, SIZE);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (WriterException | IOException e) {
			throw new RuntimeException("Failed to generate QR code", e);
		}
	}

	/**
	 * Mimics the standard "upi://pay?..." deep-link format used by real UPI apps.
	 */
	private String buildUpiPayload(String vpa, String payeeName) {
		return "upi://pay?pa=" + vpa + "&pn=" + payeeName.replace(" ", "%20") + "&cu=INR";
	}

	/**
	 * Dynamic QR: encodes a fixed amount and optional note, the way a merchant's
	 * "scan to pay this exact amount" QR works. Not persisted - generated fresh on
	 * each request.
	 */
	public String generateDynamicQrCodeBase64(String vpa, String payeeName, java.math.BigDecimal amount, String note) {
		StringBuilder payload = new StringBuilder(buildUpiPayload(vpa, payeeName));
		if (amount != null) {
			payload.append("&am=").append(amount.toPlainString());
		}
		if (note != null && !note.isBlank()) {
			payload.append("&tn=").append(note.replace(" ", "%20"));
		}
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(payload.toString(), BarcodeFormat.QR_CODE, SIZE, SIZE);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (WriterException | IOException e) {
			throw new RuntimeException("Failed to generate QR code", e);
		}
	}
}
