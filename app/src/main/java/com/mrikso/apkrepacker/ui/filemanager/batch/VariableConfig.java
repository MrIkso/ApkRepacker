package com.mrikso.apkrepacker.ui.filemanager.batch;

import android.content.Context;

public class VariableConfig {
    public int mStartAt = 0;
    public Context mContext;

    public static class Builder {
        private VariableConfig variableConfig = new VariableConfig();

        public VariableConfig build() {
            return this.variableConfig;
        }

        public Builder withNumberingStartAt(int i) {
            this.variableConfig.mStartAt = i;
            return this;
        }

        public Builder setContext(Context context) {
            this.variableConfig.mContext = context;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
