package SamaiReceiverServiceAPI;

import SMLWebService.MyLib;
import SMLWebService.Routine;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils._global;
import utils.myglobal;
import utils._routine;
import java.sql.Date;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.StreamingOutput;

@Path("v1")
public class SamaiReceiverServiceAPI {

    private String _formatDate(Date date) {
        SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return _format.format(date);
    }

    private static boolean isNotNull(String txt) {
        return txt != null && txt.trim().length() > 0 ? true : false;
    }

    @GET
    @Path("/authentication")
    public Response Authentication(
            @QueryParam("provider_name") String strProvider,
            @QueryParam("database_name") String strDatabaseName,
            @QueryParam("user_code") String strUserCode,
            @QueryParam("password") String strPassword) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {
            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));
            String __strQUERY1 = "SELECT code as user_code, name_1 as user_name FROM erp_user WHERE code=upper('" + strUserCode + "') AND password='" + strPassword + "' ORDER BY code";

            Statement __stmt1;
            ResultSet __rs1;
            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __rs1 = __stmt1.executeQuery(__strQUERY1);
            JSONArray __jsonArr = new JSONArray();
            while (__rs1.next()) {

                JSONObject obj = new JSONObject();
                obj.put("user_code", __rs1.getString("user_code"));
                obj.put("user_name", __rs1.getString("user_name"));

                __jsonArr.put(obj);

            }
            __rs1.close();
            __stmt1.close();

            __objResponse.put("success", true);
            __objResponse.put("data", __jsonArr);
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getBranchList")
    public Response getBranchList(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "select code, name_1 from erp_branch_list order by code";

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("code", __rsHead.getString("code"));
                obj.put("name", __rsHead.getString("name_1"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __rsHead.close();
            __stmt1.close();
            __conn.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getItemSearch")
    public Response getItemSearch(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("search") String strSearch) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));
            String _where = "";
            StringBuilder __where = new StringBuilder();
            String _whereFinal = "";
            if (strSearch.trim().length() > 0) {
                String[] __fieldList = {"ic_code", "description", "barcode"};
                String[] __keyword = strSearch.trim().split(" ");
                for (int __field = 0; __field < __fieldList.length; __field++) {
                    if (__keyword.length > 0) {
                        if (__where.length() > 0) {
                            __where.append(" or ");
                        } else {
                            __where.append("  ");
                        }
                        __where.append("(");
                        for (int __loop = 0; __loop < __keyword.length; __loop++) {
                            if (__loop > 0) {
                                __where.append(" and ");
                            }
                            __where.append("upper(" + __fieldList[__field]
                                    + ") like \'%"
                                    + __keyword[__loop].toUpperCase() + "%\'");
                        }
                        __where.append(")");
                        _where += " and " + __where.toString();
                    }
                }
            }

            _whereFinal = _where;
            String __strQUERY1 = "select split_part(ic_code, '-', 1) AS item_ref_code,ic_code,barcode,description,unit_code from ic_inventory_barcode where 1=1 " + _whereFinal + " limit 1500";

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("barcode", __rsHead.getString("barcode"));
                obj.put("item_ref_code", __rsHead.getString("item_ref_code"));
                obj.put("item_code", __rsHead.getString("ic_code"));
                obj.put("item_name", __rsHead.getString("description"));
                obj.put("unit_code", __rsHead.getString("unit_code"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __rsHead.close();
            __stmt1.close();
            __conn.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getBarcodeSearch")
    public Response getBarcodeSearch(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("search") String strSearch) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));
            String _where = "";
            StringBuilder __where = new StringBuilder();
            String _whereFinal = "";

            _whereFinal = _where;
            String __strQUERY1 = "select split_part(ic_code, '-', 1) AS item_ref_code,ic_code,barcode,description,unit_code from ic_inventory_barcode where 1=1 and barcode='" + strSearch + "' limit 1500";

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("barcode", __rsHead.getString("barcode"));
                obj.put("item_ref_code", __rsHead.getString("item_ref_code"));
                obj.put("item_code", __rsHead.getString("ic_code"));
                obj.put("item_name", __rsHead.getString("description"));
                obj.put("unit_code", __rsHead.getString("unit_code"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __rsHead.close();
            __stmt1.close();
            __conn.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getReceiveDocDetail")
    public Response getReceiveDocDetail(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("docno") String __doc_no) {

        JSONObject __objResponse = new JSONObject();

        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            JSONArray jsarrDetailSo = new JSONArray();
            JSONArray jsarrDetail = new JSONArray();
            String query = "SELECT\n"
                    + "    split_part(td.item_code, '-', 1) AS item_ref_code,\n"
                    + "    td.item_code AS item_code,\n"
                    + "    COALESCE(i.name_1, '') AS item_name,\n"
                    + "    td.unit_code, "
                    + "    td.qty AS qty "
                    + " FROM ic_trans_detail td "
                    + " LEFT JOIN ic_inventory i "
                    + "  ON i.code = td.item_code  "
                    + " WHERE td.doc_no = (\n"
                    + "    SELECT doc_ref\n"
                    + "    FROM krc_trans\n"
                    + "    WHERE doc_no = '" + __doc_no + "' "
                    + " )\n"
                    + " order by line_number asc ;";
//            System.out.println("query " + query);
            Statement __stmt = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt.executeQuery(query);
            while (__rsHead.next()) {
                JSONObject obj = new JSONObject();
                obj.put("barcode", "");
                obj.put("item_ref_code", __rsHead.getString("item_ref_code"));
                obj.put("item_code", __rsHead.getString("item_code"));
                obj.put("item_name", __rsHead.getString("item_name"));

                obj.put("unit_code", __rsHead.getString("unit_code"));
                obj.put("qty", __rsHead.getString("qty"));

                jsarrDetailSo.put(obj);
            }

            String query1 = "select doc_no, "
                    + "    barcode, "
                    + "    coalesce((select name_1 from ic_inventory where ic_inventory.code = item_code),'')as item_name,"
                    + "    item_code, "
                    + "    unit_code, "
                    + "    qty, "
                    + "    so_qty, "
                    + "    remark, "
                    + "    line_number from krc_trans_detail where doc_no = '" + __doc_no + "' order by line_number";
            Statement __stmt2 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead2 = __stmt2.executeQuery(query1);
            while (__rsHead2.next()) {
                JSONObject obj = new JSONObject();
                obj.put("barcode", __rsHead2.getString("barcode"));

                obj.put("item_code", __rsHead2.getString("item_code"));
                obj.put("item_name", __rsHead2.getString("item_name"));
                obj.put("unit_code", __rsHead2.getString("unit_code"));
                obj.put("qty", __rsHead2.getString("qty"));
                obj.put("remark", __rsHead2.getString("remark"));

                jsarrDetail.put(obj);
            }

            __rsHead2.close();
            __stmt2.close();

            __rsHead.close();
            __stmt.close();

            __objResponse.put("details_so", jsarrDetailSo);
            __objResponse.put("details_receive", jsarrDetail);
            __objResponse.put("success", true);

            __conn.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getImagesList")
    public Response getImagesList(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("doc_no") String strDocNo) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "SELECT guid_code FROM sml_doc_images  where image_id = '" + strDocNo + "' ";
            System.out.println(__strQUERY1);
            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("guid_code", __rsHead.getString("guid_code"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __rsHead.close();
            __stmt1.close();
            __conn.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getWarehouse")
    public Response getWarehouse(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("branchcode") String strBranchcode) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "SELECT code,name_1 FROM ic_warehouse  where upper(branch_use) like '%" + strBranchcode.toUpperCase() + "%' ORDER BY code";
            System.out.println(__strQUERY1);
            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("code", __rsHead.getString("code"));
                obj.put("name", __rsHead.getString("name_1"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __rsHead.close();
            __stmt1.close();
            __conn.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getLocation")
    public Response getLocation(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("whcode") String strWhcode) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "SELECT code,name_1 FROM ic_shelf where whcode = '" + strWhcode + "' ORDER BY code";

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("code", __rsHead.getString("code"));
                obj.put("name", __rsHead.getString("name_1"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __rsHead.close();
            __stmt1.close();
            __conn.close();

        } catch (Exception ex) {
            __objResponse.put("message", ex.getMessage());
            return Response.status(400).entity(__objResponse).build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/sendApprove")
    public Response sendApprove(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("docno") String strDocno) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "update krc_trans set status=1 WHERE doc_no ='" + strDocno + "' ;";
            System.out.println("__strQUERY1");
            Statement __stmt1;

            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmt1.executeUpdate(__strQUERY1);
            __stmt1.close();
            __conn.close();

            __objResponse.put("success", true);
            __objResponse.put("data", new JSONArray());
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/sendCloseJob")
    public Response sendCloseJob(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("docno") String strDocno) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "update krc_trans set status=2 WHERE doc_no ='" + strDocno + "' ;";
            System.out.println("__strQUERY1");
            Statement __stmt1;

            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmt1.executeUpdate(__strQUERY1);
            __stmt1.close();
            __conn.close();

            __objResponse.put("success", true);
            __objResponse.put("data", new JSONArray());
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getPermission")
    public Response getPermission(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("search") String strSearch) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String _where = "";
            if (!strSearch.trim().equals("")) {
                _where = " where upper(code) like '%" + strSearch.toUpperCase() + "%' or upper(name_1) like '%" + strSearch.toUpperCase() + "%' ";
            }

            String __strQUERY1 = "select code,name_1,coalesce((select receive_screen from krc_permission where upper(user_code) = upper(code)),0) as receive_screen, "
                    + "coalesce((select admin_screen from krc_permission where upper(user_code) = upper(code)),0) as admin_screen, "
                    + "coalesce((select history_screen from krc_permission where upper(user_code) = upper(code)),0) as history_screen "
                    + "from erp_user " + _where;
            System.out.println("__strQUERY1 " + __strQUERY1);
            Statement __stmt1;
            ResultSet __rsHead;
            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            //JSONArray __jsonArr = _convertResultSetIntoJSON(__rs1);
            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("code", __rsHead.getString("code"));
                obj.put("name_1", __rsHead.getString("name_1"));
                obj.put("receive_screen", __rsHead.getString("receive_screen"));
                obj.put("admin_screen", __rsHead.getString("admin_screen"));
                obj.put("history_screen", __rsHead.getString("history_screen"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __conn.close();
            __rsHead.close();
            __stmt1.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getUserPermission")
    public Response getUserPermission(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("usercode") String strSearch) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String _where = "";
            if (!strSearch.trim().equals("")) {
                _where = " where upper(code) = '" + strSearch.toUpperCase() + "' ";
            }

            String __strQUERY1 = "select code,name_1,coalesce((select receive_screen from krc_permission where upper(user_code) = upper(code)),0) as receive_screen, "
                    + "coalesce((select admin_screen from krc_permission where upper(user_code) = upper(code)),0) as admin_screen, "
                    + "coalesce((select history_screen from krc_permission where upper(user_code) = upper(code)),0) as history_screen "
                    + "from erp_user " + _where;
            System.out.println("__strQUERY1 " + __strQUERY1);
            Statement __stmt1;
            ResultSet __rsHead;
            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();

            while (__rsHead.next()) {

                JSONObject obj = new JSONObject();

                obj.put("code", __rsHead.getString("code"));
                obj.put("name_1", __rsHead.getString("name_1"));
                obj.put("receive_screen", __rsHead.getString("receive_screen"));
                obj.put("admin_screen", __rsHead.getString("admin_screen"));
                obj.put("history_screen", __rsHead.getString("history_screen"));

                jsarr.put(obj);
            }

            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __conn.close();
            __rsHead.close();
            __stmt1.close();
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/upDatePermission")
    public Response upDatePermission(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("user") String user_code,
            @QueryParam("receive_screen") String receive_screen,
            @QueryParam("admin_screen") String admin_screen,
            @QueryParam("history_screen") String history_screen) throws Exception {

        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            UUID uuid = UUID.randomUUID();
            String strGUID = uuid.toString();

            StringBuilder __result = new StringBuilder();
            StringBuilder __query_builder = new StringBuilder();
            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            Statement __stmt1;

            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery("select user_code from krc_permission where user_code = '" + user_code + "'");

            __rsHead.next();

            int row = __rsHead.getRow();

            if (row > 0) {
                Statement __stmtdelete = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                __stmtdelete.executeUpdate("delete from krc_permission where user_code = '" + user_code + "';");
            }

            __query_builder.append("INSERT INTO krc_permission (user_code,receive_screen,admin_screen,history_screen) values ('" + user_code + "','" + receive_screen + "','" + admin_screen + "','" + history_screen + "');");

            System.out.println("__query_builder" + __query_builder);

            Statement __stmt2;

            __stmt2 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmt2.executeUpdate(__query_builder.toString());

            __objResponse.put("msg", "success");
            __objResponse.put("success", true);

            __stmt2.close();
            __conn.close();

        } catch (JSONException ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(__objResponse.toString(), MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/saveDocImage")
    public Response saveDocImage(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            String data) throws Exception {
        String docno = "";
        String image_file = "";

        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            if (data != null) {
                JSONObject objJSData = new JSONObject(data);
                docno = objJSData.has("doc_no") ? objJSData.getString("doc_no") : "";
                image_file = objJSData.has("image_file") ? objJSData.getString("image_file") : "";
            }

            String imgreplaced = image_file.replace("data:image/png;base64,", "");
            String imgreplaced2 = imgreplaced.replace("data:image/gif;base64,", "");
            String imgreplaced3 = imgreplaced2.replace("data:image/jpg;base64,", "");
            String imgreplaced4 = imgreplaced3.replace("data:image/bmp;base64,", "");
            String imgreplaced5 = imgreplaced4.replace("data:image/jpeg;base64,", "");

            UUID uuid = UUID.randomUUID();
            String strGUID = uuid.toString();
            byte[] bytesDecoded = Base64.decodeBase64(imgreplaced5.getBytes());
            StringBuilder __result = new StringBuilder();
            StringBuilder __query_builder = new StringBuilder();
            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase() + "_images", _global.FILE_CONFIG(strProvider));
            Connection __conn2 = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));
            String query = "INSERT INTO sml_doc_images (image_id,image_file,system_id,guid_code,image_order) values (?,?,?,?,?);";
            String query2 = "INSERT INTO sml_doc_images (image_id,system_id,guid_code,image_order) values (?,?,?,?)";

            System.out.println("query : " + query);

            PreparedStatement __stmt;
            PreparedStatement __stmt2;

            __stmt = __conn.prepareStatement(query);
            __stmt.setString(1, docno);
            __stmt.setBytes(2, bytesDecoded);
            __stmt.setString(3, "");
            __stmt.setString(4, strGUID);
            __stmt.setInt(5, 0);
            __stmt.executeUpdate();

            __stmt2 = __conn2.prepareStatement(query2);
            __stmt2.setString(1, docno);
            __stmt2.setString(2, "");
            __stmt2.setString(3, strGUID);
            __stmt2.setInt(4, 0);
            __stmt2.executeUpdate();

            __stmt2.close();

            __objResponse.put("msg", "success");
            __objResponse.put("success", true);

            __stmt.close();
            __conn.close();
            __conn2.close();

        } catch (JSONException ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(__objResponse.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/deleteDocImage")
    public Response deleteDocImage(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("guid_code") String strGuid) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase() + "_images", _global.FILE_CONFIG(strProvider));
            Connection __conn2 = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));
            String __strQUERY1 = "delete FROM sml_doc_images WHERE guid_code ='" + strGuid + "' ;";
            System.out.println("__strQUERY1");
            Statement __stmt1;
            Statement __stmt2;

            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmt1.executeUpdate(__strQUERY1);
            __stmt1.close();
            __conn.close();

            __stmt2 = __conn2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmt2.executeUpdate(__strQUERY1);
            __stmt2.close();
            __conn2.close();

            __objResponse.put("success", true);
            __objResponse.put("data", new JSONArray());
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getDocImage/{guid_code}")
    public Response getDocImage(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @PathParam("guid_code") String guidCode
    ) throws Exception {

        if (guidCode == null || guidCode.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        _routine __routine = new _routine();

        String sql = "SELECT image_file FROM sml_doc_images WHERE guid_code = ? LIMIT 1";

        try (Connection conn = __routine._connect(
                strDatabaseName.toLowerCase() + "_images",
                _global.FILE_CONFIG(strProvider));
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guidCode.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                // สำคัญสำหรับ Postgres(bytea): ใช้ getBytes ไม่ใช้ Blob
                byte[] imageBytes = rs.getBytes("image_file");
                if (imageBytes == null || imageBytes.length == 0) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                String mime = detectImageMime(imageBytes);
                if (mime == null) {
                    // ถ้าคุณ “รู้แน่” ว่าเก็บ PNG เสมอ ให้ return "image/png" ไปเลยจะดีกว่า
                    mime = "application/octet-stream";
                }

                return Response.ok(imageBytes)
                        .type(mime) // ต้องเป็น image/png หรือ image/jpeg เพื่อให้ <img> render
                        .header("Cache-Control", "public, max-age=86400")
                        .build();
            }
        }
    }

    private static String detectImageMime(byte[] b) {
        if (b == null || b.length < 4) {
            return null;
        }

        // PNG
        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        // JPEG
        if (b.length >= 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // GIF
        if (b.length >= 6
                && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return "image/gif";
        }
        // BMP
        if (b.length >= 2 && b[0] == 'B' && b[1] == 'M') {
            return "image/bmp";
        }
        // WEBP
        if (b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }

        return null;
    }

    @GET
    @Path("/deleteReceiveDoc")
    public Response deleteReceiveDoc(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("docno") String strDocno) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "delete FROM krc_trans WHERE doc_no ='" + strDocno + "' ;"
                    + " delete FROM krc_trans_detail WHERE doc_no ='" + strDocno + "' ;";
            System.out.println("__strQUERY1");
            Statement __stmt1;

            __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmt1.executeUpdate(__strQUERY1);
            __stmt1.close();
            __conn.close();

            __objResponse.put("success", true);
            __objResponse.put("data", new JSONArray());
        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getDashBoardDetail")
    public Response getDashBoardDetail(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("ic_code") String strIcCode
    ) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "select price_type,sale_type,ap.name_1 as cust_name,''::text as cust_group_1,''::text as cust_group_2,from_qty,to_qty,unit_code,from_date,to_date,sale_price1,sale_price2,coalesce(ip.creator_code,'') as creator_code,coalesce(TO_CHAR(ip.create_date_time_now, 'YYYY-MM-DD'),'') as doc_date,coalesce(TO_CHAR(ip.create_date_time_now, 'HH24:MI'),'') AS doc_time,ip.status from ic_inventory_purchase_price ip \n"
                    + "left join ap_supplier ap on ap.code = ip.supplier_code\n"
                    + " where ip.to_date > now() and ip.ic_code = '" + strIcCode + "'  order by ip.price_type asc,ip.unit_code,ip.create_date_time_now desc ";
            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);
            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {
                JSONObject obj = new JSONObject();
                obj.put("price_type", __rsHead.getString("price_type"));
                obj.put("sale_type", __rsHead.getString("sale_type"));
                obj.put("cust_name", __rsHead.getString("cust_name"));
                obj.put("cust_group_1", __rsHead.getString("cust_group_1"));
                obj.put("cust_group_2", __rsHead.getString("cust_group_2"));
                obj.put("from_qty", __rsHead.getString("from_qty"));
                obj.put("to_qty", __rsHead.getString("to_qty"));
                obj.put("unit_code", __rsHead.getString("unit_code"));
                obj.put("from_date", __rsHead.getString("from_date"));
                obj.put("to_date", __rsHead.getString("to_date"));
                obj.put("sale_price1", __rsHead.getString("sale_price1"));
                obj.put("sale_price2", __rsHead.getString("sale_price2"));
                obj.put("creator_code", __rsHead.getString("creator_code"));
                obj.put("doc_date", __rsHead.getString("doc_date"));
                obj.put("doc_time", __rsHead.getString("doc_time"));
                obj.put("status", __rsHead.getString("status"));
                obj.put("type", "0");

                jsarr.put(obj);
            }

            __rsHead.close();
            __stmt1.close();

            String __strQUERYGroup = "select price_type,sale_type,coalesce(ap.name_1,'') as cust_name,cust_group_1,cust_group_2,from_qty,to_qty,unit_code,from_date,to_date,sale_price1,sale_price2,coalesce(ip.creator_code,'') as creator_code,coalesce(TO_CHAR(ip.create_date_time_now, 'YYYY-MM-DD'),'') as doc_date,coalesce(TO_CHAR(ip.create_date_time_now, 'HH24:MI'),'') AS doc_time,ip.status \n"
                    + "from ic_inventory_price ip \n"
                    + "left join ap_supplier ap on ap.code = ip.cust_code\n"
                    + "  where ip.price_mode = 1 and ip.to_date > now() and ip.ic_code = '" + strIcCode + "' order by ip.price_type asc,ip.unit_code,ip.create_date_time_now desc ";
            Statement __stmtGroup = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsGroup = __stmtGroup.executeQuery(__strQUERYGroup);

            while (__rsGroup.next()) {
                JSONObject obj = new JSONObject();
                obj.put("price_type", __rsGroup.getString("price_type"));
                obj.put("sale_type", __rsGroup.getString("sale_type"));
                obj.put("cust_name", __rsGroup.getString("cust_name"));
                obj.put("cust_group_1", __rsGroup.getString("cust_group_1"));
                obj.put("cust_group_2", __rsGroup.getString("cust_group_2"));
                obj.put("from_qty", __rsGroup.getString("from_qty"));
                obj.put("to_qty", __rsGroup.getString("to_qty"));
                obj.put("unit_code", __rsGroup.getString("unit_code"));
                obj.put("from_date", __rsGroup.getString("from_date"));
                obj.put("to_date", __rsGroup.getString("to_date"));
                obj.put("sale_price1", __rsGroup.getString("sale_price1"));
                obj.put("sale_price2", __rsGroup.getString("sale_price2"));
                obj.put("creator_code", __rsGroup.getString("creator_code"));
                obj.put("doc_date", __rsGroup.getString("doc_date"));
                obj.put("doc_time", __rsGroup.getString("doc_time"));
                obj.put("status", __rsGroup.getString("status"));
                obj.put("type", "1");
                jsarr.put(obj);
            }

            String __strQUERYGroupSub = "select price_type,sale_type,coalesce(ap.name_1,'') as cust_name,cust_group_1,cust_group_2,from_qty,to_qty,unit_code,from_date,to_date,sale_price1,sale_price2,coalesce(ip.creator_code,'') as creator_code,coalesce(TO_CHAR(ip.create_date_time_now, 'YYYY-MM-DD'),'') as doc_date,coalesce(TO_CHAR(ip.create_date_time_now, 'HH24:MI'),'') AS doc_time,ip.status "
                    + "from ic_inventory_price ip "
                    + "left join ap_supplier ap on ap.code = ip.cust_code "
                    + "  where ip.price_mode = 0 and ip.to_date > now() and ip.ic_code = '" + strIcCode + "' order by ip.price_type asc,ip.unit_code,ip.create_date_time_now desc";
            Statement __stmtGroupSub = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsGroupSub = __stmtGroupSub.executeQuery(__strQUERYGroupSub);
            JSONArray jsarrGroupSub = new JSONArray();
            while (__rsGroupSub.next()) {
                JSONObject obj = new JSONObject();
                obj.put("price_type", __rsGroupSub.getString("price_type"));
                obj.put("sale_type", __rsGroupSub.getString("sale_type"));
                obj.put("cust_name", __rsGroupSub.getString("cust_name"));
                obj.put("cust_group_1", __rsGroupSub.getString("cust_group_1"));
                obj.put("cust_group_2", __rsGroupSub.getString("cust_group_2"));
                obj.put("from_qty", __rsGroupSub.getString("from_qty"));
                obj.put("to_qty", __rsGroupSub.getString("to_qty"));
                obj.put("unit_code", __rsGroupSub.getString("unit_code"));
                obj.put("from_date", __rsGroupSub.getString("from_date"));
                obj.put("to_date", __rsGroupSub.getString("to_date"));
                obj.put("sale_price1", __rsGroupSub.getString("sale_price1"));
                obj.put("sale_price2", __rsGroupSub.getString("sale_price2"));
                obj.put("creator_code", __rsGroupSub.getString("creator_code"));
                obj.put("doc_date", __rsGroupSub.getString("doc_date"));
                obj.put("doc_time", __rsGroupSub.getString("doc_time"));
                obj.put("status", __rsGroupSub.getString("status"));
                obj.put("type", "2");
                jsarr.put(obj);
            }

            __rsGroupSub.close();
            __stmtGroupSub.close();

            __objResponse.put("data", jsarr);

            __objResponse.put("success", true);

            __conn.close();

        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getSODocList")
    public Response getSODocList(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("search") String strSearch,
            @QueryParam("fromdate") String strFromdate,
            @QueryParam("todate") String strTodate,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);

        try {
            // กันค่าติดลบ หรือ 0
            if (page < 1) {
                page = 1;
            }
            if (size < 1) {
                size = 10;
            }

            int offset = (page - 1) * size;

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            // สร้าง where เงื่อนไขค้นหา
            String _where = "";
            String _whereText = "";
            StringBuilder __where = new StringBuilder();

            if (strSearch.trim().length() > 0) {
                String[] __fieldList = {"ic.doc_no"};
                String[] __keyword = strSearch.trim().split(" ");
                for (int __field = 0; __field < __fieldList.length; __field++) {
                    if (__keyword.length > 0) {
                        if (__where.length() > 0) {
                            __where.append(" or ");
                        } else {
                            __where.append("  ");
                        }
                        __where.append("(");
                        for (int __loop = 0; __loop < __keyword.length; __loop++) {
                            if (__loop > 0) {
                                __where.append(" and ");
                            }
                            __where.append("upper(" + __fieldList[__field]
                                    + ") like \'%"
                                    + __keyword[__loop].toUpperCase() + "%\'");
                        }
                        __where.append(")");
                        _whereText += " and " + __where.toString();
                    }
                }
            }

            if (strFromdate != null && !strFromdate.isEmpty()
                    && strTodate != null && !strTodate.isEmpty()) {
                _where += " and ic.doc_date between '" + strFromdate + "' and '" + strTodate + "' ";
            }
            if (strSearch.trim().length() > 0) {
                _where = "";
            }

            // นับ total ก่อนไว้ใช้สำหรับ pagination (optional แต่แนะนำให้มี)
            String __strCount = "select count(*) as total_count "
                    + "from ic_trans ic "
                    + "left join ap_supplier ar on ar.code = ic.cust_code "
                    + "left join erp_user erp on erp.code = ic.sale_code "
                    + "where trans_flag in (6,12) and last_status = 0 "
                    + "AND NOT EXISTS (select doc_no from krc_trans krc where krc.doc_ref = ic.doc_no) "
                    + _where + _whereText;

            Statement __stmtCount = __conn.createStatement();
            ResultSet __rsCount = __stmtCount.executeQuery(__strCount);
            int total = 0;
            if (__rsCount.next()) {
                total = __rsCount.getInt("total_count");
            }
            __rsCount.close();
            __stmtCount.close();

            // ดึงข้อมูลหน้าแต่ละหน้า
            String __strQUERY1
                    = "select ic.doc_no, ic.doc_date, ic.doc_time, ic.cust_code, ic.branch_code, "
                    + "ic.sale_code, ic.remark, ic.creator_code, "
                    + "coalesce(ar.name_1,'') as cust_name, "
                    + "coalesce(erp.name_1,'') as sale_name "
                    + "from ic_trans ic "
                    + "left join ap_supplier ar on ar.code = ic.cust_code "
                    + "left join erp_user erp on erp.code = ic.sale_code "
                    + " where trans_flag in (6,12) and last_status = 0 "
                    + " AND NOT EXISTS (select doc_no from krc_trans krc where krc.doc_ref = ic.doc_no) "
                    + _where + _whereText
                    + " order by ic.create_datetime DESC "
                    + " limit " + size + " offset " + offset;

            System.out.println(__strQUERY1);

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);

            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {
                JSONObject obj = new JSONObject();
                obj.put("doc_no", __rsHead.getString("doc_no"));
                obj.put("doc_date", __rsHead.getString("doc_date"));
                obj.put("doc_time", __rsHead.getString("doc_time"));

                obj.put("remark", __rsHead.getString("remark"));
                obj.put("cust_code", __rsHead.getString("cust_code"));
                obj.put("cust_name", __rsHead.getString("cust_name"));
                obj.put("sale_code", __rsHead.getString("sale_code"));
                obj.put("sale_name", __rsHead.getString("sale_name"));
                obj.put("branch_code", __rsHead.getString("branch_code"));

                jsarr.put(obj);
            }

            __rsHead.close();
            __stmt1.close();
            __conn.close();

            // ใส่ข้อมูล pagination กลับไปด้วย
            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __objResponse.put("page", page);
            __objResponse.put("size", size);
            __objResponse.put("total", total);                    // จำนวน record ทั้งหมด
            __objResponse.put("totalPages", // จำนวนหน้าทั้งหมด
                    (int) Math.ceil((double) total / (double) size));

        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }

        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/createReceiveDoc")
    public Response saveCartSubDetail(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            String data) {
        JSONObject __objResponse = new JSONObject();
        String docno = "";
        String docdate = "";
        String doctime = "";
        String docref = "";
        String branchcode = "";

        String remark = "";
        String usercode = "";
        String custcode = "";
        String salecode = "";

        JSONArray items = new JSONArray();
        if (data != null) {
            JSONObject objJSData = new JSONObject(data);
            docno = objJSData.has("doc_no") ? objJSData.getString("doc_no") : "";
            docref = objJSData.has("doc_ref") ? objJSData.getString("doc_ref") : "";
            custcode = objJSData.has("cust_code") ? objJSData.getString("cust_code") : "";
            salecode = objJSData.has("sale_code") ? objJSData.getString("sale_code") : "";
            branchcode = objJSData.has("branch_code") ? objJSData.getString("branch_code") : "";
            usercode = objJSData.has("user_code") ? objJSData.getString("user_code") : "";
            remark = objJSData.has("remark") ? objJSData.getString("remark") : "";
        }

        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            String __strQUERY1 = "select doc_no from krc_trans where doc_no='" + docno + "' ";

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            ResultSet __rsCount = __stmt1.executeQuery(__strQUERY1);

            int row = __rsCount.getRow();

            if (row == 0) {
                String __strQUERYz = "INSERT INTO krc_trans ( "
                        + "    doc_no, "
                        + "    doc_date, "
                        + "    doc_time, "
                        + "    wh_code, "
                        + "    location_code, "
                        + "    creator_code, "
                        + "    remark, "
                        + "    status, "
                        + "    is_approve, "
                        + "    user_approve, "
                        + "    approve_date_time, "
                        + "    branch_code, "
                        + "    doc_ref, "
                        + "    is_close, "
                        + "    user_close, "
                        + "    close_date_time, "
                        + "    cust_code, "
                        + "    sale_code "
                        + ") VALUES ( "
                        + "    '" + docno + "',        "
                        + "    now(),     "
                        + "    now(),     "
                        + "    '',            "
                        + "    '',            "
                        + "    '" + usercode + "',          "
                        + "    '" + remark + "', "
                        + "    0,                 "
                        + "    0,                "
                        + "    '',               "
                        + "    now(),              "
                        + "    '" + branchcode + "',            "
                        + "    '" + docref + "',               "
                        + "    '0',                "
                        + "    '',               "
                        + "    now(),              "
                        + "    '" + custcode + "',          "
                        + "    '" + salecode + "'          "
                        + ");";

                System.out.println("" + __strQUERYz);
                Statement __stmtz;
                __stmtz = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                __stmtz.executeUpdate(__strQUERYz);
                __stmtz.close();
                __objResponse.put("success", true);
                __objResponse.put("data", "success");
            } else {
                __objResponse.put("success", false);
                __objResponse.put("data", "DocNo Duplicated");
            }
            __stmt1.close();
            __conn.close();

        } catch (Exception ex) {
            __objResponse.put("message", ex.getMessage());
            return Response.status(400).entity(String.valueOf(__objResponse)).build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/getReceiveDocList")
    public Response getReceiveDocList(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            @QueryParam("search") String strSearch,
            @QueryParam("fromdate") String strFromdate,
            @QueryParam("todate") String strTodate,
            @QueryParam("status") @DefaultValue("0") String strStatus,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        JSONObject __objResponse = new JSONObject();
        __objResponse.put("success", false);

        try {
            // กันค่าติดลบ หรือ 0
            if (page < 1) {
                page = 1;
            }
            if (size < 1) {
                size = 10;
            }

            int offset = (page - 1) * size;

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));

            // สร้าง where เงื่อนไขค้นหา
            String _where = "";
            String _whereText = "";
            StringBuilder __where = new StringBuilder();

            if (strSearch.trim().length() > 0) {
                String[] __fieldList = {"ic.doc_no", "ic.doc_ref", "ic.sale_code", "ar.name_1", "erp.name_1", "ic.cust_code", "ic.remark", "ic.user_approve", "ic.user_close"};
                String[] __keyword = strSearch.trim().split(" ");
                for (int __field = 0; __field < __fieldList.length; __field++) {
                    if (__keyword.length > 0) {
                        if (__where.length() > 0) {
                            __where.append(" or ");
                        } else {
                            __where.append("  ");
                        }
                        __where.append("(");
                        for (int __loop = 0; __loop < __keyword.length; __loop++) {
                            if (__loop > 0) {
                                __where.append(" and ");
                            }
                            __where.append("upper(" + __fieldList[__field]
                                    + ") like \'%"
                                    + __keyword[__loop].toUpperCase() + "%\'");
                        }
                        __where.append(")");
                        _whereText += " and " + __where.toString();
                    }
                }
            }

            if (strFromdate != null && !strFromdate.isEmpty()
                    && strTodate != null && !strTodate.isEmpty()) {
                _where += " and ic.doc_date between '" + strFromdate + "' and '" + strTodate + "' ";
            }

            // นับ total ก่อนไว้ใช้สำหรับ pagination (optional แต่แนะนำให้มี)
            String __strCount = "select count(*) as total_count "
                    + " from krc_trans ic"
                    + " left join ap_supplier ar on ar.code = ic.cust_code "
                    + " left join erp_user erp on erp.code = ic.sale_code "
                    + " where ic.status = " + strStatus
                    + " "
                    + _where + _whereText;
            System.out.println("__strCount " + __strCount);
            Statement __stmtCount = __conn.createStatement();
            ResultSet __rsCount = __stmtCount.executeQuery(__strCount);
            int total = 0;
            if (__rsCount.next()) {
                total = __rsCount.getInt("total_count");
            }
            __rsCount.close();
            __stmtCount.close();

            // ดึงข้อมูลหน้าแต่ละหน้า
            String __strQUERY1
                    = "select ic.doc_no, ic.doc_date, ic.doc_time, ic.cust_code, ic.branch_code, "
                    + "ic.sale_code, ic.remark, ic.creator_code,ic.doc_ref,coalesce(TO_CHAR(ic.approve_date_time, 'YYYY-MM-DD'),'') as approve_date,coalesce(TO_CHAR(ic.approve_date_time, 'HH24:MI'),'') AS approve_time,"
                    + "coalesce(TO_CHAR(ic.close_date_time, 'YYYY-MM-DD'),'') as close_date,coalesce(TO_CHAR(ic.close_date_time, 'HH24:MI'),'') AS close_time, "
                    + "coalesce(ar.name_1,'') as cust_name, "
                    + "coalesce(erp.name_1,'') as sale_name, "
                    + "coalesce(ic.user_approve,'') as user_approve, "
                    + "coalesce(ic.user_close,'') as user_close, "
                    + "coalesce(erp_approve.name_1,'') as user_approve_name, "
                    + "coalesce(erp_close.name_1,'') as user_close_name, "
                    + "coalesce((select count(image_id) from sml_doc_images where image_id = ic.doc_ref),0) as image_count, "
                    + "coalesce((select sum(qty) from ic_trans_detail icx where icx.doc_no = ic.doc_ref and trans_flag in (6,12)),0) as so_qty, "
                    + "coalesce((select sum(qty) from krc_trans_detail krx where krx.doc_no = ic.doc_no),0) as receive_qty "
                    + " from krc_trans ic "
                    + " left join ap_supplier ar on ar.code = ic.cust_code "
                    + " left join erp_user erp on erp.code = ic.sale_code "
                    + " left join erp_user erp_approve on erp.code = ic.user_approve "
                    + " left join erp_user erp_close on erp.code = ic.user_close "
                    + " where  ic.status = " + strStatus
                    + " "
                    + _where + _whereText
                    + " order by ic.create_datetime DESC "
                    + " limit " + size + " offset " + offset;

            System.out.println(__strQUERY1);

            Statement __stmt1 = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet __rsHead = __stmt1.executeQuery(__strQUERY1);

            JSONArray jsarr = new JSONArray();
            while (__rsHead.next()) {
                JSONObject obj = new JSONObject();
                obj.put("doc_no", __rsHead.getString("doc_no"));
                obj.put("doc_date", __rsHead.getString("doc_date"));
                obj.put("doc_time", __rsHead.getString("doc_time"));
                obj.put("remark", __rsHead.getString("remark"));
                obj.put("cust_code", __rsHead.getString("cust_code"));
                obj.put("cust_name", __rsHead.getString("cust_name"));
                obj.put("sale_code", __rsHead.getString("sale_code"));
                obj.put("sale_name", __rsHead.getString("sale_name"));
                obj.put("doc_ref", __rsHead.getString("doc_ref"));
                obj.put("branch_code", __rsHead.getString("branch_code"));
                obj.put("approve_date", __rsHead.getString("approve_date"));
                obj.put("approve_time", __rsHead.getString("approve_time"));
                obj.put("close_date", __rsHead.getString("close_date"));
                obj.put("close_time", __rsHead.getString("close_time"));
                obj.put("user_approve_name", __rsHead.getString("user_approve_name"));
                obj.put("user_approve", __rsHead.getString("user_approve"));
                obj.put("user_close", __rsHead.getString("user_close"));
                obj.put("user_close_name", __rsHead.getString("user_close_name"));
                obj.put("so_qty", __rsHead.getString("so_qty"));
                obj.put("receive_qty", __rsHead.getString("receive_qty"));
                obj.put("image_count", __rsHead.getString("image_count"));
                if (Float.parseFloat(__rsHead.getString("receive_qty")) >= Float.parseFloat(__rsHead.getString("so_qty"))) {
                    obj.put("can_approve", "1");
                } else {
                    obj.put("can_approve", "0");
                }
                jsarr.put(obj);
            }

            __rsHead.close();
            __stmt1.close();
            __conn.close();

            // ใส่ข้อมูล pagination กลับไปด้วย
            __objResponse.put("success", true);
            __objResponse.put("data", jsarr);
            __objResponse.put("page", page);
            __objResponse.put("size", size);
            __objResponse.put("total", total);                    // จำนวน record ทั้งหมด
            __objResponse.put("totalPages", // จำนวนหน้าทั้งหมด
                    (int) Math.ceil((double) total / (double) size));

        } catch (Exception ex) {
            return Response.status(400).entity("{ERROR: " + ex.getMessage() + "}").build();
        }

        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/updateReceiveDoc")
    public Response updateReceiveDoc(
            @QueryParam("provider") String strProvider,
            @QueryParam("dbname") String strDatabaseName,
            String data) {
        JSONObject __objResponse = new JSONObject();
        String docno = "";
        JSONArray details = new JSONArray();
        if (data != null) {
            JSONObject objJSData = new JSONObject(data);
            docno = objJSData.has("docno") ? objJSData.getString("docno") : "";
            if (objJSData.has("details")) {
                details = objJSData.getJSONArray("details");
            }
        }

        __objResponse.put("success", false);
        try {

            _routine __routine = new _routine();
            Connection __conn = __routine._connect(strDatabaseName.toLowerCase(), _global.FILE_CONFIG(strProvider));
            StringBuilder _queryDetail = new StringBuilder();

            Statement __stmtDelete;

            __stmtDelete = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmtDelete.executeUpdate("delete from krc_trans_detail where doc_no = '" + docno + "';");
            __stmtDelete.close();

            for (int i = 0; i < details.length(); i++) {
                JSONObject objJSDataItem = details.getJSONObject(i);
                _queryDetail.append("insert into krc_trans_detail (doc_no, "
                        + "    barcode, "
                        + "    item_year, "
                        + "    item_code, "
                        + "    unit_code, "
                        + "    qty, "
                        + "    so_qty, "
                        + "    remark, "
                        + "    line_number) "
                        + "values ('" + docno + "','','','" + objJSDataItem.getString("item_code") + "','" + objJSDataItem.getString("unit_code") + "','" + objJSDataItem.getString("qty") + "','0',''," + i + ");");

            }
            Statement __stmtDetail;
            System.out.println(_queryDetail);
            __stmtDetail = __conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            __stmtDetail.executeUpdate(_queryDetail.toString());
            __stmtDetail.close();
            __objResponse.put("success", true);
            __objResponse.put("data", "success");
            __conn.close();

        } catch (Exception ex) {
            __objResponse.put("message", ex.getMessage());
            return Response.status(400).entity(String.valueOf(__objResponse)).build();
        }
        return Response.ok(String.valueOf(__objResponse), MediaType.APPLICATION_JSON).build();
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
