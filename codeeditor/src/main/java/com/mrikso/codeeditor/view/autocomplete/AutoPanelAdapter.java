package com.mrikso.codeeditor.view.autocomplete;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrikso.codeeditor.R;
import com.mrikso.codeeditor.util.Flag;
import com.mrikso.codeeditor.view.FreeScrollingTextField;

import java.util.ArrayList;
import java.util.List;

import static com.mrikso.codeeditor.util.DLog.log;

/**
 * Adapter定义
 */
public class AutoPanelAdapter extends BaseAdapter implements Filterable {

    private final int PADDING = 20;
    private int _h;
    private Flag _abort;
    private DisplayMetrics dm;
    private List<ListItem> listItems;
    private Bitmap bitmap;
    private Context _context;
    private AutoCompletePanel mAutoComplete;
    private FreeScrollingTextField mTextFiled;

    public AutoPanelAdapter(Context context, AutoCompletePanel panel, FreeScrollingTextField textField) {
        _context = context;
        mAutoComplete = panel;
        mTextFiled = textField;
        _abort = new Flag();
        listItems = new ArrayList<>();
        dm = context.getResources().getDisplayMetrics();
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_method);
    }

    public void abort() {
        _abort.set();
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public ListItem getItem(int i) {
        return listItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View tempView = null;
        if (view == null) {
            View rootView = LayoutInflater.from(_context).inflate(R.layout.auto_panel_item, null);
            tempView = rootView;
        } else {
            tempView = view;
        }
        TextView textView = tempView.findViewById(R.id.auto_panel_text);
        ImageView imageView = tempView.findViewById(R.id.auto_panel_icon);
        String text = getItem(i).getText();
        SpannableString spannableString = null;
        ForegroundColorSpan foregroundColorSpan = null;
        //   Setting setting = Setting.getInstance(_context);
        log(text);
        if (text.contains("(")) {
            //函数
            ForegroundColorSpan argsForegroundColorSpan = null;
            spannableString = new SpannableString(text);
            //    if(setting.isDarkMode()) {
            //        foregroundColorSpan = new ForegroundColorSpan(Color.WHITE);
            //       argsForegroundColorSpan = new ForegroundColorSpan(Color.parseColor("#9C9C9C"));
            //     }
            //    else{
            foregroundColorSpan = new ForegroundColorSpan(Color.BLACK);
            argsForegroundColorSpan = new ForegroundColorSpan(Color.GRAY);
            //     }
            spannableString.setSpan(foregroundColorSpan, 0, text.indexOf('('), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(argsForegroundColorSpan, text.indexOf('('), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (text.contains("[keyword]")) {
            //关键字
            //    if(setting.isDarkMode()) {
            //       foregroundColorSpan = new ForegroundColorSpan(Color.YELLOW);
            //   }
            //     else{
            foregroundColorSpan = new ForegroundColorSpan(mAutoComplete._textColor);
            //    }
            int idx = text.indexOf("[keyword]");
            text = text.substring(0, idx);
            spannableString = new SpannableString(text);
            spannableString.setSpan(foregroundColorSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            //其他
            spannableString = new SpannableString(text);
            //    if(setting.isDarkMode()) {
            //          foregroundColorSpan = new ForegroundColorSpan(Color.WHITE);
            //    }
            //    else{
            foregroundColorSpan = new ForegroundColorSpan(mAutoComplete._textColor);
            //      }
            spannableString.setSpan(foregroundColorSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spannableString);
        imageView.setImageBitmap(getItem(i).getBitmap());
        return tempView;
    }

    public void restart() {
        _abort.clear();
    }

    /**
     * 计算列表高
     *
     * @return
     */
    public int getItemHeight() {
        if (_h != 0)
            return _h;
        LayoutInflater inflater = LayoutInflater.from(_context);
        View rootView = inflater.inflate(R.layout.auto_panel_item, null);
        rootView.measure(0, 0);
        _h = rootView.getMeasuredHeight();

        return _h;
    }

    /**
     * 实现自动完成的过滤算法
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            /**
             * 本方法在后台线程执行，定义过滤算法
             */
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // 此处实现过滤
                // 过滤后利用FilterResults将过滤结果返回
                ArrayList<String> buf = new ArrayList<String>();
                String input = String.valueOf(constraint).toLowerCase();

                String[] keywords = AutoCompletePanel._globalLanguage.getUserWord();
                for (String k : keywords) {
                    if (k.toLowerCase().startsWith(input))
                        buf.add(k);
                }
                keywords = AutoCompletePanel._globalLanguage.getKeywords();
                for (String k : keywords) {
                    if (k.indexOf(input) == 0)
                        buf.add(k);
                }
                keywords = AutoCompletePanel._globalLanguage.getNames();
                for (String k : keywords) {
                    if (k.toLowerCase().startsWith(input))
                        buf.add(k);
                }
                mAutoComplete._constraint = input;
                FilterResults filterResults = new FilterResults();
                filterResults.values = buf;   // results是上面的过滤结果
                filterResults.count = buf.size();  // 结果数量
                return filterResults;
            }

            /**
             * 本方法在UI线程执行，用于更新自动完成列表
             */
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0 && !_abort.isSet()) {
                    // 有过滤结果，显示自动完成列表
                    listItems.clear();   // 清空旧列表
                    ArrayList<String> stringArrayList = (ArrayList<String>) results.values;
                    for (int i = 0; i < stringArrayList.size(); i++) {
                        String itemText = stringArrayList.get(i);
                        if (itemText.contains("(")) {
                            listItems.add(new ListItem(bitmap, itemText));
                        } else {
                            listItems.add(new ListItem(null, itemText));
                        }
                    }
                    int y = mTextFiled.getCaretY() + mTextFiled.rowHeight() / 2 - mTextFiled.getScrollY();
                    mAutoComplete.setHeight(getItemHeight() * Math.min(2, results.count));

                    mAutoComplete.setHorizontalOffset(PADDING);
                    mAutoComplete.setWidth(mTextFiled.getWidth() - PADDING * 2);
                    mAutoComplete.setVerticalOffset(y - mTextFiled.getHeight());//_textField.getCaretY()-_textField.getScrollY()-_textField.getHeight());
                    notifyDataSetChanged();
                    mAutoComplete.show();
                } else {
                    // 无过滤结果，关闭列表
                    notifyDataSetInvalidated();
                }
            }

        };
        return filter;
    }
}
