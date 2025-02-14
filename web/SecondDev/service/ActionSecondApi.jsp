<%@ page import="weaver.general.Util" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="cn.hutool.json.JSONUtil" %>
<%@ page import="SecondDev.util.RequestBodySecondUtil" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    response.setContentType("application/json;charset=utf-8");
    out.clear();

    String xzjk = Util.null2String(request.getParameter("xzjk"));
    String workflowid = Util.null2String(request.getParameter("workflowid"));
    String requestid = Util.null2String(request.getParameter("requestid"));

    String tablename = getTableNameByRequestId(workflowid);
    RecordSet recordSet = new RecordSet();
    String sql;

    sql = "select * from " + tablename + " where requestid=" + requestid;
    recordSet.executeQuery(sql);
    recordSet.next();
    String billid = Util.null2String(recordSet.getString("id"));
    String[] fields = recordSet.getColumnName();
    Map<String, String> mapMain = new HashMap<String, String>();
    for (String field : fields) {
        mapMain.put(field.toLowerCase(), Util.null2String(recordSet.getString(field)));
    }

    RequestBodySecondUtil requestBodySecondUtil = new RequestBodySecondUtil(xzjk, workflowid);
    String url = requestBodySecondUtil.getUrl();
    Map<String, List<String>> headers = requestBodySecondUtil.getHeaders();
    JSONObject jsonObject = requestBodySecondUtil.requestUtil("", mapMain, tablename, billid);

    out.println("----url----\n" + url);
    out.println("----headers----\n" + headers);
    out.println("----jsonObject----\n" + JSONUtil.toJsonPrettyStr(jsonObject));
    out.println("----time----");

%>
<%!

    private String getTableNameByRequestId(String workflowId) {
        RecordSet rs = new RecordSet();
        String sql = "select a.tablename from workflow_bill a, workflow_base b where a.id=b.formid and b.id=" + workflowId;
        rs.execute(sql);
        rs.next();
        return rs.getString(1);
    }

%>