package com.app.figpdfconvertor.figpdf.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.ads.AdManagerNative;
import com.app.figpdfconvertor.figpdf.databinding.ActivityLanguageBinding;
import com.app.figpdfconvertor.figpdf.preferences.AppHelper;
import com.app.figpdfconvertor.figpdf.utils.DoubleClickListener;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.app.figpdfconvertor.figpdf.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.Locale;

public class LanguageActivity extends BaseActivity {

    private ActivityLanguageBinding binding;
    private String selectedLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        selectedLanguageCode = AppHelper.getLanguageCode(); // load default saved language

        if (AppHelper.isFirstTime()) {
            binding.nextTxt.setText(R.string.next);
        } else {
            binding.nextTxt.setText(R.string.save);
        }
        if (AppHelper.getShowNativeLanguage()) {
            AdManagerNative.renderNativeAdLarge(this, findViewById(R.id.layLargeNative), findViewById(R.id.layNative));
        } else {
            findViewById(R.id.layNative).setVisibility(View.GONE);
        }
        setupBackPressed();
        setupRecyclerView();
        buttonClickListener();
    }

    private void setupRecyclerView() {
        String savedLanguageCode = AppHelper.getLanguageCode();
        ArrayList<Language> languageList = getLanguageList(savedLanguageCode);

        binding.languageRv.setLayoutManager(new LinearLayoutManager(this));
        AdapterLanguage adapter = new AdapterLanguage(this, languageList, selectedLanguage -> {
            selectedLanguageCode = selectedLanguage.languageCode;
        });
        binding.languageRv.setAdapter(adapter);
    }

    private void buttonClickListener() {
        binding.nextTxt.setOnClickListener(v -> {
            saveSelectedLanguage();
            navigateToNextScreen();
        });
    }

    private void saveSelectedLanguage() {
        AppHelper.setLanguageCode(selectedLanguageCode);
        LocaleHelper.setLocale(this, AppHelper.getLanguageCode());
    }

    private void navigateToNextScreen() {
        if (AppHelper.isFirstTime()) {
            AppHelper.setFirstTime(false);
            startActivity(new Intent(LanguageActivity.this, MainActivity.class));
        } else {
            Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    public class Language {
        public String languageCode;
        public int languageIcon;
        public String languageName;
        public boolean isSelected;

        public Language(String languageCode, int languageIcon, String languageName, boolean isSelected) {
            this.languageCode = languageCode;
            this.languageIcon = languageIcon;
            this.languageName = languageName;
            this.isSelected = isSelected;
        }
    }

    private ArrayList<Language> getLanguageList(String savedLanguageCode) {
        ArrayList<Language> languages = new ArrayList<>();
        languages.add(new Language("en", R.drawable.language_english_img, "English", savedLanguageCode.equals("en")));
        languages.add(new Language("hi", R.drawable.language_hindi_img, "Hindi", savedLanguageCode.equals("hi")));
        languages.add(new Language("es", R.drawable.language_spanish_img, "Spanish", savedLanguageCode.equals("es")));
        languages.add(new Language("fr", R.drawable.language_french_img, "French", savedLanguageCode.equals("fr")));
        languages.add(new Language("de", R.drawable.language_german_img, "German", savedLanguageCode.equals("de")));
        languages.add(new Language("pt", R.drawable.language_portuguese_img, "Portuguese", savedLanguageCode.equals("pt")));
        languages.add(new Language("th", R.drawable.language_thai_img, "Thai", savedLanguageCode.equals("th")));
        languages.add(new Language("zh", R.drawable.language_chinese_img, "Chinese", savedLanguageCode.equals("zh")));
        languages.add(new Language("ja", R.drawable.language_japanese_img, "Japanese", savedLanguageCode.equals("ja")));
        languages.add(new Language("ru", R.drawable.language_russian_img, "Russian", savedLanguageCode.equals("ru")));
        languages.add(new Language("vi", R.drawable.language_vietnamese_img, "Vietnamese", savedLanguageCode.equals("vi")));
        languages.add(new Language("tr", R.drawable.language_turkish_img, "Turkish", savedLanguageCode.equals("tr")));
        languages.add(new Language("bn", R.drawable.language_turkish_img, "Bengali", savedLanguageCode.equals("bn")));
        languages.add(new Language("in", R.drawable.language_turkish_img, "Indonesian", savedLanguageCode.equals("in")));
        languages.add(new Language("it", R.drawable.language_turkish_img, "Italian", savedLanguageCode.equals("it")));
        languages.add(new Language("ko", R.drawable.language_turkish_img, "Korean", savedLanguageCode.equals("ko")));
        languages.add(new Language("pl", R.drawable.language_turkish_img, "Polish", savedLanguageCode.equals("pl")));
        languages.add(new Language("nl", R.drawable.language_turkish_img, "Dutch", savedLanguageCode.equals("nl")));
        languages.add(new Language("ms", R.drawable.language_turkish_img, "Malay", savedLanguageCode.equals("ms")));
        languages.add(new Language("fil", R.drawable.language_turkish_img, "Filipino", savedLanguageCode.equals("fil")));
        return languages;
    }

    public static class AdapterLanguage extends RecyclerView.Adapter<AdapterLanguage.ViewHolder> {

        private final Context context;
        private final ArrayList<Language> languageList;
        private final LanguageListener languageListener;
        private String langCode;

        public AdapterLanguage(Context context, ArrayList<Language> languageList, LanguageListener languageListener) {
            this.context = context;
            this.languageList = languageList;
            this.languageListener = languageListener;
            this.langCode = AppHelper.getLanguageCode();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Language language = languageList.get(position);
            boolean isSelected = langCode.equals(language.languageCode);

            holder.languageSelectImg.setImageDrawable(ContextCompat.getDrawable(context,
                    isSelected ? R.drawable.lang_selected_img : R.drawable.lang_unselected_img));

            holder.languageFlagImg.setImageResource(language.languageIcon);
            holder.languageNameTxt.setText(language.languageName);

            Locale locale = new Locale(language.languageCode);
            holder.languageTitleTxt.setText(String.format("%s", locale.getDisplayLanguage(locale)));

            holder.itemView.setOnClickListener(view -> {
                langCode = language.languageCode;
                notifyDataSetChanged();
                languageListener.onClick(language);
            });
        }

        @Override
        public int getItemCount() {
            return languageList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private final AppCompatImageView languageFlagImg;
            private final AppCompatImageView languageSelectImg;
            private final TextView languageNameTxt;
            private final TextView languageTitleTxt;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                languageFlagImg = itemView.findViewById(R.id.languageFlagImg);
                languageSelectImg = itemView.findViewById(R.id.languageSelectImg);
                languageNameTxt = itemView.findViewById(R.id.languageNameTxt);
                languageTitleTxt = itemView.findViewById(R.id.languageTitleTxt);
            }
        }
    }

    public interface LanguageListener {
        void onClick(Language modelLanguage);
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            boolean doubleBackToExitPressedOnce = false;

            @Override
            public void handleOnBackPressed() {
                if (AppHelper.isFirstTime()) {
                    if (doubleBackToExitPressedOnce) {
                        finishAffinity();
                        System.exit(0);
                    } else {
                        doubleBackToExitPressedOnce = true;
                        Toast.makeText(LanguageActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                        new Handler(Looper.getMainLooper()).postDelayed(() ->
                                doubleBackToExitPressedOnce = false, 2000);
                    }
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        binding.backKey.setOnClickListener(new DoubleClickListener() {
            @Override
            public void performClick(View v) {
                callback.handleOnBackPressed();
            }
        });
    }
}