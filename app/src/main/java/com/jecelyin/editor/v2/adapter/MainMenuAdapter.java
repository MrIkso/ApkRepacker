package com.jecelyin.editor.v2.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.duy.ide.editor.editor.R;
import com.jecelyin.editor.v2.common.Command;
import com.jecelyin.editor.v2.manager.MenuManager;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.jecelyin.editor.v2.widget.menu.MenuFactory;
import com.jecelyin.editor.v2.widget.menu.MenuGroup;
import com.jecelyin.editor.v2.widget.menu.MenuItemInfo;


import java.util.ArrayList;
import java.util.List;

public class MainMenuAdapter extends RecyclerView.Adapter {
    private static final int ITEM_TYPE_GROUP = 1;
    private final List<MenuItemInfo> menuItems;
    private final LayoutInflater inflater;
    private MenuItem.OnMenuItemClickListener menuItemClickListener;

    public MainMenuAdapter(Context context) {
        inflater = LayoutInflater.from(context);

        MenuFactory menuFactory = MenuFactory.getInstance(context);
        MenuGroup[] groups = new MenuGroup[]{MenuGroup.FILE, MenuGroup.EDIT, MenuGroup.VIEW, MenuGroup.OTHER};
        menuItems = new ArrayList<>();
        for (MenuGroup group : groups) {
            if (group == MenuGroup.TOP) {
                continue;
            }
            menuItems.add(new MenuItemInfo(group, 0, Command.CommandEnum.NONE, 0, 0));
            menuItems.addAll(menuFactory.getMenuItemsWithoutToolbarMenu(group));
        }
    }

    public void setMenuItemClickListener(MenuItem.OnMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return menuItems.get(position).getItemId() == 0 ? ITEM_TYPE_GROUP : super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_GROUP) {
            return new GroupViewHolder(inflater.inflate(R.layout.main_menu_group, parent, false));
        } else {
            return new ItemViewHolder(inflater.inflate(R.layout.main_menu_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MenuItemInfo item = menuItems.get(position);
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder vh = (ItemViewHolder) holder;
            vh.mTextView.setText(item.getTitleResId());
            Drawable icon = MenuManager.makeMenuNormalIcon(vh.itemView.getContext(), item.getIconResId());
            vh.mTextView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            vh.itemView.setOnClickListener(v -> {
                if (menuItemClickListener != null) {
                    menuItemClickListener.onMenuItemClick(item);
                    if (MenuFactory.isCheckboxMenu(item.getItemId())) {
                        vh.mCheckBox.setChecked(!vh.mCheckBox.isChecked());
                    }
                }
            });
            if (MenuFactory.isCheckboxMenu(item.getItemId())) {
                vh.mCheckBox.setVisibility(View.VISIBLE);
                vh.mCheckBox.setChecked(MenuFactory.isChecked(vh.itemView.getContext(), item.getItemId()));
            } else {
                vh.mCheckBox.setVisibility(View.GONE);
            }
        } else {
            GroupViewHolder vh = (GroupViewHolder) holder;
            vh.mNameTextView.setText(item.getGroup().getTitleId());
        }
    }

    @Override
    public int getItemCount() {
        return menuItems == null ? 0 : menuItems.size();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'main_menu_item.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        SwitchCompat mCheckBox;

        ItemViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.textView);
            mCheckBox = view.findViewById(R.id.checkbox);
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'main_menu_group.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView mNameTextView;

        GroupViewHolder(View view) {
            super(view);
            mNameTextView = view.findViewById(R.id.nameTextView);
        }
    }
}

