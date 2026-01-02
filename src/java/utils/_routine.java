/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author admin
 */
public class _routine {

    public String _loginGuid = "";
    String _databaseServer = "";
    String _databaseUserCode = "";
    String _databaseUserPassword = "";
    String _databasePortNumber = "";

    public _routine() {
    }

    public _routine(String strDatabaseName, String strProviderCode) {
        _global._databaseName = strDatabaseName;
        _global._providerCode = strProviderCode;
    }

    public byte[] _scale(byte[] fileData, int width, int height) {
        ByteArrayInputStream __in = new ByteArrayInputStream(fileData);
        try {
            BufferedImage __img = ImageIO.read(__in);
            if (height == 0) {
                height = (width * __img.getHeight()) / __img.getWidth();
            }
            if (width == 0) {
                width = (height * __img.getWidth()) / __img.getHeight();
            }
            Image __scaledImage = __img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage __imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            __imageBuff.getGraphics().drawImage(__scaledImage, 0, 0, new Color(0, 0, 0), null);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ImageIO.write(__imageBuff, "jpg", buffer);

            return buffer.toByteArray();
        } catch (Exception e) {
        }
        return null;
    }

    public static String dateThai(String strDate) {

        String Months[] = {
            "ม.ค", "ก.พ", "มี.ค", "เม.ย",
            "พ.ค", "มิ.ย", "ก.ค", "ส.ค",
            "ก.ย", "ต.ค", "พ.ย", "ธ.ค"};

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        int year = 0, month = 0, day = 0;

        try {

            Date date = df.parse(strDate);

            Calendar c = Calendar.getInstance();

            c.setTime(date);

            year = c.get(Calendar.YEAR);

            month = c.get(Calendar.MONTH);

            day = c.get(Calendar.DATE);

        } catch (ParseException e) {

// TODO Auto-generated catch block
            e.printStackTrace();

        }

        return String.format("%s %s %s", day, Months[month], year + 543);

    }

    public byte[] _getImage(Connection conn, String id) throws Exception {
        return _getImage(conn, id, 90, 180);
    }

    public byte[] _getImage(Connection conn, String id, int width, int height) throws Exception, SQLException {
        try {
            byte[] __imgData = null;
            Statement __stmt = conn.createStatement();

            // Query
            String __req = "select image_file from images where image_id = '" + id + "'";
            ResultSet __rset = __stmt.executeQuery(__req);
            if (__rset.next()) {
                __imgData = __rset.getBytes("image_file");
            }
            __rset.close();
            __stmt.close();
            return _scale(__imgData, 90, 180);
        } catch (Exception ex) {
        }
        return null;
    }

    public Connection _connect() {
        return this._connect(_global._databaseName);
    }

