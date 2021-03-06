package com.example.dms.dmsa2;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BattlefieldActivity extends Activity
        implements OnClickListener {
    private static final int MAX_RECEIVED_DISPLAY = 5;
    private BattlePlayer battlePlayer;
    private Button readyButton, test0, test1, test2;
    private TextView updateTextView, currentStats, previousAction;
    private List<String> receivedMessages;
    private String hpLable, actionLable;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battlefield);
        // obtain the battlePlayer passed in with the intent
        Intent intent = getIntent();
        battlePlayer = (BattlePlayer) intent.getExtras().get
                (BattlePlayer.class.getName());
        readyButton = (Button) findViewById(R.id.bReady);
        readyButton.setOnClickListener(this);
        updateTextView = (TextView) findViewById(R.id.tvUpdates);
        receivedMessages = new ArrayList<String>();
        test0 = (Button) findViewById(R.id.bTest0);
        test1 = (Button) findViewById(R.id.bTest1);
        test2 = (Button) findViewById(R.id.bTest2);
        test0.setOnClickListener(this);
        test1.setOnClickListener(this);
        test2.setOnClickListener(this);

        currentStats = (TextView) findViewById(R.id.tvHP);
        previousAction = (TextView) findViewById(R.id.tvStatus);
        hpLable = "" + battlePlayer.MAX_HP;
        actionLable = battlePlayer.getPlayerName() + ", you are in perfect condition.";

        currentStats.setText("Your current HP: " + hpLable);
        previousAction.setText(actionLable);
        battlePlayer.registerActivity(this);
        Thread thread = new Thread(battlePlayer);
        thread.start();
    }

    public void onStart() {
        super.onStart();
//        battlePlayer.registerActivity(this);
//        Thread thread = new Thread(battlePlayer);
//        thread.start();
    }

    public void onStop() {
        super.onStop();
//        battlePlayer.stop();
//        battlePlayer.registerActivity(null);
    }

    public synchronized void receivedUpdate(String message) {
        receivedMessages.add(0, message);
        final String update = interpret(message);

        updateTextView.post(new Runnable() {
            public void run() {
//                updateTextView.setText(stringBuilder.toString());
                updateTextView.setText(update + "\n" + updateTextView.getText().toString());
                currentStats.setText("Your current HP: " + hpLable);
                previousAction.setText(actionLable);
            }
        });

    }

    private String interpret(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, "_");
        String type = tokenizer.nextToken();
        if (type.equalsIgnoreCase("update")) {
            String attackerID = tokenizer.nextToken();
            String attackerHP = tokenizer.nextToken();
            String enemyID = tokenizer.nextToken();
            String enemyHP = tokenizer.nextToken();
            String result = tokenizer.nextToken().toLowerCase();
            if (battlePlayer.getPlayerName().equals(attackerID)) {
                attackerID = "YOU";
                hpLable = attackerHP;
            }
            if (battlePlayer.getPlayerName().equals(enemyID)) {
                enemyID = "YOU";
                hpLable = enemyHP;
            }
            String translation = "";

            switch (result) {
                case "hit":
                    translation = attackerID + "(" + attackerHP + ")" + " attacked " + enemyID + "(" + enemyHP + ")";
                    break;
                case "counter":
                    translation = enemyID + "(" + enemyHP + ")" + " counterattacked " + attackerID + "(" + attackerHP + ")";
                    break;
                case "missed":
                    translation = enemyID + "(" + enemyHP + ")" + " dodged the attack from " + attackerID + "(" + attackerHP + ")";
                    break;
                default:
                    translation = "Something strange thing just happened in the Battlefield!!";
                    break;
            }
            if (attackerID.equals("YOU") || enemyID.equals("YOU")) {
                actionLable = translation;
            }

            return translation;
        } else
            return s;


    }

    // implementation of OnClickListener method
    public void onClick(View view) {
        if (view == readyButton) {

            Intent intent = new Intent(this, BattleAction.class);
            startActivityForResult(intent, 0);
            Toast.makeText(getApplicationContext(),
                    "Open action activity", Toast.LENGTH_SHORT).show();


        } else if (view == test0) {

            String result = "hit";
            String rMessage = battlePlayer.getPlayerName() + "_other_" + result;
            battlePlayer.forward(rMessage);
//            updateTextView.setText(rMessage);
            Toast toastMessage = Toast.makeText(getApplicationContext(),
                    rMessage, Toast.LENGTH_SHORT);
            toastMessage.show();

        } else if (view == test1) {

            String result = "counter";
            String rMessage = battlePlayer.getPlayerName() + "_other_" + result;
            battlePlayer.forward(rMessage);
//            updateTextView.setText(rMessage);
            Toast toastMessage = Toast.makeText(getApplicationContext(),
                    rMessage, Toast.LENGTH_SHORT);
            toastMessage.show();

        } else if (view == findViewById(R.id.bTest2)) {

            String result = "missed";
            String rMessage = battlePlayer.getPlayerName() + "_other_" + result;
            battlePlayer.forward(rMessage);
//            updateTextView.setText(rMessage);
            Toast toastMessage = Toast.makeText(getApplicationContext(),
                    rMessage, Toast.LENGTH_SHORT);
            toastMessage.show();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent i) {
        Log.e("rrrrr"," get response++++++  "+(i == null));
        if (i != null) {
            Bundle bun = i.getExtras();
            if (reqCode == 0 && resCode == RESULT_OK) {
                Log.e("rrrrr"," get response++++++  "+(bun.getString("resultMessage")));
                battlePlayer.forward(bun.getString("resultMessage"));
            }
        }
    }
}
