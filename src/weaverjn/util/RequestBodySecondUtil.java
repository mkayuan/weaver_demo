package weaverjn.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.StringUtil;
import weaver.general.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ayuan
 * date 2025/1/24
 */

public class RequestBodySecondUtil {

    private String uftablename = "uf_ActionSecondApi";
    private String url = "";
    private String mainid = "";
    private String fhzd = "";
    private String cgbs = "";

    public RequestBodySecondUtil(String tablename, String xzjk, String workflowid) {
        this.uftablename = tablename;
        RecordSet recordSet = new RecordSet();
        String sql;
        sql = "select id,url,fhzd,cgbs from " + this.uftablename + "  where xzjk=? and lcid = ?";
        recordSet.executeQuery(sql, xzjk, workflowid);
        recordSet.next();
        this.url = Util.null2String(recordSet.getString("url"));
        this.mainid = Util.null2String(recordSet.getString("id"));
        this.fhzd = Util.null2String(recordSet.getString("fhzd"));
        this.cgbs = Util.null2String(recordSet.getString("cgbs"));
    }

    public RequestBodySecondUtil(String xzjk, String workflowid) {
        RecordSet recordSet = new RecordSet();
        String sql;
        sql = "select id,url,fhzd,cgbs from " + this.uftablename + "  where xzjk=? and lcid = ?";
        recordSet.executeQuery(sql, xzjk, workflowid);
        recordSet.next();
        this.url = Util.null2String(recordSet.getString("url"));
        this.mainid = Util.null2String(recordSet.getString("id"));
        this.fhzd = Util.null2String(recordSet.getString("fhzd"));
        this.cgbs = Util.null2String(recordSet.getString("cgbs"));
    }

    public String getCgbs() {
        return cgbs;
    }

    public String getFhzd() {
        return fhzd;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeaders() {
        RecordSet recordSet = new RecordSet();
        Map<String, List<String>> headers = new HashMap<>();
        String sql = "select headervalue,headername from " + this.uftablename + "_dt1  where mainid = ?";
        recordSet.executeQuery(sql, mainid);
        while (recordSet.next()) {
            ArrayList<String> valueList = new ArrayList<>();
            valueList.add(Util.null2String(recordSet.getString("headervalue")));
            headers.put(Util.null2String(recordSet.getString("headername")), valueList);
        }
        return headers;
    }

    public JSONObject requestUtil(String fjd, Map<String, String> mapMain, String tablename, String billid) {
        JSONObject jsonObject = new JSONObject();
        RecordSet recordSet = new RecordSet();
        RecordSet recordSet_mx = new RecordSet();
        String sql;
        sql = "select * from " + this.uftablename + "_dt2 where mainid = '" + this.mainid + "'  and fjd = '" + fjd + "'";
        if (fjd.isEmpty()) {
            sql = "select * from " + this.uftablename + "_dt2 where mainid = '" + this.mainid + "'  and fjd is null ";
        }
        recordSet.executeQuery(sql);
        while (recordSet.next()) {
            String jkzdm = Util.null2String(recordSet.getString("jkzdm"));
            String lczdm = Util.null2String(recordSet.getString("lczdm")).toLowerCase();
            String mxh = Util.null2String(recordSet.getString("mxh"));
            String isarray = Util.null2String(recordSet.getString("isarray"));
            String gdz = Util.null2String(recordSet.getString("gdz"));
            String tszd = Util.null2String(recordSet.getString("tszd"));

            switch (tszd) {
                case "0"://时间戳
                    jsonObject.put(jkzdm, System.currentTimeMillis());
                    break;
                case "1"://布尔类型
                    jsonObject.put(jkzdm, true);
                    if (Util.null2String(mapMain.get(lczdm)).equals("1")) {
                        jsonObject.put(jkzdm, false);
                    }
                    break;
                case "2"://集合
                    jsonObject.put(jkzdm, mapMain.get(lczdm).split(","));
                    break;
                default:
                    if (!StringUtil.isEmpty(gdz)) {
                        jsonObject.put(jkzdm, gdz);
                    } else {
                        if (lczdm.isEmpty()) {
                            if (mxh.equals("0")) {
                                JSONObject object = requestUtil(jkzdm, mapMain, tablename, billid);
                                if (isarray.equals("1")) {
                                    JSONArray array = new JSONArray();
                                    array.add(object);
                                    jsonObject.put(jkzdm, array);
                                } else {
                                    jsonObject.put(jkzdm, object);
                                }
                            } else {
                                JSONArray array = new JSONArray();
                                sql = "select * from " + tablename + "_dt" + mxh + " where mainid=" + billid;
                                recordSet_mx.executeQuery(sql);
                                while (recordSet_mx.next()) {
                                    String[] fields = recordSet_mx.getColumnName();
                                    Map<String, String> mapMain_dt = new HashMap<String, String>();
                                    for (String field : fields) {
                                        mapMain_dt.put(field.toLowerCase(), Util.null2String(recordSet_mx.getString(field)));
                                    }
                                    JSONObject jsonObject1 = requestUtil(jkzdm, mapMain_dt, tablename, billid);
                                    array.add(jsonObject1);
                                }
                                jsonObject.put(jkzdm, array);
                            }
                        } else {
                            jsonObject.put(jkzdm, Util.null2String(mapMain.get(lczdm)));
                        }
                    }
                    break;
            }
        }
        return jsonObject;
    }

}
