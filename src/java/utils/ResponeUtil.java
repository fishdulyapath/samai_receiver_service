package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResponeUtil {

    public static JSONObject str2Json(String strParam) {
        String tmpParam = strParam == null || strParam.isEmpty() ? "{}" : strParam;
        return new JSONObject(tmpParam);
    }

    public static JSONObject param2Json(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String tmpStr = sb.toString().isEmpty() ? "{}" : sb.toString();
        return new JSONObject(strEscape(tmpStr));
    }

    public static JSONArray catchDev(Exception e) {
        JSONArray stackMsg = new JSONArray();
        StackTraceElement[] elements = e.getStackTrace();
        for (int iterator = 1; iterator <= elements.length; iterator++) {
            stackMsg.put("Class Name:" + elements[iterator - 1].getClassName() + ", Method Name:" + elements[iterator - 1].getMethodName() + ", Line Number:" + elements[iterator - 1].getLineNumber());
        }
        return stackMsg;
    }

    public static JSONArray query2Array(ResultSet __rs) throws SQLException {
        ResultSetMetaData _rsMeta = __rs.getMetaData();
        int _colBodyCount = _rsMeta.getColumnCount();
        JSONArray allData = new JSONArray();
        while (__rs.next()) {
            JSONObject tmpBody = new JSONObject();
            for (int __i = 1; __i <= _colBodyCount; __i++) {
                String _colBodyName = _rsMeta.getColumnName(__i);
                tmpBody.put(_colBodyName, __rs.getObject(__i));
            }
            allData.put(tmpBody);
        }
        return allData;
    }

//    public static JSONArray query2ArrayBytea2Base64(ResultSet __rs) throws SQLException {
//        ResultSetMetaData _rsMeta = __rs.getMetaData();
//        int _colBodyCount = _rsMeta.getColumnCount();
//        JSONArray allData = new JSONArray();
//        while (__rs.next()) {
//            JSONObject tmpBody = new JSONObject();
//            for (int __i = 1; __i <= _colBodyCount; __i++) {
//                String _colBodyName = _rsMeta.getColumnName(__i);
//                if (_rsMeta.getColumnTypeName(__i).equals("bytea")) {
//                    byte[] tmpFile = __rs.getBytes(__i);
//                    tmpBody.put(_colBodyName, tmpFile == null ? "" : Base64.getEncoder().encodeToString(tmpFile));
//                } else {
//                    tmpBody.put(_colBodyName, __rs.getObject(__i));
//                }
//            }
//            allData.put(tmpBody);
//        }
//        return allData;
//    }

    public static JSONArray query2ArrayByName(ResultSet __rs, String columnName) throws SQLException {
        ResultSetMetaData _rsMeta = __rs.getMetaData();
        int _colBodyCount = _rsMeta.getColumnCount();
        JSONArray allData = new JSONArray();
        while (__rs.next()) {
            for (int __i = 1; __i <= _colBodyCount; __i++) {
                allData.put(__rs.getObject(columnName));
            }
        }
        return allData;
    }

    public static ArrayList _resultSetToArrayList(ResultSet rs) throws SQLException {
        ResultSetMetaData __md = rs.getMetaData();
        int __columns = __md.getColumnCount();
        ArrayList __results = new ArrayList();
        while (rs.next()) {
            HashMap __row = new HashMap();
            __results.add(__row);
            for (int __i = 1; __i <= __columns; __i++) {
                __row.put(__md.getColumnName(__i), rs.getObject(__i));
            }
        }
        return __results;
    }

    public static String strEscape(String str) {
        return str.replaceAll("'", "''");
    }
}
