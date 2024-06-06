package E9ApiDemo;

import com.alibaba.fastjson.JSONObject;
import ln.TimeUtil;
import net.sf.json.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import weaver.general.Util;
import weaver.rsa.security.RSA;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 说明
 * 第三方系统调用泛微E9API接口（REST）创建流程
 */
public class RestCreatRequest {

    private static final String appid = "EEAA5436-7577-4BE0-8C6C-89E9D88805EB";//OA提供的APPID，查询数据库 SELECT * FROM ECOLOGY_BIZ_EC
    private static final String address = "http://127.0.0.1:8081";//OA系统地址
    private static final String userid = "1";//流程创建人id

    public static void main(String[] args) {

        /*调用注册接口
         * 此接口只需调用一次，调用成功后把secret和spk保存下来，后续调用接口可直接使用。*/
        String back = getRegist();
        JSONObject object = JSONObject.parseObject(back);
        String secrit = object.getString("secrit");
        String spk = object.getString("spk");

        /*获取token接口*/
        String bak2 = applytoken(secrit, spk);
        JSONObject objecttoken = JSONObject.parseObject(bak2);
        String token = objecttoken.getString("token");

        /*拼接heads*/
        Map<String, String> heads = new HashMap<String, String>();
        RSA rsa = new RSA();
        String rsa_userid = rsa.encrypt(null, userid, null, "utf8", spk, false);
        heads.put("token", token);
        heads.put("appid", appid);
        heads.put("userid", rsa_userid);
        System.out.println("rsa_userid**********" + rsa_userid);


        /*
         * 拼接参数
         * */

        /*********************************主表参数 start*****************************/

        JSONArray mainList = new JSONArray();

        JSONObject fieldmap = new JSONObject();
        fieldmap.put("fieldName", "werks");//流程字段名
        fieldmap.put("fieldValue", "测试字段一");//流程字段值
        mainList.add(fieldmap);

        JSONObject fieldmap2 = new JSONObject();
        fieldmap2.put("fieldName", "lgort");
        fieldmap2.put("fieldValue", "测试字段二");
        mainList.add(fieldmap2);


        //附件字段示例——以链接方式传参
        JSONObject fj_Path = new JSONObject();
        fj_Path.put("fieldName", "fjsc");

        List<JSONObject> fjList = new ArrayList<>();
        JSONObject fjmx = new JSONObject();
        fjmx.put("filePath", "https://manwei01.oss-cn-hangzhou.aliyuncs.com/pro/2023/07/08/20230708211723UMISS9TY8.pdf");
        fjmx.put("fileName", "附件字段测试.pdf");
        fjList.add(fjmx);

        fj_Path.put("fieldValue", fjList);
        mainList.add(fj_Path);


        //附件字段示例——以文件流方式传参
        JSONObject fj_Base64 = new JSONObject();
        fj_Base64.put("fieldName", "wjl");
        List<JSONObject> fjList_Base64 = new ArrayList<>();

        JSONObject fjmx2 = new JSONObject();
        //一定要以base64 开头 ，还需要关注下产品那边
        fjmx2.put("filePath", "base64:" + fileToBase64("D:/weaver-file/文本文档.txt"));
        fjmx2.put("fileName", "文本文档.txt");
        fjList_Base64.add(fjmx2);

        fj_Base64.put("fieldValue", fjList_Base64);
        mainList.add(fj_Base64);

        /***********************************主表参数 end*****************************/

        /***********************************明细表参数 start*****************************/
        //明细表参数非必填，需根据实际情况添加

        JSONArray dtlist = new JSONArray();

        /*  *********** 明细一 ******** */
        JSONObject dt1Map = new JSONObject();
        dt1Map.put("tableDBName", "formtable_main_8_dt1");//明细表表名

        List<JSONObject> workflowRequestTableRecords = new ArrayList<>();

        //第一行数据
        List<JSONObject> workflowRequestTableFields1 = new ArrayList<>();
        JSONObject workflowRequestTableFieldsMap1 = new JSONObject();

        JSONObject dtField11 = new JSONObject();
        dtField11.put("fieldName", "xm");
        dtField11.put("fieldValue", "张三");
        workflowRequestTableFields1.add(dtField11);

        JSONObject dtField12 = new JSONObject();
        dtField12.put("fieldName", "lgort");
        dtField12.put("fieldValue", "7");
        workflowRequestTableFields1.add(dtField12);

        workflowRequestTableFieldsMap1.put("recordOrder", "0");
        workflowRequestTableFieldsMap1.put("workflowRequestTableFields", workflowRequestTableFields1);
        workflowRequestTableRecords.add(workflowRequestTableFieldsMap1);

        //第二行数据
        List<JSONObject> workflowRequestTableFields2 = new ArrayList<>();
        JSONObject workflowRequestTableFieldsMap2 = new JSONObject();

        JSONObject dtField21 = new JSONObject();
        dtField21.put("fieldName", "xm");
        dtField21.put("fieldValue", "张三三");
        workflowRequestTableFields2.add(dtField21);

        JSONObject dtField22 = new JSONObject();
        dtField22.put("fieldName", "lgort");
        dtField22.put("fieldValue", "8");
        workflowRequestTableFields2.add(dtField22);

        workflowRequestTableFieldsMap2.put("recordOrder", "0");
        workflowRequestTableFieldsMap2.put("workflowRequestTableFields", workflowRequestTableFields2);
        workflowRequestTableRecords.add(workflowRequestTableFieldsMap2);

        dt1Map.put("workflowRequestTableRecords", workflowRequestTableRecords);


        dtlist.add(dt1Map);


        /*  ************* 明细二 ***************** */
        JSONObject dt2Map = new JSONObject();
        dt2Map.put("tableDBName", "formtable_main_8_dt2");//明细表表名

        List<JSONObject> workflowRequestTableRecords2 = new ArrayList<>();

        //第一行数据
        List<JSONObject> workflowRequestTableFields1_dt2 = new ArrayList<>();
        JSONObject workflowRequestTableFieldsMap1_dt2 = new JSONObject();

        JSONObject dtField1_dt2 = new JSONObject();
        dtField1_dt2.put("fieldName", "wb");
        dtField1_dt2.put("fieldValue", "测试");
        workflowRequestTableFields1_dt2.add(dtField1_dt2);

        JSONObject dtField2_dt2 = new JSONObject();
        dtField2_dt2.put("fieldName", "rlzy");
        dtField2_dt2.put("fieldValue", "8");
        workflowRequestTableFields1_dt2.add(dtField2_dt2);

        workflowRequestTableFieldsMap1_dt2.put("recordOrder", "0");
        workflowRequestTableFieldsMap1_dt2.put("workflowRequestTableFields", workflowRequestTableFields1_dt2);
        workflowRequestTableRecords2.add(workflowRequestTableFieldsMap1_dt2);

        //第二行数据
        List<JSONObject> workflowRequestTableFields2_dt2 = new ArrayList<>();
        JSONObject workflowRequestTableFieldsMap2_dt2 = new JSONObject();

        JSONObject dtField21_dt2 = new JSONObject();
        dtField21_dt2.put("fieldName", "wb");
        dtField21_dt2.put("fieldValue", "测试第二行");
        workflowRequestTableFields2_dt2.add(dtField21_dt2);

        JSONObject dtField22_dt2 = new JSONObject();
        dtField22_dt2.put("fieldName", "rlzy");
        dtField22_dt2.put("fieldValue", "7");
        workflowRequestTableFields2_dt2.add(dtField22_dt2);

        workflowRequestTableFieldsMap2_dt2.put("recordOrder", "0");
        workflowRequestTableFieldsMap2_dt2.put("workflowRequestTableFields", workflowRequestTableFields2_dt2);
        workflowRequestTableRecords2.add(workflowRequestTableFieldsMap2_dt2);

        dt2Map.put("workflowRequestTableRecords", workflowRequestTableRecords2);

        dtlist.add(dt2Map);

        /***********************************明细表参数 end*****************************/


        //其他参数,非必填
        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("isnextflow ", "1");//新建流程是否默认提交到第二节点，可选值为[0 ：不流转 1：流转 (默认)]
        otherParams.put("delReqFlowFaild ", "1");//新建流程失败是否默认删除流程，可选值为[0 ：不删除 1：删除 (默认)]


        Map<String, String> inMap = new HashMap<>();
        inMap.put("requestName", "测试创建流程" + TimeUtil.getCurrentTimeString());//流程标题
        inMap.put("workflowId", "6");//流程id
        inMap.put("mainData", mainList.toString());//主表参数
        inMap.put("detailData", dtlist.toString());//明细表参数，非必填,无明细表可省略该行
        inMap.put("otherParams", otherParams.toString());//其他参数,非必填
        inMap.put("remark", "测试签字意见");//签字意见，默认值流程默认意见若未设置则为空,非必填


        /*调用创建流程接口*/
        String back1 = httpPostForm(address + "/api/workflow/paService/doCreateRequest", inMap, heads, "utf-8");
        System.out.println("调用创建流程接口返回：" + back1);
        //返回成功示例   {"code":"SUCCESS","data":{"requestid":447473},"errMsg":{},"reqFailMsg":{"keyParameters":{},"msgInfo":{},"otherParams":{"doAutoApprove":"0"}}}

    }

