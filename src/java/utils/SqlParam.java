package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlParam {
    private List<Object> params;
    private int startParam = 1;
    
    public SqlParam(){
        params = new ArrayList<>();
    }
    
    public SqlParam(List<Object> params){
        this.params = params;
    }
    
    public void setStartParam(int startParam){
        this.startParam = startParam;
    }
    
    public void addParam(Object param){
        params.add(param);
    }
    
    public void addParam(int index, Object param){
        params.add(index, param);
    }
    
    public void addParams(List<Object> params){
        for(int i = 0; i < params.size(); i++){
            this.addParam(params.get(i));
        }
    }
    
    public List<Object> getParams(){
        return this.params;
    }
    
    public PreparedStatement stmtBuilder(Connection __conn, StringBuilder sqlScript) throws SQLException{
        return stmtBuilder(__conn, sqlScript.toString());
    }
    
    public PreparedStatement stmtBuilder(Connection __conn, String sqlScript) throws SQLException{
        PreparedStatement stmt = __conn.prepareStatement(sqlScript);
        for(int i = 0; i < params.size();i++){
            stmt.setObject(i+this.startParam, this.params.get(i));
        }
        return stmt;
    }

//    public PreparedStatement stmtBuilder(PreparedStatement stmt) throws SQLException{
//        for(int i = 0; i < params.size();i++){
//            stmt.setObject(i+this.startParam, this.params.get(i));
//        }
//        return stmt;
//    }
}
