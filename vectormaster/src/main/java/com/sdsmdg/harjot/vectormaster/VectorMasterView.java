package com.sdsmdg.harjot.vectormaster;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sdsmdg.harjot.vectormaster.models.ClipPathModel;
import com.sdsmdg.harjot.vectormaster.models.GroupModel;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.sdsmdg.harjot.vectormaster.models.VectorModel;
import com.sdsmdg.harjot.vectormaster.utilities.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class VectorMasterView extends View {

    VectorModel vectorModel;
    Context context;

    Resources resources;
    int resID = -1;
    boolean useLegacyParser = true;
    private boolean useLightTheme = true;

    XmlPullParser xpp;

    String TAG = "VECTOR_MASTER";

    private Matrix scaleMatrix;

    int width = 0, height = 0;

    public int attrWidth = 0, attrHeight = 0;

    private float scaleRatio, strokeRatio;

    private InputStream vectorStream;
    private File vectorFile;

    private boolean isVector = false;

    public VectorMasterView(Context context) {
        this(context, (AttributeSet) null);
        this.context = context;
        init();
    }

    public VectorMasterView(Context context, AttributeSet attr) {
        this(context, attr, 0);
        this.context = context;
        init();
    }

    public VectorMasterView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        this.context = context;
        init();
    }


    public VectorMasterView(Context context, int resID) {
        this(context);
        this.context = context;
        this.resID = resID;
        init();
    }

    public VectorMasterView(Context context, File file) {
        this(context);
        this.context = context;
        this.vectorFile = file;
        init();
    }

    private void init() {
        resources = context.getResources();
        buildVectorModel();
    }

    void buildVectorModel() {

        if (vectorFile != null) {
            try {
                vectorStream = new FileInputStream(vectorFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (vectorStream != null) {
            try {
                XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                parserFactory.setNamespaceAware(true);
                xpp = parserFactory.newPullParser();
                xpp.setInput(vectorStream, "utf-8");
            } catch (XmlPullParserException xml) {
                Log.e(TAG, "Error from reading vector image");
                xml.printStackTrace();
                vectorModel = null;
                return;
            }

        } else if (resID != -1) {
            xpp = resources.getXml(resID);
        } else {
            vectorModel = null;
            return;
        }
        int tempPosition;
        PathModel pathModel = new PathModel();
        vectorModel = new VectorModel();
        GroupModel groupModel = new GroupModel();
        ClipPathModel clipPathModel = new ClipPathModel();
        Stack<GroupModel> groupModelStack = new Stack<>();

        try {
            int event = xpp.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = xpp.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        switch (name) {
                            case "vector":

                                isVector = true;

                                tempPosition = getAttrPosition(xpp, "viewportWidth");
                                vectorModel.setViewportWidth((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_VIEWPORT_WIDTH);

                                tempPosition = getAttrPosition(xpp, "viewportHeight");
                                vectorModel.setViewportHeight((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_VIEWPORT_HEIGHT);

                                tempPosition = getAttrPosition(xpp, "alpha");
                                vectorModel.setAlpha((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_ALPHA);
                                ////////////
//                                tempPosition = getAttrPosition(xpp, "tint");
//                                vectorModel.setTint((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition), useLightTheme) : DefaultValues.VECTOR_TINT);
                                ////////////
                                tempPosition = getAttrPosition(xpp, "name");
                                vectorModel.setName((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                                tempPosition = getAttrPosition(xpp, "width");
                                vectorModel.setWidth((tempPosition != -1) ? Utils.getFloatFromDimensionString(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_WIDTH);
                                attrWidth = (int) vectorModel.getWidth();

                                tempPosition = getAttrPosition(xpp, "height");
                                vectorModel.setHeight((tempPosition != -1) ? Utils.getFloatFromDimensionString(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_HEIGHT);
                                attrHeight = (int) vectorModel.getHeight();
                                break;
                            case "path":
                                pathModel = new PathModel();

                                tempPosition = getAttrPosition(xpp, "name");
                                pathModel.setName((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                                tempPosition = getAttrPosition(xpp, "fillAlpha");
                                pathModel.setFillAlpha((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_FILL_ALPHA);

                                tempPosition = getAttrPosition(xpp, "fillColor");
                                pathModel.setFillColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition), useLightTheme) : useLightTheme ? DefaultValues.PATH_FILL_COLOR_BLACK : DefaultValues.PATH_FILL_COLOR_WHITE);

                                tempPosition = getAttrPosition(xpp, "fillType");
                                pathModel.setFillType((tempPosition != -1) ? Utils.getFillTypeFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_FILL_TYPE);

                                tempPosition = getAttrPosition(xpp, "pathData");
                                if(tempPosition != -1){
                                    String patchData = xpp.getAttributeValue(tempPosition);
                                    if(!patchData.startsWith("@")){
                                    pathModel.setPathData(patchData);
                                   }
                                    else{
                                        //break;
                                        pathModel.setPathData("0");
                                    }

                                }
                                else {
                                    pathModel.setPathData(null);
                                }
                               // pathModel.setPathData((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                                tempPosition = getAttrPosition(xpp, "strokeAlpha");
                                pathModel.setStrokeAlpha((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_ALPHA);

                                tempPosition = getAttrPosition(xpp, "strokeColor");
                                pathModel.setStrokeColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition), useLightTheme) : DefaultValues.PATH_STROKE_COLOR);

                                tempPosition = getAttrPosition(xpp, "strokeLineCap");
                                pathModel.setStrokeLineCap((tempPosition != -1) ? Utils.getLineCapFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_LINE_CAP);

                                tempPosition = getAttrPosition(xpp, "strokeLineJoin");
                                pathModel.setStrokeLineJoin((tempPosition != -1) ? Utils.getLineJoinFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_LINE_JOIN);

                                tempPosition = getAttrPosition(xpp, "strokeMiterLimit");
                                pathModel.setStrokeMiterLimit((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_MITER_LIMIT);

                                tempPosition = getAttrPosition(xpp, "strokeWidth");
                                pathModel.setStrokeWidth((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_WIDTH);

                                tempPosition = getAttrPosition(xpp, "trimPathEnd");
                                pathModel.setTrimPathEnd((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_TRIM_PATH_END);

                                tempPosition = getAttrPosition(xpp, "trimPathOffset");
                                pathModel.setTrimPathOffset((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_TRIM_PATH_OFFSET);

                                tempPosition = getAttrPosition(xpp, "trimPathStart");
                                pathModel.setTrimPathStart((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_TRIM_PATH_START);

                                pathModel.buildPath(useLegacyParser);
                                break;
                            case "group":
                                groupModel = new GroupModel();

                                tempPosition = getAttrPosition(xpp, "name");
                                groupModel.setName((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                                tempPosition = getAttrPosition(xpp, "pivotX");
                                groupModel.setPivotX((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_PIVOT_X);

                                tempPosition = getAttrPosition(xpp, "pivotY");
                                groupModel.setPivotY((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_PIVOT_Y);

                                tempPosition = getAttrPosition(xpp, "rotation");
                                groupModel.setRotation((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_ROTATION);

                                tempPosition = getAttrPosition(xpp, "scaleX");
                                groupModel.setScaleX((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_SCALE_X);

                                tempPosition = getAttrPosition(xpp, "scaleY");
                                groupModel.setScaleY((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_SCALE_Y);

                                tempPosition = getAttrPosition(xpp, "translateX");
                                groupModel.setTranslateX((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_TRANSLATE_X);

                                tempPosition = getAttrPosition(xpp, "translateY");
                                groupModel.setTranslateY((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.GROUP_TRANSLATE_Y);

                                groupModelStack.push(groupModel);
                                break;
                            case "clip-path":
                                clipPathModel = new ClipPathModel();

                                tempPosition = getAttrPosition(xpp, "name");
                                clipPathModel.setName((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                                tempPosition = getAttrPosition(xpp, "pathData");
                                if(tempPosition != -1){
                                    String patchData = xpp.getAttributeValue(tempPosition);
                                    if(!patchData.startsWith("@")){
                                        clipPathModel.setPathData(patchData);
                                    }
                                    else {
                                        //break;
                                        clipPathModel.setPathData("0");
                                    }
                                }
                                else {
                                    clipPathModel.setPathData(null);
                                }
                               //clipPathModel.setPathData((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                                clipPathModel.buildPath(useLegacyParser);
                                break;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        switch (name) {
                            case "path":
                                if (groupModelStack.size() == 0) {
                                    vectorModel.addPathModel(pathModel);
                                } else {
                                    groupModelStack.peek().addPathModel(pathModel);
                                }
                                //if(pathModel.getPath() != null)
                                vectorModel.getFullpath().addPath(pathModel.getPath());
                                break;
                            case "clip-path":
                                //if(clipPathModel != null)
                                if (groupModelStack.size() == 0) {
                                    vectorModel.addClipPathModel(clipPathModel);
                                } else {
                                    groupModelStack.peek().addClipPathModel(clipPathModel);
                                }
                                break;
                            case "group":
                                GroupModel topGroupModel = groupModelStack.pop();
                                if (groupModelStack.size() == 0) {
                                    topGroupModel.setParent(null);
                                    vectorModel.addGroupModel(topGroupModel);
                                } else {
                                    topGroupModel.setParent(groupModelStack.peek());
                                    groupModelStack.peek().addGroupModel(topGroupModel);
                                }
                                break;
                            case "vector":
                                vectorModel.buildTransformMatrices();
                                break;
                        }
                        break;
                }
                event = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

    }

    int getAttrPosition(XmlPullParser xpp, String attrName) {
        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (xpp.getAttributeName(i).equals(attrName)) {
                return i;
            }
        }
        return -1;
    }

    public int getResID() {
        return resID;
    }

    public void setResID(int resID) {
        this.resID = resID;
    }

    public void setInputStream(InputStream stream) {
        this.vectorStream = stream;
        buildVectorModel();
        scaleMatrix = null;
    }

    public void setVectorFile(File file) {
        this.vectorFile = file;
        buildVectorModel();
        scaleMatrix = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0) {
            width = w;
            height = h;

            buildScaleMatrix();
            scaleAllPaths();
            scaleAllStrokes();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = canvas.getWidth();
        height = canvas.getHeight();

        if (vectorModel == null) {
            return;
        }

        setAlpha(Utils.getAlphaFromFloat(vectorModel.getAlpha()));

        vectorModel.drawPaths(canvas);
    }

    void buildScaleMatrix() {

        scaleMatrix = new Matrix();

        scaleMatrix.postTranslate(width / 2 - vectorModel.getViewportWidth() / 2, height / 2 - vectorModel.getViewportHeight() / 2);

        float widthRatio = width / vectorModel.getViewportWidth();
        float heightRatio = height / vectorModel.getViewportHeight();
        float ratio = Math.min(widthRatio, heightRatio);

        scaleRatio = ratio;

        scaleMatrix.postScale(ratio, ratio, width / 2, height / 2);
    }

    void scaleAllPaths() {
        vectorModel.scaleAllPaths(scaleMatrix);
    }

    void scaleAllStrokes() {
        strokeRatio = Math.min(width / vectorModel.getWidth(), height / vectorModel.getHeight());
        vectorModel.scaleAllStrokeWidth(strokeRatio);
    }

    public Path getFullPath() {
        if (vectorModel != null) {
            return vectorModel.getFullpath();
        }
        return null;
    }

    public GroupModel getGroupModelByName(String name) {
        GroupModel gModel;
        for (GroupModel groupModel : vectorModel.getGroupModels()) {
            if (Utils.isEqual(groupModel.getName(), name)) {
                return groupModel;
            } else {
                gModel = groupModel.getGroupModelByName(name);
                if (gModel != null)
                    return gModel;
            }
        }
        return null;
    }

    public PathModel getPathModelByName(String name) {
        PathModel pModel = null;
        for (PathModel pathModel : vectorModel.getPathModels()) {
            if (Utils.isEqual(pathModel.getName(), name)) {
                return pathModel;
            }
        }
        for (GroupModel groupModel : vectorModel.getGroupModels()) {
            pModel = groupModel.getPathModelByName(name);
            if (pModel != null && Utils.isEqual(pModel.getName(), name))
                return pModel;
        }
        return pModel;
    }

    public ClipPathModel getClipPathModelByName(String name) {
        ClipPathModel cModel = null;
        for (ClipPathModel clipPathModel : vectorModel.getClipPathModels()) {
            if (Utils.isEqual(clipPathModel.getName(), name)) {
                return clipPathModel;
            }
        }
        for (GroupModel groupModel : vectorModel.getGroupModels()) {
            cModel = groupModel.getClipPathModelByName(name);
            if (cModel != null && Utils.isEqual(cModel.getName(), name))
                return cModel;
        }
        return cModel;
    }

    public void update() {
        invalidate();
    }

    public boolean isVector() { return isVector; }

    public float getScaleRatio() {
        return scaleRatio;
    }

    public float getStrokeRatio() {
        return strokeRatio;
    }

    public Matrix getScaleMatrix() {
        return scaleMatrix;
    }

    private void setUseLightTheme(boolean useLightTheme) {
        this.useLightTheme = useLightTheme;
    }
}
