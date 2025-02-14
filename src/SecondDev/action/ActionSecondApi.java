package SecondDev.action;

import SecondDev.util.RequestBodySecondUtil;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ayuan
 * date 2025/1/7
 */

public class ActionSecondApi extends BaseBean implements Action {

    private String xzjk;//参数：选择接口，对应建模表选择接口下拉框id
    private int type = 0;//默认输出日志

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void Log(Object str) {
        if (type == 0) {
            logger.info(str);
        }
    }

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
            //默认建模配置表表名 uf_ActionSecondApi
            RequestBodySecondUtil requestBodySecondUtil = new RequestBodySecondUtil(xzjk, workflowid);
            String url = requestBodySecondUtil.getUrl();
            String fhzd = requestBodySecondUtil.getFhzd();
            String cgbs = requestBodySecondUtil.getCgbs();
            type = requestBodySecondUtil.getLog();
            Map<String, List<String>> headers = requestBodySecondUtil.getHeaders();
            JSONObject jsonObject = requestBodySecondUtil.requestUtil("", mapMain, tablename, billid);
            Log("url===" + url);
            Log("headers===" + headers);
            Log("jsonObject===" + jsonObject);

            String result = HttpRequest.post(url)
                    .body(jsonObject.toJSONString(), "UTF-8")
                    .header(headers)
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            Log("result===" + result);

            //通过返回字段和成功标识判断接口是否成功
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