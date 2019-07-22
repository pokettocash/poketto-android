package com.poketto.poketto.controllers

import android.os.Bundle
import android.app.Activity
import android.util.Log
import android.graphics.PointF
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener
import kotlinx.android.synthetic.main.activity_qrcode.*
import android.content.Intent


class CustomQRCodeActivity : Activity(), OnQRCodeReadListener {

    private var qrCodeReaderView: QRCodeReaderView? = null
    private var detectedQRCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_qrcode)

        qrCodeReaderView = findViewById(com.poketto.poketto.R.id.qrdecoderview)
        qrCodeReaderView!!.setOnQRCodeReadListener(this)

        // Use this function to enable/disable decoding
        qrCodeReaderView!!.setQRDecodingEnabled(true)

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView!!.setAutofocusInterval(2000L)

        // Use this function to enable/disable Torch
        qrCodeReaderView!!.setTorchEnabled(false)

        // Use this function to set back camera preview
        qrCodeReaderView!!.setBackCamera()

        close.setOnClickListener {
            finish()
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    override fun onQRCodeRead(text: String, points: Array<PointF>) {
        if(!detectedQRCode) {
            Log.d("onQRCodeRead", "text: " + text)
            detectedQRCode = true
//            val intent = Intent(intent)
//            val b = Bundle()
//            b.putString("address", text)
//            intent.putExtras(b)
//            startActivityForResult(intent, RESULT_OK)
//            finishActivity(RESULT_OK)
            val resultIntent = Intent()
            resultIntent.putExtra("address", text)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        qrCodeReaderView!!.startCamera()
    }

    override fun onPause() {
        super.onPause()
        qrCodeReaderView!!.stopCamera()
    }
}