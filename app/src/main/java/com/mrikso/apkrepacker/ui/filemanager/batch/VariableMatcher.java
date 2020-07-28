package com.mrikso.apkrepacker.ui.filemanager.batch;

import android.content.Context;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariableMatcher {

    private Map<String, Variable> variableMap = new LinkedHashMap();

    public VariableMatcher(final VariableConfig variableConfig) {
        this.variableMap.put("#", new Variable() {
            private int startAt = variableConfig.mStartAt;
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                int i2;
                int length = sb.length();
                int i3 = 1;
                int i4 = 1;
                while (true) {
                    i2 = i + i3;
                    if (i2 >= length) {
                        break;
                    }
                    int i5 = i4 + 1;
                    if (sb.charAt(i4 + i) != '#') {
                        break;
                    }
                    i3++;
                    i4 = i5;
                }
                int i6 = this.startAt;
                this.startAt = i6 + 1;
                sb.replace(i, i2, String.format("%0" + i3 + "d", i6));
                return i3;
            }

            @Override
            public String describe() {
                return "#    - " + context.getString(R.string.variable_digit);
            }

            @Override
            public String pattern() {
                return "#";
            }
        });
        this.variableMap.put("N", new Variable() {
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                return VariableMatcher.applyString(sb, i, sEFile.getName());
            }

            @Override
            public String describe() {
                return "%N - " + context.getString(R.string.variable_file_name_with_extension);
            }

            @Override
            public String pattern() {
                return "%N";
            }
        });
        this.variableMap.put("n", new Variable() {
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                return VariableMatcher.applyString(sb, i, sEFile.getExtension());
            }

            @Override
            public String describe() {
                return "%n - " + context.getString(R.string.variable_file_name);
            }

            @Override
            public String pattern() {
                return "%n";
            }
        });
        this.variableMap.put("E", new Variable() {
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                return VariableMatcher.applyString(sb, i, sEFile.getExtension());
            }

            @Override
            public String describe() {
                return "%E - " + context.getString(R.string.variable_file_extension);
            }

            @Override
            public String pattern() {
                return "%E";
            }
        });
        this.variableMap.put("S", new Variable() {
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                return VariableMatcher.applyString(sb, i, sEFile.getFormattedSize(context, false));
            }

            @Override
            public String describe() {
                return "%S - " + context.getString(R.string.variable_file_size);
            }

            @Override
            public String pattern() {
                return "%S";
            }
        });
        this.variableMap.put("D", new Variable() {
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                return VariableMatcher.applyString(sb, i, sEFile.getFormattedModificationDate(context).toString());
            }

            @Override
            public String describe() {
                return "%D - " + context.getString(R.string.variable_file_date);
            }

            @Override
            public String pattern() {
                return "%D";
            }
        });
        this.variableMap.put("T", new Variable() {
            private Context context = variableConfig.mContext;
            @Override
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                return VariableMatcher.applyString(sb, i, sEFile.getFormattedHour(context).toString().replace(':', '-'));
            }

            @Override
            public String describe() {
                return "%T - " + context.getString(R.string.variable_file_time);
            }

            @Override
            public String pattern() {
                return "%T";
            }
        });
        /*this.a.put("V", new Variable() {
            

            @Override 
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                if (!FileTypeHelper.isAPK(sEFile.getName()) || !sEFile.isLocal()) {
                    return 0;
                }
                return VariableMatcher.applyString(sb, i, VariableMatcher.extractPackageInfo(sEFile).versionName);
            }

            @Override 
            public String describe() {
                return "%V - " + ResUtils.getString(R.string.variable_apk_version);
            }

            @Override 
            public String pattern() {
                return "%V";
            }
        });*/
        /*this.a.put("A", new Variable() {
            @Override 
            public int apply(StringBuilder sb, int i, FileHolder sEFile) {
                if (!FileTypeHelper.isAPK(sEFile.getName()) || !sEFile.isLocal()) {
                    return 0;
                }
                return VariableMatcher.applyString(sb, i, VariableMatcher.extractPackageInfo(sEFile).applicationInfo.loadLabel(SEApp.get().getPackageManager()).toString());
            }

            @Override 
            public String describe() {
                return "%A - " + ResUtils.getString(R.string.variable_apk_app_name);
            }

            @Override 
            public String pattern() {
                return "%A";
            }
        });*/
    }

    static int applyString(StringBuilder sb, int i, String str) {
        sb.replace(i, i + 2, str);
        return str.length();
    }

    /*static PackageInfo extractPackageInfo(FileHolder r4) {
        PackageInfo packageInfo = (PackageInfo) r4.getExtra("package_info");
        if (packageInfo != null) {
            return packageInfo;
        }
        PackageInfo packageArchiveInfo = SEApp.get().getPackageManager().getPackageArchiveInfo(r4.getPath(), 0);
        packageArchiveInfo.applicationInfo.sourceDir = r4.getPath();
        packageArchiveInfo.applicationInfo.publicSourceDir = r4.getPath();
        r4.putExtra("package_info", packageArchiveInfo);
        return packageArchiveInfo;
    }*/

    public Collection<Variable> getAll() {
        return this.variableMap.values();
    }

    public Variable match(String str) {
        return this.variableMap.get(str);
    }
}
