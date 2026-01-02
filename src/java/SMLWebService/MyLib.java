/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SMLWebService;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONArray;
import java.sql.ResultSet;

/**
 *
 * @author TOE
 */
public class MyLib {

    public static JSONObject _getJson(String XMLString) {

        int PRETTY_PRINT_INDENT_FACTOR = 4;
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(XMLString);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            //System.out.println(jsonPrettyPrintString);
            return xmlJSONObj;
        } catch (JSONException je) {
            System.out.println(je.toString());
        }

        return null;
    }

    public static String _getJsonStr(String XMLString) {

        int PRETTY_PRINT_INDENT_FACTOR = 4;
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(XMLString);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            //System.out.println(jsonPrettyPrintString);
            return jsonPrettyPrintString;
        } catch (JSONException je) {
            System.out.println(je.toString());
        }

        return "";
    }

    public static String _JsonStr(JSONObject xmlJSONObj) {

        int PRETTY_PRINT_INDENT_FACTOR = 4;
        try {
            //JSONObject xmlJSONObj = XML.toJSONObject(XMLString);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            //System.out.println(jsonPrettyPrintString);
            return jsonPrettyPrintString;
        } catch (JSONException je) {
            System.out.println(je.toString());
        }

        return "";
    }

    public static String _whereLike(String fieldList, String query) {
        StringBuilder __result = new StringBuilder();
        String[] __fieldList = fieldList.trim().split(",");
        String[] __query = query.trim().split(" ");
        for (int __loop1 = 0; __loop1 < __query.length; __loop1++) {
            if (__loop1 > 0) {
                __result.append(" and ");
            }
            __result.append("(");
            for (int __loop2 = 0; __loop2 < __fieldList.length; __loop2++) {
                if (__loop2 > 0) {
                    __result.append(" or ");
                }
                __result.append("upper(").append(__fieldList[__loop2].toString()).append(")");
                __result.append(" like \'%");
                __result.append(__query[__loop1].toString().toUpperCase());
                __result.append("%\'");
            }
            __result.append(")");
        }
        return __result.toString();
    }

    public static JSONArray _convertResultSetIntoJSON(ResultSet resultSet) throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < total_rows; i++) {
                String columnName = resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase();
                Object columnValue = resultSet.getObject(i + 1);
                // if value in DB is null, then we set it to default value
                if (columnValue == null) {
                    columnValue = "null";
                }
                /*
                Next if block is a hack. In case when in db we have values like price and price1 there's a bug in jdbc - 
                both this names are getting stored as price in ResulSet. Therefore when we store second column value,
                we overwrite original value of price. To avoid that, i simply add 1 to be consistent with DB.
                 */
                if (obj.has(columnName)) {
                    columnName += "1";
                }
                obj.put(columnName, columnValue);
            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }
}