    public Connection _connect(String databaseName) {
        StringBuilder __dbConnectUrl = new StringBuilder();
        Connection __conn = null;

        try {// PostgreSQL
            String __dbClassName = "org.postgresql.Driver";
            __dbConnectUrl.append("jdbc:postgresql");
            String __dbDataPort = "5432";
            if (this._databasePortNumber.length() > 0) {
                __dbDataPort = this._databasePortNumber;
            }
            __dbConnectUrl.append("://").append(this._databaseServer).append(":").append(__dbDataPort);
            if (databaseName.length() != 0) {
                __dbConnectUrl.append("/").append(databaseName.toLowerCase());
            }
            Class.forName(__dbClassName).newInstance();
            System.out.println(__dbConnectUrl.toString() + ", " + _global._databaseUserCode + ", " + _global._databaseUserPassword);
            __conn = DriverManager.getConnection(__dbConnectUrl.toString(), this._databaseUserCode, this._databaseUserPassword);
            try ( // PostgreSql
                    Statement __stmt = __conn.createStatement()) {
                __stmt.execute("set enable_seqscan=false");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException __ex) {
            String __error = __ex.getMessage();
            __ex.printStackTrace();
            System.out.println(__dbConnectUrl.toString() + ", " + this._databaseUserCode + ", " + this._databaseUserPassword);
        }

        return __conn;
    }

    public Connection _connect(String databaseName, String configFileName) {

        if (configFileName == "") {
            return this._connect(databaseName);
        }

        try {
            String __xReloadFile = _readXmlFile(configFileName);
            DocumentBuilderFactory __docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder __docBuilder = __docBuilderFactory.newDocumentBuilder();
            Document __doc = __docBuilder.parse(new InputSource(new StringReader(__xReloadFile)));
            __doc.getDocumentElement().normalize();
            NodeList __listOfData = __doc.getElementsByTagName("node");
            Node __firstNode = __listOfData.item(0);

            if (__firstNode.getNodeType() == Node.ELEMENT_NODE) {
                Element __firstElement = (Element) __firstNode;
                // ---
                this._databaseServer = _xmlGetNodeValue(__firstElement, "server");
                this._databaseUserCode = _xmlGetNodeValue(__firstElement, "user");
                this._databaseUserPassword = _xmlGetNodeValue(__firstElement, "password");
                String __databasePortNumber = _xmlGetNodeValue(__firstElement, "port");
                if (__databasePortNumber.length() > 0) {
                    this._databasePortNumber = __databasePortNumber;
                }

            }

        } catch (SAXParseException __err) {
            System.out.println("** Parsing error" + ", line " + __err.getLineNumber() + ", uri " + __err.getSystemId());
            System.out.println(" " + __err.getMessage());
        } catch (SAXException __ex) {
            Exception __x = __ex.getException();
        } catch (Throwable __t) {
        }

        return this._connect(databaseName);
    }

    public ResultSet _excute(String strQuery, SqlParam sqlParam) throws Exception {
        Connection __conn = this._connect(_global._databaseName, _global._providerCode);
        PreparedStatement __stmt;
        ResultSet __rsData;
        try {
            if (sqlParam != null) {
                __stmt = sqlParam.stmtBuilder(__conn, strQuery);
            } else {
                __stmt = __conn.prepareStatement(strQuery);
            }
            __rsData = __stmt.executeQuery();
        } finally {
            if (__conn != null) {
                __conn.close();
            }
        }
        return __rsData;
    }

    public JSONArray _excute2Array(String strQuery, SqlParam sqlParam) throws Exception {
        Connection __conn = this._connect(_global._databaseName, _global._providerCode);
        PreparedStatement __stmt;
        ResultSet __rsData;
        try {
            if (sqlParam != null) {
                __stmt = sqlParam.stmtBuilder(__conn, strQuery);
            } else {
                __stmt = __conn.prepareStatement(strQuery);
            }
            __rsData = __stmt.executeQuery();
        } finally {
            if (__conn != null) {
                __conn.close();
            }
        }
        return ResponeUtil.query2Array(__rsData);
    }

    public Integer _excuteUpdate(String strQuery, SqlParam sqlParam) throws Exception {
        Connection __conn = this._connect(_global._databaseName, _global._providerCode);
        PreparedStatement __stmt;
        Integer __result = 0;
        try {
            if (sqlParam != null) {
                __stmt = sqlParam.stmtBuilder(__conn, strQuery);
            } else {
                __stmt = __conn.prepareStatement(strQuery);
            }
            __result = __stmt.executeUpdate();
        } finally {
            if (__conn != null) {
                __conn.close();
            }
        }
        return __result;
    }

    public Integer _rowCount(String strQuery, SqlParam sqlParam) throws Exception {
        Connection __conn = this._connect(_global._databaseName, _global._providerCode);
        PreparedStatement __stmt;
        Integer __count = 0;
        try {
            if (sqlParam != null) {
                __stmt = sqlParam.stmtBuilder(__conn, strQuery);
            } else {
                __stmt = __conn.prepareStatement(strQuery);
            }
            ResultSet __rsData = __stmt.executeQuery();

            while (__rsData.next()) {
                __count++;
            }
        } finally {
            if (__conn != null) {
                __conn.close();
            }
        }

        return __count;
    }

    /**
     * อ่านไฟล์ xml
     *
     * @param xmlName ไฟล์ xml ที่ต้องการอ่าน
     * @return
     */
    public String _readXmlFile(String xmlName) {
        String __readLine = "";
        try {
            // Reader __input = new InputStreamReader(new FileInputStream(xmlName));
            //     BufferedReader __in = new BufferedReader(__input);
            String __tempDir = System.getProperty("java.io.tmpdir");
            BufferedReader __in = new BufferedReader(new InputStreamReader(new FileInputStream(__tempDir + "/" + xmlName), "UTF8"));
            char[] __cBuf = new char[65536];
            StringBuilder __stringBuf = new StringBuilder();
            int __readThisTime = 0;
            while (__readThisTime != -1) {
                try {
                    __readThisTime = __in.read(__cBuf, 0, 65536);
                    __stringBuf.append(__cBuf, 0, __readThisTime);
                } catch (Exception __ex) {
                }
            } // end while
            __readLine = __stringBuf.toString();
            __in.close();
        } catch (Exception __ex) {
            System.out.println("_readXmlFile:" + __ex.getMessage());
            __readLine = __ex.getMessage();
        }
        return __readLine;
    }

    // ดึงตัวแปรจาก Tag XML
    /**
     * ตัวช่วยสำหรับดึงค่า Value ออกจาก XML
     *
     * @param firstElement Element ที่ต้องการ
     * @param tagName ชื่อ Tag ที่ต้องการ
     * @return ข้อมูลที่อยู่ระหว่าง Tag (Value)
     */
    //private
    public String _xmlGetNodeValue(Element firstElement, String tagName) {
        try {
            NodeList __firstNameList = firstElement.getElementsByTagName(tagName);
            if (__firstNameList.getLength() > 0) {
                Element __firstNameElement = (Element) __firstNameList.item(0);
                NodeList __textFNList = __firstNameElement.getChildNodes();
                if (__textFNList.getLength() > 0) {
                    Node __getData = __textFNList.item(0);
                    return __getData.getNodeValue().trim();
                }
            }
        } catch (Exception __ex) {
            System.out.println("_xmlGetNodeValue:" + __ex.getMessage());
        }
        return "";
    }
}
