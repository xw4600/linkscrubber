package com.linkscrubber;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private List<String> urls;
    private String cleanedUrl;
    private TextView message;
    private TextView cleanedUrlView;
    private TextView note;
    private ListView pickList;
    private View resultPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        cleanedUrlView = findViewById(R.id.cleanedUrl);
        note = findViewById(R.id.note);
        pickList = findViewById(R.id.pickList);
        resultPanel = findViewById(R.id.resultPanel);

        findViewById(R.id.btnShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCleaned();
            }
        });
        findViewById(R.id.btnCopy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyCleaned();
            }
        });
        findViewById(R.id.btnOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCleaned();
            }
        });
        findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        urls = Scrubber.findUrls(extractText(getIntent()));
        if (urls.isEmpty()) {
            showMessage(R.string.no_link);
        } else if (urls.size() == 1) {
            showResult(urls.get(0));
        } else {
            showPicker();
        }
    }

    private static String extractText(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            return text == null ? null : text.toString();
        }
        return null;
    }

    private void showMessage(int resId) {
        message.setText(resId);
        message.setVisibility(View.VISIBLE);
        pickList.setVisibility(View.GONE);
        resultPanel.setVisibility(View.GONE);
    }

    private void showPicker() {
        message.setText(R.string.pick_link);
        message.setVisibility(View.VISIBLE);
        pickList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, urls));
        pickList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showResult(urls.get(position));
            }
        });
        pickList.setVisibility(View.VISIBLE);
        resultPanel.setVisibility(View.GONE);
    }

    private void showResult(String raw) {
        Scrubber.Result result = Scrubber.scrub(raw);
        cleanedUrl = result.url;
        cleanedUrlView.setText(result.url);
        note.setText(result.note == null ? getString(R.string.no_query) : result.note);
        message.setVisibility(View.GONE);
        pickList.setVisibility(View.GONE);
        resultPanel.setVisibility(View.VISIBLE);
        copyCleaned();
    }

    private void copyCleaned() {
        ClipboardManager clipboard = getSystemService(ClipboardManager.class);
        clipboard.setPrimaryClip(ClipData.newPlainText("LinkScrubber", cleanedUrl));
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    private void shareCleaned() {
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, cleanedUrl);
        Intent chooser = Intent.createChooser(send, getString(R.string.share_title));
        chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS,
                new ComponentName[] { new ComponentName(this, MainActivity.class) });
        startActivity(chooser);
    }

    private void openCleaned() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cleanedUrl)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show();
        }
    }
}
