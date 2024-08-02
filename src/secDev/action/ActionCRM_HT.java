package secDev.action;

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
import java.util.Map;

/**
 * @author ayuan
 * date 2024/7/28
 */

public class ActionCRM_HT extends BaseBean implements Action {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(RequestInfo requestInfo) {

        String requestid = requestInfo.getRequestid();
        RequestManager manager = requestInfo.getRequestManager();
        int formid = manager.getFormid();

        FormNameBiz formNameBiz = new FormNameBiz();
        String tablename = formNameBiz.getTableNameByFormId(formid);
        RecordSet recordSet = new RecordSet();
        String sql;

        sql = "select * from uf_srmapi where jk = 0";
        recordSet.execute(sql);
        if (recordSet.next()) {
            String url = Util.null2String(recordSet.getString("url"));
            String urlToken = Util.null2String(recordSet.getString("urlToken"));

            int billid = Util.getIntValue(recordSet.getString("id"));


            Map<String, String> map = new HashMap<>();
            sql = "select * from uf_srmapi_dt1 where mainid = " + billid;
            recordSet.execute(sql);
            while (recordSet.next()) {
                map.put(Util.null2String(recordSet.getString("jkzd")), Util.null2String(recordSet.getString("oazd")));
            }

            JSONObject json= new JSONObject();
            sql = "select * from " + tablename + "  where requestid=?";
            recordSet.executeQuery(sql, requestid);
            recordSet.next();

            for (String jkzd: map.keySet()) {
                json.put(jkzd,Util.null2String(recordSet.getString(map.get(jkzd))));
            }

            String token = getTokken(urlToken);
            logger.info("token---" + token);
            logger.info("json---" + json.toJSONString());
            String result = HttpRequest.post(url)
                    .header("Authorization", token)
                    .body(json.toJSONString())
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            logger.info("result---" + result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (!Util.null2String(jsonObject.getString("code")).equals("200")) {
                manager.setMessageid(requestid);
                manager.setMessagecontent(Util.null2String(jsonObject.getString("msg")));
                return "0";
            }
            return "1";
        } else {
            manager.setMessageid(requestid);
            manager.setMessagecontent("未配置建模表接口信息");
            return "0";
        }

    }

    private String getTokken(String url) {
//        String url = getPropValue("ActionCRM", "urlToken");
        String result = HttpRequest.post(url)
                .timeout(20000)//超时，毫秒
                .execute().body();
        JSONObject jsonObject = JSONObject.parseObject(result);
        return Util.null2String(jsonObject.getString("access_token"));
    }
}
