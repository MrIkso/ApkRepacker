package com.mrikso.patchengine;

import java.util.List;

/**
 * Класс в который добавляются результаты матчинга файлов
 */
public class Section {
    public int end;
    public int start;
    public List<String> groupStrs;

    public Section(int _start, int _end, List<String> _groupStrs) {
        this.start = _start;
        this.end = _end;
        this.groupStrs = _groupStrs;
    }

    /**
     * Возвращет последний инедекс матчинга
     * @return
     */
    public int getEnd() {
        return end;
    }
    /**
     * Возвращет первый инедекс матчинга
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     * Возвращет список групп матчинга
     * @return
     */
    public List<String> getGroupStrs() {
        return groupStrs;
    }
}