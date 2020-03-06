package com.openspark.sqlstream.util;


import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DtStringUtil {

    public static List<String> splitIgnoreQuota(String str, char delimiter) {
        List<String> tokensList = new ArrayList<>();
        boolean inQuotes = false;
        boolean inSingleQuotes = false;
        StringBuilder b = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c == delimiter) {
                if (inQuotes) {
                    b.append(c);
                } else if (inSingleQuotes) {
                    b.append(c);
                } else {
                    tokensList.add(b.toString());
                    b = new StringBuilder();
                }
            } else if (c == '\"') {
                inQuotes = !inQuotes;
                b.append(c);
            } else if (c == '\'') {
                inSingleQuotes = !inSingleQuotes;
                b.append(c);
            } else {
                b.append(c);
            }
        }

        tokensList.add(b.toString());

        return tokensList;
    }

    /***
     * Split the specified string delimiter --- ignored in brackets and quotation marks delimiter
     * @param str
     * @param delimter
     * @return
     */
    public static String[] splitIgnoreQuotaBrackets(String str, String delimter) {
        String splitPatternStr = delimter + "(?![^()]*+\\))(?![^{}]*+})(?![^\\[\\]]*+\\])(?=(?:[^\"]|\"[^\"]*\")*$)";
        return str.split(splitPatternStr);
    }


    public static String upperCaseFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    public static <E> HashSet<E> newHashSet(E... elements) {
        checkNotNull(elements);
        HashSet<E> list = new HashSet(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> List<E> newArrayList(E... elements) {
        checkNotNull(elements);
        List<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0;
    }

    public static DataType strConverDataType(String filedType) {
        switch (filedType) {
            case "boolean":
                return DataTypes.BooleanType;
            case "int":
                return DataTypes.IntegerType;

            case "bigint":
                return DataTypes.LongType;

            case "tinyint":
            case "byte":
                return DataTypes.ByteType;

            case "short":
            case "smallint":
                return DataTypes.ShortType;

            case "char":
            case "varchar":
            case "string":
                return DataTypes.StringType;

            case "float":
                return DataTypes.FloatType;

            case "double":
                return DataTypes.DoubleType;

            case "date":
                return DataTypes.DateType;

            case "timestamp":
                return DataTypes.TimestampType;

        }

        throw new RuntimeException("不支持 " + filedType + " 类型");
    }

    public static Object strConverType(String str, String filedType) {
        switch (filedType.toLowerCase()) {
            case "boolean":
                return Boolean.valueOf(str);
            case "int":
                return Integer.valueOf(str);

            case "bigint":
                return Long.valueOf(str);

            case "tinyint":
            case "byte":
                return Byte.valueOf(str);

            case "short":
            case "smallint":
                return Short.valueOf(str);

            case "char":
            case "varchar":
            case "string":
                return str;

            case "float":
                return Float.valueOf(str);

            case "double":
                return Double.valueOf(str);

            case "date":
                return Date.valueOf(str);

            case "timestamp":
                return str;
            //这里不转为timestamp的原因是：后面是采用 转json再from_json的方式
            //转的话，后面会解析不出来，倒是没有数据的情况，大坑，已踩
            //return Timestamp.valueOf(str);

        }

        throw new RuntimeException("字符串" + str + "不能转化为" + filedType + " 类型");
    }

    public static Map parseProp(String propsStr) {
        String[] strs = propsStr.trim().split("'\\s*,");
        Map<String, Object> propMap = new HashMap<>();
        for (int i = 0; i < strs.length; i++) {
            List<String> ss = DtStringUtil.splitIgnoreQuota(strs[i], '=');
            String key = ss.get(0).trim();
            String value = ss.get(1).trim().replaceAll("'", "").trim();
            propMap.put(key, value);
        }

        return propMap;
    }

    public static String getAppName(String appInfo){
        List<String> ss = DtStringUtil.splitIgnoreQuota(appInfo.trim(), '=');
        String appName = ss.get(1).trim().replaceAll("'", "").trim();
        return appName;
    }

    public static String converStrToTime(String inStr) {
        Pattern p = Pattern.compile("[A-Za-z]+$");
        Matcher m = p.matcher(inStr);
        boolean isValid = m.matches();
        if (isValid) {
            switch (inStr) {

            }
        }
        return "";
    }

    public static void main(String[] args) {
        String patt = "[a-z|A-Z]";
        String number = "2s".replaceAll("[^(0-9)]", "");
        String time = "2s".replaceAll("[^(A-Za-z)]", "");
//        Pattern r = Pattern.compile(patt);
//        Matcher m = r.matcher("2s");
        System.out.println(time);
        System.out.println(number);
    }
}
