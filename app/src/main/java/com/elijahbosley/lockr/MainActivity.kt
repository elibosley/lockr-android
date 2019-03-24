package com.elijahbosley.lockr

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var unlockButton: Button
    lateinit var lockButton: Button
    // Firebase Authentication Providers
    private var providers = Arrays.asList<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.EmailBuilder().build())
    private var RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        unlockButton = findViewById(R.id.unlock_button)
        lockButton = findViewById(R.id.lock_button)
        lockButton.setOnClickListener { changeFirebaseVal("locked") }
        unlockButton.setOnClickListener { changeFirebaseVal("unlocked") }
        disableButtons()
        connectToFirebase()
    }

    private fun connectToFirebase() {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        auth.addAuthStateListener {
            authStateChanged(auth, db)
        }

    }

    override fun onResume() {
        super.onResume()
        connectToFirebase()
    }

    private fun enableButtons() {
        unlockButton.isEnabled = true
        lockButton.isEnabled = true
    }

    private fun disableButtons() {
        unlockButton.isEnabled = false
        lockButton.isEnabled = false
    }

    private fun authStateChanged(auth: FirebaseAuth, db: FirebaseFirestore) {
        if (auth.currentUser != null) { // Logged In
            findViewById<Button>(R.id.login).text = getString(R.string.log_out_button_text)
            db.collection("users").document(auth.currentUser!!.uid).get()
                    .addOnSuccessListener { result ->
                        val isAdmin = result["admin"] as Boolean
                        if (isAdmin) {
                            enableButtons()
                        } else {
                            disableButtons()
                        }
                    }
        } else {
            findViewById<Button>(R.id.login).text = getText(R.string.log_in_button_text)
            disableButtons()
        }
    }

    private fun changeFirebaseVal(lockstatus: String) {
        if (auth.currentUser != null) { //TODO add more auth here holy cow
            val data = HashMap<String, String>()
            data["lock_status"] = lockstatus
            db.collection("locks").document("1").set(data as Map<String, Any>, SetOptions.merge())
        }
    }

    fun firebaseLogin(view: View) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            //TODO launch user management page here
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle("Log Out")
            alertDialog.setMessage("Are you sure you wish to log out")
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL") { dialog, which -> dialog.dismiss() }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "LOG OUT"
            ) { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                dialog.dismiss()
            }
            alertDialog.show()
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN)
        }
    }
}

