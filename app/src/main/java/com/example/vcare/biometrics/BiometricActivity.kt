 package com.example.vcare.biometrics

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.vcare.R
import com.example.vcare.login.LoginActivity
import java.util.concurrent.Executor
import kotlin.math.absoluteValue

class BiometricActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        val executor = ContextCompat.getMainExecutor(this)

        val biometricManager = androidx.biometric.BiometricManager.from(this)

        when (biometricManager.canAuthenticate().absoluteValue) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                authUser(executor)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(
                    this,
                    getString(R.string.no_hardware),
                    Toast.LENGTH_LONG
                ).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(
                    this,
                    getString(R.string.hardware_unavailable),
                    Toast.LENGTH_LONG
                ).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(
                    this,
                    getString(R.string.biometric_not_setup),
                    Toast.LENGTH_LONG
                ).show()
        }
    }

    private fun authUser(executor: Executor?) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_required))
            .setSubtitle(getString(R.string.continue_using_app))
            .setNegativeButtonText("cancel")
            .build()

        val biometricPrompt = executor?.let {
            BiometricPrompt(this, it,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int, errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.error_occured),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.auth_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.welcome),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@BiometricActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                })
        }

        biometricPrompt?.authenticate(promptInfo)
    }
}