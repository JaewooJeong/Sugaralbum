
package com.kiwiple.imageframework.network;

public class ProtocolParam {
    private static final int PROT_PARAM_INT = 0;
    private static final int PROT_PARAM_FLOAT = 1;
    private static final int PROT_PARAM_STRING = 2;
    private static final int PROT_PARAM_BOOLEAN = 3;
    private static final int PROT_PARAM_FILE = 4;

    int mType = PROT_PARAM_INT;

    String mName;
    int mIntValue;
    double mFloatValue;
    String mStringValue;
    boolean mBooleanValue;

    boolean mIsImage = false;

    public ProtocolParam(String name, int n) {
        mName = name;
        mType = PROT_PARAM_INT;
        mIntValue = n;
    }

    public ProtocolParam(String name, double n) {
        mName = name;
        mType = PROT_PARAM_FLOAT;
        mFloatValue = n;
    }

    public ProtocolParam(String name, String n) {
        mName = name;
        mType = PROT_PARAM_STRING;

        if(n == null || n.length() == 0) {
            mStringValue = "NULL";
        } else {
            mStringValue = n;
        }
    }

    public ProtocolParam(String name, boolean n) {
        mName = name;
        mType = PROT_PARAM_BOOLEAN;

        mBooleanValue = n;
    }

    public ProtocolParam(String name, String filename, boolean isImage) {
        mName = name;
        mType = PROT_PARAM_FILE;
        mIsImage = isImage;
        mStringValue = filename;
    }

    public boolean isInt() {
        if(mType == PROT_PARAM_INT) {
            return true;
        }
        return false;
    }

    public boolean isFloat() {
        if(mType == PROT_PARAM_FLOAT) {
            return true;
        }
        return false;
    }

    public boolean isString() {
        if(mType == PROT_PARAM_STRING) {
            return true;
        }
        return false;
    }

    public boolean isBoolean() {
        if(mType == PROT_PARAM_BOOLEAN) {
            return true;
        }
        return false;
    }

    public boolean isFile() {
        if(mType == PROT_PARAM_FILE) {
            return true;
        }
        return false;
    }

    public boolean isImageFile() {
        return mIsImage;
    }

    public int intValue() {
        return mIntValue;
    }

    public double floatValue() {
        return mFloatValue;
    }

    public String stringValue() {
        return mStringValue;
    }

    public boolean booleanValue() {
        return mBooleanValue;
    }

    public String val() {
        if(isString()) {
            return mStringValue;
        } else if(isFloat()) {
            return Double.valueOf(mFloatValue).toString();
        } else if(isInt()) {
            return Integer.valueOf(mIntValue).toString();
        } else if(isBoolean()) {
            return Boolean.valueOf(mBooleanValue).toString();
        } else if(isFile()) {
            return mStringValue;
        } else {
            return new String();
        }
    }

    public String name() {
        return mName;
    }

    /**
     * Desc : GET_METHOD���� url�� param ���·� ����.
     * 
     * @Method Name : bufferGETMethod
     * @return
     */
    public StringBuffer bufferGETMethod() {
        StringBuffer buffer = new StringBuffer();

        String v;
        v = val();
        buffer.append('/').append(mName).append('/').append(v);

        return buffer;
    }
}
