package com.example.dms.dmsa2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Represents a Battle action object which is transmitted to the opponent
 * via NFC.
 *
 * http://www.wikihow.com/Make-a-Rock%2C-Paper%2C-Scissors-Game-in-Java
 * https://developer.android.com/guide/topics/connectivity/nfc/nfc.html
 *
 * @author jaimesbooth 20150504
 * @modified jaimesbooth 20150505 Imported to Android project from Java CLI
 * @modified andy 20150507 added Bluetooth, modified NFC functionality and game play
 */
public class BattleAction extends Activity implements CreateNdefMessageCallback{

    private NfcAdapter mNfcAdapter;
    private TextView textView;

    private String myID;
    private String myMove;
    private String underAttack;

    /**
     * Called when this Activity is started.
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_action);
        // Associate the player's text field to the variable text view
        textView = (TextView) findViewById(R.id.attackText);
        // Set the player's ID based on the registered Bluetooth name
        myID = BluetoothAdapter.getDefaultAdapter().getName();
        // Initialise player's move
        myMove = "";
        // Initialise underAttack
        underAttack = "";

        // Set up NFC when battlefield is started
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Close Activity if NFC is not available
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }
        /*
         * Call setNdefPushMessageCallback passing in a NfcAdapter in the onCreate() method
		 * of your activity. These methods require at least one activity that you want to
		 * enable with Android Beam, along with an optional list of other activities to activate.
		 */
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    /**
     * Creates a NdefMessage that contains the NdefRecords (attack and player ID)
     * that are to be pushed onto the other device. Called when this device is
     * in range of another device that might support NDEF push.
     * It allows the application to create the NDEF message only when it is required.
     * @param event NfcEvent with the nfcAdapter field set
     * @return The NDEF message with embedded player id and player move to push, or null to not provide a message
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // Get the player's attack move
        // Generate a random move if the player's move has not been set.
        if (myMove.length() == 0) {
            myMove = randomMove();
//            textView.setText("You chose " + myMove);
        }
        // Embed player id and player move into the NDEF message
        String textMessage = (myID + "_" + myMove);
        NdefMessage msg = new NdefMessage(new NdefRecord[]{createMimeRecord(
                "text/plain", textMessage.getBytes())});
        return msg;
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType The name of the MIME type to encapsulate
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    /**
     * Called when the activity will start interacting with the user.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    /**
     * When the activity is re-launched while at the top of the activity stack
     * instead of a new instance of the activity being started, onNewIntent()
     * will be called on the existing instance with the Intent that was
     * used to re-launch it.
     *
     * An activity will always be paused before receiving a new intent,
     * so you can count on onResume() being called after this method.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and informs player
     * of the opponents attack
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];

        // Populate the underAttack String with the opponents id and attack move
        // record 0 contains the MIME type, record 1 is the AAR, if present
        underAttack = new String(msg.getRecords()[0].getPayload());

        // Notify player of attack
        textView.setText("You are under attack, \nselect your move to react");

    }

    /**
     * Generates a random attack move
     * @return A random attack move
     */
    private String randomMove() {
        Random random = new Random();
        int choice = random.nextInt(3);
        switch (choice) {
            case 0:
                return "ROCK";
            case 1:
                return "PAPER";
            case 2:
                return "SCISSORS";
            default:
                return "ERROR";
        }
    }

    /**
     * Computes the result (hit:win, counter:lose, missed:tie) of comparing
     * a player's move with the opponents move.
     * @param moveA Player's move
     * @param moveB Opponents move
     * @return The result of the comparison of attacks
     */
    public String computeResult(String moveA, String moveB) {
        // Tie
        if (moveA.equalsIgnoreCase(moveB)) {
            return "missed";
        }

        switch (moveA) {
            case "ROCK":
                // Ternary operator. If (testCondition is true), assign the value before the ":"
                // to result; otherwise, assign the value after ":" to result."
                return (moveB.equalsIgnoreCase("SCISSORS") ? "hit" : "counter");
            case "PAPER":
                return (moveB.equalsIgnoreCase("ROCK") ? "hit" : "counter");
            case "SCISSORS":
                return (moveB.equalsIgnoreCase("PAPER") ? "hit" : "counter");
        }

        // Should never reach here
        return "ERROR";
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     * Finishes the current activity.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Called when a view has been clicked.
     * Handles click events (to the buttons) in the view.
     * @param v The view that was clicked
     */
    public void onClick(View v) {
        if (v == findViewById(R.id.bRock))
            myMove = "ROCK";
        else if (v == findViewById(R.id.bPaper))
            myMove = "PAPER";
        else if (v == findViewById(R.id.bScisssors))
            myMove = "SCISSORS";
        // Change the text view to inform player of their chosen move
        textView.setText("==You chose " + myMove);

        // Check if the player is under attack
        if (underAttack.length() > 0) {
            // If under attack request a counter attack
            takeReaction();
        }
    }

    /**
     * Called when a player is under attack.
     * Extracts the opponents id and opponents attack move from
     * the underAttack string.
     */
    private void takeReaction() {
        // Extract the opponent's id and attack move from
        // the underAttack string.
        StringTokenizer st = new StringTokenizer(underAttack, "_");
        String enemyID = st.nextToken();
        String enemyMove = st.nextToken();

        // Evaluate the player's and opponents move
        String result = computeResult(enemyMove, myMove);

        // Inform player of result of battle
        String resultMessage = enemyID + "_" + myID + "_" + result;
        textView.setText(resultMessage);


        // Depending on the computed battle result
        // If there was an error cancel result
        if (result.equalsIgnoreCase("ERROR")) {
            setResult(RESULT_CANCELED);
        // Otherwise, return the result message to the Battlefield Activity
        } else {
            Intent i = new Intent(this, BattlefieldActivity.class);
            i.putExtra("resultMessage", resultMessage);
            setResult(RESULT_OK, i);
        }
        // Call finish() when your activity is done and should be closed.
        // The ActivityResult is propagated back to whoever launched you via onActivityResult().
        finish();

    }
}