    /**
     * 注册接口
     * 此接口只需调用一次，调用成功后把secret和spk保存下来，后续调用接口会用到。
     *
     * @return
     */
    public static String getRegist() {
        Map<String, String> heads = new HashMap<String, String>();
        String cpk = new RSA().getRSA_PUB();//OA提供RSA PublicKey。 文件路径：ecology/WEB-INF/lib/keys/rsa_2048_pub.key
        heads.put("appid", appid);//OA提供的APPID
        heads.put("cpk", cpk);
        String data = httpPostForm(address + "/api/ec/dev/auth/regist", null, heads, "utf-8");
        System.out.println("***************getRegist_data**********");
        System.out.println(cpk);
        System.out.println(data);

        JSONObject jsonObject = JSONObject.parseObject(data);
        String secret = Util.null2String(jsonObject.getString("secret"));
        String spk = Util.null2String(jsonObject.getString("spk"));

        System.out.println(secret);
        System.out.println(spk);

        return data;
    }

    /**
     * 获取token
     *
     * @param secrit
     * @param spk
     * @return
     */
    public static String applytoken(String secrit, String spk) {

        Map<String, String> heads = new HashMap<String, String>();
        RSA rsa = new RSA();
        String secret_2 = rsa.encrypt(null, secrit, null, "utf-8", spk, false);
        heads.put("appid", appid);//OA提供的APPID
        heads.put("secret", secret_2);
        String data = httpPostForm(address + "/api/ec/dev/auth/applytoken", null, heads, "utf-8");
        System.out.println("***************applytoken_data**********");
        System.out.println(secret_2);
        System.out.println(data);
        return data;
    }


    /**
     * 发送 http post 请求，参数以form表单键值对的形式提交。
     */
    public static String httpPostForm(String url, Map<String, String> params, Map<String, String> headers, String encode) {

        if (encode == null) {
            encode = "utf-8";
        }

        String content = null;
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        try {

            closeableHttpClient = HttpClients.createDefault();
            HttpPost httpost = new HttpPost(url);

            //设置header
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            //组织请求参数
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            if (params != null && params.size() > 0) {
                Set<String> keySet = params.keySet();
                for (String key : keySet) {
                    paramList.add(new BasicNameValuePair(key, params.get(key)));
                }
            }
            httpost.setEntity(new UrlEncodedFormEntity(paramList, encode));


            httpResponse = closeableHttpClient.execute(httpost);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {  //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 将文件转为Base64文件流
     *
     * @param str 文件地址
     * @return Base64文件流
     */
    private static String fileToBase64(String str) {
        File file = new File(str);
        if (!file.exists()) {
            return null;
        }
        // base64编码
        String context = null;
        try {
            FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            inputFile.read(buffer);
            inputFile.close();
            context = Base64.getEncoder().encodeToString(buffer).replaceAll("\r|\n", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return context;
    }

}
