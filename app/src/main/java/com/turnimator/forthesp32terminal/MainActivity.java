package com.turnimator.forthesp32terminal;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    String[] autoFillHints;
    EditText textURI;
    EditText textPort;
    LinearLayout mainLayout;
    LinearLayout buttonLayout;
    LinearLayout splitlayout;
    LinearLayout connectionLayout;
    LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

    LinearLayout leftSideListLayout, rightSideLayout;

    ToggleButton buttonConnect;
    ArrayList<Button> buttonList = new ArrayList<>();
    ArrayList<String> buttonCommandList = new ArrayList<>();

    ListView wordListView;
    CustomAutoCompleteTextView commandField;
    EditText responseView;

    ArrayList<String> wordList = new ArrayList<>();
    SeekBar seekBarSpeed;
    RadarView radarView;

    ArrayDeque<String> commandQ = new ArrayDeque<>();
    Thread commandTask;

    ArrayDeque<String> responseQ = new ArrayDeque<>();
    Thread responseTask;

    void getWords(CustomAutoCompleteTextView commandField) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wordList);
        wordListView.setAdapter(adapter);
        wordListView.setFocusable(true);
        autoFillHints = ForthCommunicationModel.send(textURI.getText().toString(), Integer.parseInt(textPort.getText().toString()), "words");
        for (int i = 0; i < autoFillHints.length; i++) {
            String[] sa = autoFillHints[i].split(" ");
            for (int j = 0; j < sa.length; j++) {
                if (sa[j].equals("ok")) {
                    continue;
                }
                if (sa[j].equals("-->")) {
                    continue;
                }
                adapter.add(sa[j]);
            }
        }
        commandField.setAdapter(adapter);
        commandField.setHint("1 1 + .");
        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String s = ((TextView) view).getText().toString();
                commandField.append(" " + s);
            }
        });
        commandField.setMinimumWidth(600);
        commandField.setMaxWidth(600);
        wordListView.setFocusable(true);

    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        radarView = new RadarView(this);


        splitlayout = new LinearLayout(this);
        splitlayout.setOrientation(LinearLayout.HORIZONTAL);

        leftSideListLayout = new LinearLayout(this);
        leftSideListLayout.setOrientation(LinearLayout.VERTICAL);
        rightSideLayout = new LinearLayout(this);
        rightSideLayout.setOrientation(LinearLayout.VERTICAL);

        connectionLayout = new LinearLayout(this);
        connectionLayout.setOrientation(LinearLayout.HORIZONTAL);

        buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        buttonLayout.addView(radarView);

        mainLayout.addView(connectionLayout);
        mainLayout.addView(buttonLayout);
        mainLayout.addView(splitlayout); // Divide rest of screen in two

        splitlayout.addView(leftSideListLayout);
        splitlayout.addView(rightSideLayout);

        wordListView = new ListView(this);
        rightSideLayout.addView(wordListView);
        rightSideLayout.setGravity(Gravity.RIGHT);

        // Connetion layout
        textURI = new EditText(this);
        textURI.setText("192.168.4.1");
        connectionLayout.addView(textURI);
        textPort = new EditText(this);
        textPort.setText("23");
        connectionLayout.addView(textPort);

        commandField = new CustomAutoCompleteTextView(this);
        leftSideListLayout.addView(commandField);

        responseView = new EditText(this);
        responseView.setMovementMethod(new ScrollingMovementMethod());
        leftSideListLayout.addView(responseView);

        seekBarSpeed = new SeekBar(this);

        LinearLayout.LayoutParams rightSideParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rightSideParams.gravity = Gravity.RIGHT;
        wordListView.setLayoutParams(rightSideParams);

        this.addContentView(mainLayout, verticalParams);

        commandTask = new Thread(new Runnable() {

            public synchronized void doCommand() {
                if (commandQ.isEmpty()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                String cmd = commandQ.removeLast();
                Log.d("Sending", cmd);
                String[] sa = ForthCommunicationModel.send(textURI.getText().toString(), Integer.parseInt(textPort.getText().toString()), cmd);
                for (int i = 0; i < sa.length; i++) {
                    responseQ.addFirst(sa[i]);
                }
                synchronized (responseTask) {
                    responseTask.notify();
                }
            }

            @Override
            public void run() {
                for (; ; ) {
                    doCommand();
                }
            }
        });

        commandTask.start();

        responseTask = new Thread(new Runnable() {
            public synchronized void doResponse() {
                for (; ; ) {
                    if (responseQ.isEmpty()) {
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    String s = responseQ.removeLast();
                    if (s.startsWith("@")) {
                        parseResponse(s);
                    }
                    if (s.equals("-->") || s.equals("-->  ok")) {
                        continue;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseView.append(s + "\n");
                        }
                    });
                }
            }

            @Override
            public void run() {
                for (; ; ) {
                    doResponse();
                }
            }
        });

        responseTask.start();

        commandField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String s = commandField.getText().toString(); //+ "\n"; // The newline is contained in the commandField! BUG BUG BUG
                commandField.setText("");
                if (s.startsWith("\\@")) {
                    parseCommand(s);
                    return true;
                }
                responseView.append(s + "\n");
                Log.d("Adding to commandQ", s);
                commandQ.addFirst(s);
                return true;
            }
        });

        responseView.setText("");
        getWords(commandField);
    }


    boolean parseCommand(String param) {
        boolean rv = false;
        String[] s = param.split(" ");
        if (param.toLowerCase(Locale.ROOT).equals("\\@clr") || s[0].toLowerCase(Locale.ROOT).equals("\\@clr")) {
            responseView.setText("");
            responseView.invalidate();
            return true;
        }
        if (param.toLowerCase(Locale.ROOT).equals("\\@words") || s[0].toLowerCase(Locale.ROOT).equals("\\@clr")) {
            getWords(commandField);
            return true;
        }
        if (s[0].toLowerCase(Locale.ROOT).equals("\\@button")) {
            rv = true;
            String text = s[1];

            String buttonCommand = s[2];
            for (int i = 3; i < s.length; i++) {
                buttonCommand += " " + s[i];
            }

            buttonCommandList.add(buttonCommand);
            int bci = buttonCommandList.indexOf(buttonCommand);

            Button b = new Button(this);
            buttonList.add(b);
            int bi = buttonList.indexOf(b);
            buttonList.get(bi).setText(text);
            buttonList.get(bi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    responseView.append(buttonCommandList.get(bci));
                    String[] sa = ForthCommunicationModel.send(textURI.getText().toString(), Integer.parseInt(textPort.getText().toString()), buttonCommandList.get(bci));
                    for (int i = 0; i < sa.length; i++) {
                        responseView.append(sa[i] + "\n");
                    }
                }
            });
            buttonLayout.addView(buttonList.get(bi));
            return true;
        }
        if (s[0].toLowerCase(Locale.ROOT).equals("\\@plot")) {
            float deg = 0;
            float dist = 0;
            try {
                deg = Float.parseFloat(s[1]);
                dist = Float.parseFloat(s[2]);
            } catch (Exception ex) {
                responseView.append(ex.toString());
                return true;
            }
            radarView.plotPolar(deg, dist);

            return true;
        }
        if (s[0].toLowerCase(Locale.ROOT).equals("\\@rot")) {
            double angle = 0;
            try {
                angle = Float.parseFloat(s[1]);
            } catch (Exception ex) {
                responseView.append(ex.toString());
                return true;
            }
            radarView.rotate(angle);

            return true;
        }

        return false;
    }


    boolean parseResponse(String param) {
        boolean rv = false;
        String[] s = param.split(" ");
        if (param.toLowerCase(Locale.ROOT).equals("@clr")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    responseView.setText("");
                    responseView.invalidate();
                }
            });
            return true;
        }

        if (s[0].toLowerCase(Locale.ROOT).equals("@plot")) {
            double deg = 0.0;
            float dist = 0.0F;
            try {
                deg = Float.parseFloat(s[1]);
                dist = Float.parseFloat(s[2]);
            } catch (Exception ex) {
                Log.d("parseResponse", ex.toString());
            }
            Log.d("ParseResponse", "plotPolar(" + deg + ", " + dist + ")");
            double finalDeg = deg;
            float finalDist = dist;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    radarView.plotPolar(finalDeg, finalDist);
                }
            });

            return true;
        }

        if (s[0].toLowerCase(Locale.ROOT).equals("@rotate")) {
            float deg = Float.parseFloat(s[1]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    radarView.rotate(deg);
                }
            });

            return true;
        }

        if (s[0].toLowerCase(Locale.ROOT).equals("@button")) {
            rv = true;
            String text = s[1];

            String buttonCommand = s[2];
            for (int i = 3; i < s.length; i++) {
                buttonCommand += " " + s[i];
            }
            String finalButtonCommand = buttonCommand;
            Context o = this;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonCommandList.add(finalButtonCommand);
                    int bci = buttonCommandList.indexOf(finalButtonCommand);

                    Button b = new Button(o);
                    buttonList.add(b);
                    int bi = buttonList.indexOf(b);
                    buttonList.get(bi).setText(text);
                    buttonList.get(bi).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            responseView.append(buttonCommandList.get(bci));
                            String[] sa = ForthCommunicationModel.send(textURI.getText().toString(), Integer.parseInt(textPort.getText().toString()), buttonCommandList.get(bci));
                            for (int i = 0; i < sa.length; i++) {
                                responseView.append(sa[i] + "\n");
                            }
                        }
                    });
                    buttonLayout.addView(buttonList.get(bi));

                }
            });


        }
        return rv;
    }

}