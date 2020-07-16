/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mrikso.apkrepacker.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ide.editor.lexer.XmlLexTask;
import com.mrikso.apkrepacker.ide.editor.theme.ThemeLoader;
import com.mrikso.apkrepacker.ide.editor.theme.model.EditorTheme;
import com.mrikso.apkrepacker.ide.editor.view.CodeEditor;
import com.mrikso.apkrepacker.ide.editor.view.IEditAreaView;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class EditorThemeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private EditorThemeAdapter mEditorThemeAdapter;
    private EditorPreferences mPreferences;
    private ProgressBar mProgressBar;
    private LoadThemeTask mLoadThemeTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_code_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreferences = EditorPreferences.getInstance(getContext());
        /*
        mProgressBar = view.findViewById(R.id.progress_bar);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        new FastScrollerBuilder(mRecyclerView).build();
        mEditorThemeAdapter = new EditorThemeAdapter(getContext());
        mEditorThemeAdapter.setOnThemeSelectListener((EditorThemeAdapter.OnThemeSelectListener) getActivity());
        mRecyclerView.setAdapter(mEditorThemeAdapter);

        loadData();

         */
    }

    private void loadData() {
        mLoadThemeTask = new LoadThemeTask(getContext());
        mLoadThemeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroyView() {
        if (mLoadThemeTask != null) {
            mLoadThemeTask.cancel(true);
        }
        super.onDestroyView();
    }

    private int findThemeIndex(EditorTheme editorTheme) {
        int position = mEditorThemeAdapter.getPosition(editorTheme);
        return Math.max(position, 0);
        //return position;
    }

    public static class EditorThemeAdapter extends RecyclerView.Adapter<EditorThemeAdapter.ViewHolder> {
        private final ArrayList<EditorTheme> mEditorThemes;
        private Context mContext;
        private OnThemeSelectListener onThemeSelectListener;
      //  private Mode mLanguage;
        private String mSampleCode;

        EditorThemeAdapter(Context context) {
            mContext = context;
            mEditorThemes = new ArrayList<>();
            resolveLanguage();
        }

        private void resolveLanguage() {
            if (mSampleCode == null) {
                try {
                    String fileName = "templates/java.template";
                 //   mLanguage = Catalog.getModeByName("Java");
                    InputStream input = mContext.getAssets().open(fileName);
                    mSampleCode = IOUtils.toString(input, StandardCharsets.UTF_8);
                    mSampleCode = mSampleCode.replace("\r\n", "\n");
                    mSampleCode = mSampleCode.replace("\r", "\n");
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        int getPosition(EditorTheme editorTheme) {
            return mEditorThemes.indexOf(editorTheme);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theme, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("UseSparseArrays")
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final EditorTheme editorTheme = mEditorThemes.get(position);

            final String title = makeTitle(position, editorTheme);
            holder.mTxtName.setText(title);
            IEditAreaView editorView = holder.mEditorView;

            editorView.setTheme(editorTheme);

            String sampleData = getSampleData();

            editorView.setText("sampleData");
            editorView.setEnabled(true);
            editorView.setLexTask(new XmlLexTask());
            editorView.setReadOnly(true);
          //  editorView.requestFocus();
            holder.mBtnSelect.setOnClickListener(v -> {
                if (onThemeSelectListener != null) {
                    onThemeSelectListener.onEditorThemeSelected(editorTheme);
                }
            });
        }

        private String makeTitle(int position, EditorTheme editorTheme) {
            return (position + 1) + ". " + editorTheme.getThemeModel().getThemeName();
        }

        @Override
        public int getItemCount() {
            return mEditorThemes.size();
        }

        private String getSampleData() {

            if (mSampleCode == null) {
                mSampleCode = "Constants.C_PLUS_PLUS_SAMPLE";
            }
            return mSampleCode;
        }

        public void setOnThemeSelectListener(OnThemeSelectListener onThemeSelectListener) {
            this.onThemeSelectListener = onThemeSelectListener;
        }

        public void addTheme(EditorTheme theme) {
            mEditorThemes.add(theme);
            notifyItemInserted(mEditorThemes.size() - 1);
        }

        public interface OnThemeSelectListener {
            void onEditorThemeSelected(EditorTheme theme);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            AppCompatButton mBtnSelect;
            CodeEditor mEditorView;
            TextView mTxtName;

            ViewHolder(View itemView) {
                super(itemView);
                setIsRecyclable(false);
                mEditorView = itemView.findViewById(R.id.editor_view);
                mEditorView.setReadOnly(true);
                mTxtName = itemView.findViewById(R.id.txt_name);
                mBtnSelect = itemView.findViewById(R.id.btn_select);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadThemeTask extends AsyncTask<Void, EditorTheme, Void> {
        private static final String TAG = "LoadThemeTask";
        private Context context;
        private AssetManager mAssetManager;

        LoadThemeTask(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAssetManager = getContext().getAssets();

            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String[] list = mAssetManager.list(ThemeLoader.ASSET_PATH);
                Arrays.sort(list);
                for (String name : list) {
                    if (isCancelled()) {
                        return null;
                    }
                    Thread.sleep(1);
                    EditorTheme theme = ThemeLoader.getTheme(context, name);
                    publishProgress(theme);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(EditorTheme... themes) {
            super.onProgressUpdate(themes);
            try {
                mEditorThemeAdapter.addTheme(themes[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled()) {
                return;
            }
            mRecyclerView.scrollToPosition(findThemeIndex(mPreferences.getEditorTheme()));
            mProgressBar.setVisibility(View.GONE);

        }
    }
}
