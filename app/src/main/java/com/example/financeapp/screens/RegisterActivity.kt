package com.example.financeapp.screens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R
import com.example.financeapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // apply color
        val textView = findViewById<TextView>(R.id.loginHere)
        val fullText = getString(R.string.already_have_an_account_login_here)
        val spannableString = SpannableString(fullText)

        // Find the start and end index of the text you want to color
        val startIndex = fullText.indexOf("Login here")
        val endIndex = startIndex + "Login here".length

        // Apply color to "Login here"
        spannableString.setSpan(
            ForegroundColorSpan(Color.RED),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbarRegister)
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set back navigation
        binding.toolbarRegister.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        binding.loginHere.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val rePassword = binding.rePasswordEditText.text.toString().trim()

            // Checking fields are empty or not
            if (email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else if (password != rePassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
