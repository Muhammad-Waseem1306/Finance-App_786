package com.example.financeapp.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.databinding.ActivityOtpverificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpverificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String // Firebase verification ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Get the verification ID from the intent
        verificationId = intent.getStringExtra("verificationId") ?: ""

        binding.verifyButton.setOnClickListener {
            val enteredOTP = binding.otpEditText.text.toString().trim()
            if (enteredOTP.isNotEmpty()) {
                verifyOTP(enteredOTP)
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyOTP(otp: String) {
        // Create a credential using the OTP and verification ID
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to main activity or dashboard
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid OTP, please try again", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
