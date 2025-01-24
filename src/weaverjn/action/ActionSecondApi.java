package weaverjn.action;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.engine.edc.biz.form.FormNameBiz;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import weaverjn.util.RequestBodySecondUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ayuan
 * date 2025/1/7
 */

public class ActionSecondApi extends BaseBean implements Action {
    private String xzjk;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(RequestInfo requestInfo) {


        String requestid = requestInfo.getRequestid();
        String workflowid = requestInfo.getWorkflowid();
        RequestManager manager = requestInfo.getRequestManager();
        int formid = manager.getFormid();
        FormNameBiz formNameBiz = new FormNameBiz();
        String tablename = formNameBiz.getTableNameByFormId(formid);

        if (xzjk.isEmpty()) {
            manager.setMessageid(requestid);
            manager.setMessagecontent("请在节点动作中配置选择接口参数。");
            return "0";
        }

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

        try {
            RequestBodySecondUtil requestBodySecondUtil = new RequestBodySecondUtil(xzjk, workflowid);
            String url = requestBodySecondUtil.getUrl();
            String fhzd = requestBodySecondUtil.getFhzd();
            String cgbs = requestBodySecondUtil.getCgbs();
            Map<String, List<String>> headers = requestBodySecondUtil.getHeaders();
            JSONObject jsonObject = requestBodySecondUtil.requestUtil("", mapMain, tablename, billid);
            logger.info("url===" + url);
            logger.info("headers===" + headers);
            logger.info("jsonObject===" + jsonObject);

            String result = HttpRequest.post(url)
                    .body(jsonObject.toJSONString(), "UTF-8")
                    .header(headers)
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            logger.info("result===" + result);
            if (!fhzd.isEmpty()) {
                JSONObject object = JSONObject.parseObject(result);
                String status = Util.null2String(object.getString(fhzd));
                if (!status.equals(cgbs)) {
                    manager.setMessageid(requestid);
                    manager.setMessagecontent(result);
                    return "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            manager.setMessageid(requestid);
            manager.setMessagecontent("Exception e：" + e.getMessage());
            return "0";
        }
        return "1";
    }

}