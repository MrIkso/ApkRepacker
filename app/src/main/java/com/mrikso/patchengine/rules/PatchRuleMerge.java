package com.mrikso.patchengine.rules;

import android.content.Context;
import android.util.SparseIntArray;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.resource.ResourceItem;
import com.mrikso.patchengine.resource.ResourceMerger;
import com.mrikso.patchengine.utils.IOUtil;
import com.mrikso.patchengine.utils.RandomHelper;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class PatchRuleMerge extends PatchRule {

    private static final String SOURCE = "SOURCE:";
    private static final String strEnd = "[/MERGE]";
    public SparseIntArray replacedIds;
    private String sourceFile;

    public PatchRuleMerge() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            String line2 = line.trim();
            if (!strEnd.equals(line2)) {
                if (!super.parseAsKeyword(line2, br)) {
                    if (SOURCE.equals(line2)) {
                        this.sourceFile = br.readLine().trim();
                    } else {
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
                    }
                }
                line = br.readLine();
            } else {
                return;
            }
        }
    }

    @Override
    public String executeRule(ProjectHelper activity, ZipFile patchZip, IPatchContext logger) {
        ZipEntry entry = patchZip.getEntry(this.sourceFile);
        if (entry == null) {
            logger.error(R.string.patch_error_no_entry, this.sourceFile);
            return null;
        }
        InputStream input = null;
        try {
            input = patchZip.getInputStream(entry);
            String path = IOUtil.makeDir("tmp/" + RandomHelper.getRandomString(6));
            FileOutputStream fos2 = new FileOutputStream(path);
            IOUtils.copy(input, fos2);
            fos2.close();
            mergeIds(activity.getProjectPath() + "/res/values/public.xml", path, activity.mContext);
            addFilesInZip(activity, path, new ResourceMerger(this, activity.getProjectPath()), logger);
        } catch (Exception e) {
            logger.error(R.string.general_error, e.getMessage());
        } catch (Throwable th) {
            closeQuietly(input);
            throw th;
        }
        closeQuietly(input);
        return null;
    }

    private void mergeIds(String curPublicXml, String zipFilepath, Context context) throws Exception {
        ZipFile zfile = null;
        InputStream input = null;
        FileInputStream fis = null;
        try {
            zfile = new ZipFile(zipFilepath);
            ZipEntry entry = zfile.getEntry("res/values/public.xml");
            if (entry != null) {
                input = zfile.getInputStream(entry);
                List<ResourceItem> addedItems = getResourceItems(input);
                fis = new FileInputStream(curPublicXml);
                this.replacedIds = refactorAddedItems(addedItems, getMaxIds(getResourceItems(fis)));
                writeAddedItems(curPublicXml, addedItems);
                return;
            }
            throw new Exception(context.getString(R.string.patch_error_publicxml_notfound));
        } finally {
            closeQuietly(fis);
            closeQuietly(input);
            closeQuietly(zfile);
        }
    }

    private void writeAddedItems(String curPublicXml, List<ResourceItem> addedItems) throws Exception {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < addedItems.size(); i++) {
            lines.add(addedItems.get(i).toString());
        }
        appendResourceLines(curPublicXml, lines);
    }

    public void appendResourceLines(String resourceFile, List<String> lines) throws Exception {
        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(resourceFile, "rw");
            long fileLength = randomFile.length();
            if (fileLength >= 16) {
                randomFile.seek(fileLength - 16);
                byte[] buffer = new byte[32];
                int readBytes = randomFile.read(buffer);
                int i = 0;
                while (true) {
                    if (i < readBytes) {
                        if (buffer[i] == 60 && buffer[i + 1] == 47) {
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
                randomFile.seek((fileLength - 16) + ((long) i));
                StringBuilder sb = new StringBuilder();
                for (int i2 = 0; i2 < lines.size(); i2++) {
                    sb.append(lines.get(i2));
                    sb.append("\n");
                }
                sb.append("</resources>");
                randomFile.write(sb.toString().getBytes());
                return;
            }
            throw new Exception("File is too small!");
        } finally {
            closeQuietly(randomFile);
        }
    }

    private SparseIntArray refactorAddedItems(List<ResourceItem> addedItems, Map<String, Integer> type2maxId) {
        SparseIntArray replaces = new SparseIntArray();
        for (int i = 0; i < addedItems.size(); i++) {
            ResourceItem item = addedItems.get(i);
            Integer curMaxId = type2maxId.get(item.getType());
            if (curMaxId != null) {
                int newId = curMaxId + 1;
                replaces.put(item.getId(), newId);
                item.setId(newId);
                type2maxId.put(item.getType(), newId);
            } else {
                int newId = ((getMaxType(type2maxId) + 1) << 16) + 2130706432;
                replaces.put(item.getId(), newId);
                item.setId(newId);
                type2maxId.put(item.getType(), newId);
            }
        }
        return replaces;
    }

    private int getMaxType(Map<String, Integer> type2maxId) {
        int maxType = 0;
        for (Integer val : type2maxId.values()) {
            int curType = val & 16711680;
            if (curType > maxType) {
                maxType = curType;
            }
        }
        return maxType >> 16;
    }

    private Map<String, Integer> getMaxIds(List<ResourceItem> items) {
        Map<String, Integer> maxIds = new HashMap<>();
        int drawableMaxId = 0;
        int layoutMaxId = 0;
        int stringMaxId = 0;
        for (ResourceItem item : items) {
            if ("drawable".equals(item.getType())) {
                if (item.getId() > drawableMaxId) {
                    drawableMaxId = item.getId();
                }
            } else if ("layout".equals(item.getType())) {
                if (item.getId() > layoutMaxId) {
                    layoutMaxId = item.getId();
                }
            } else if (!"string".equals(item.getType())) {
                Integer curMax = maxIds.get(item.getType());
                if (curMax == null || item.getId() > curMax) {
                    maxIds.put(item.getType(), item.getId());
                }
            } else if (item.getId() > stringMaxId) {
                stringMaxId = item.getId();
            }
        }
        maxIds.put("drawable", drawableMaxId);
        maxIds.put("layout", layoutMaxId);
        maxIds.put("string", stringMaxId);
        return maxIds;
    }

    private List<ResourceItem> getResourceItems(InputStream input) throws IOException {
        List<ResourceItem> result = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String readLine = br.readLine();
            if (readLine == null) {
                return result;
            }
            ResourceItem item = ResourceItem.parseFrom(readLine);
            if (item != null) {
                result.add(item);
            }
        }
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (this.sourceFile != null) {
            return true;
        }
        logger.error(R.string.patch_error_no_source_file);
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        return true;
    }

}
