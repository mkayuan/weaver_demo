package weaverjn.action;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ActionCallBack extends BaseBean implements Action {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(RequestInfo requestInfo) {

        String url = getPropValue("ActionCallBack", "url");
        String appKey = getPropValue("ActionCallBack", "appKey");

        String requestid = requestInfo.getRequestid();
        String workflowid = requestInfo.getWorkflowid();
        RequestManager manager = requestInfo.getRequestManager();
        String src = manager.getSrc();
        String remark = manager.getRemark();
        String s = Util.delHtmlWithSpace(remark);
        int userid = manager.getUser().getUID();
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery("select workcode from hrmresource where id = ?", userid);
        recordSet.next();
        JSONObject arr_item = new JSONObject();
        arr_item.put("requestid", requestid);
        arr_item.put("workflowid", workflowid);
        arr_item.put("result", src);
        arr_item.put("remark", s);

        arr_item.put("userid", userid);
        arr_item.put("workcode", Util.null2String(recordSet.getString("workcode")));
        logger.info("json:" + arr_item);


//        String ZTYPE = mt_result_rsp.getZTYPE();
//        String ZMESSAGE = mt_result_rsp.getZMESSAGE();
//        logger.info(requestid+ "接口执行完毕：" + ZTYPE + "," + ZMESSAGE);
//        if (!"S".equals(ZTYPE)) {
//            manager.setMessageid(requestid);
//            manager.setMessagecontent("调用接口报错：" + ZMESSAGE);
//            return "0";
//        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, arr_item.toJSONString());
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("appKey", appKey)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String string = Util.null2String(response.body().string());
            logger.info("string:" + string);
            JSONObject jsonObject = JSONObject.parseObject(string);
            if (!Util.str2bool(jsonObject.getString("isok"))) {
                manager.setMessageid(requestid);
                manager.setMessagecontent("接口请求错误！" + Util.null2String(jsonObject.getString("message")));
                return "0";
            }
        } catch (IOException e) {
            e.printStackTrace();
            manager.setMessageid(requestid);
            manager.setMessagecontent("接口异常。" + e.getMessage());
            return "0";
        }
        return "1";
    }
}
