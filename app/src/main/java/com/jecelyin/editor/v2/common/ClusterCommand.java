package com.jecelyin.editor.v2.common;

import com.mrikso.apkrepacker.ide.editor.EditorDelegate;

import java.util.ArrayList;

public class ClusterCommand {
    private final ArrayList<EditorDelegate> buffer;
    private Command command;

    public ClusterCommand(ArrayList<EditorDelegate> buffer) {
        this.buffer = buffer;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public void doNextCommand() {
        if (buffer == null || buffer.size() == 0) {
            return;
        }
        EditorDelegate editorFragment = buffer.remove(0);
        if (!editorFragment.doCommand(command)) {
            doNextCommand();
        }
    }
}
