package jier2025.schedule;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.job.JobTitlesComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.interfaces.hrm.HrmServiceManager;
import weaver.interfaces.hrm.OrgXmlBean;
import weaver.interfaces.hrm.UserBean;
import weaver.interfaces.schedule.BaseCronJob;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SynOrg extends BaseCronJob {
    HrmServiceManager manager = new HrmServiceManager();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute() {

        try {
            RecordSet recordSet = new RecordSet();
            recordSet.executeQuery("select * from uf_config_syn");
            recordSet.next();
            String syntime = Util.null2String(recordSet.getString("syntime"));
            String password = Util.null2String(recordSet.getString("password"));
            String synhrmurl = Util.null2String(recordSet.getString("synhrmurl"));
            String syndepturl = Util.null2String(recordSet.getString("syndepturl"));
            String businessSystemCode = Util.null2String(recordSet.getString("businessSystemCode"));
            String tableCode = Util.null2String(recordSet.getString("tableCode"));
            String date = "";
            if (syntime.equals("")) {
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
                date = yesterday.format(formatter);
            } else {
                date = syntime;
            }

            ConnectionPool CONNECTION_POOL = new ConnectionPool(5, 5 * 60, SECONDS);
            JSONObject getOrgData = getOrgData(businessSystemCode, tableCode, syndepturl, CONNECTION_POOL, date);
            if (getOrgData.getBoolean("status")) {
                JSONArray datas = JSONArray.parseArray(getOrgData.getString("data"));
//                logger.info("分部部门:" + datas.toJSONString());
                //同步分部
                synCom(datas);
                //同步部门
                synDept(datas);
                //清缓存
                SubCompanyComInfo subCompanyComInfo = new SubCompanyComInfo();
                subCompanyComInfo.removeCache();
                DepartmentComInfo departmentComInfo = new DepartmentComInfo();
                departmentComInfo.removeCache();
            } else {
//                    logger.info("获取分部部门数据失败!" + getDataDept.getString("msg"));
                logger.info("获取分部部门数据失败!");
            }
            JSONObject getDataHrm = getDataHrm("", synhrmurl, CONNECTION_POOL, date);
            if (getDataHrm.getBoolean("status")) {
                JSONArray datasHrm = JSONArray.parseArray(getDataHrm.getString("data"));
                logger.info("人员:" + datasHrm.toJSONString());
                //同步人员
                synHrm(datasHrm, password);
            } else {
//                        logger.info("获取人员数据失败!" + getDataHrm.getString("msg"));
                logger.info("获取人员数据失败!");
            }
            //清缓存
            ResourceComInfo resourceComInfo = new ResourceComInfo();
            resourceComInfo.removeCache();


            CONNECTION_POOL.evictAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public JSONObject getOrgData(String businessSystemCode, String tableCode, String url, ConnectionPool CONNECTION_POOL, String date) {
        JSONObject resultReturn = new JSONObject();
        resultReturn.put("status", false);


        JSONObject param = new JSONObject();
        param.put("businessSystemCode", businessSystemCode);
        param.put("tableCode", tableCode);
        param.put("mode", "all");
        param.put("deltaTime", date);
        param.put("order", "");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectionPool(CONNECTION_POOL)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, param.toJSONString());
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("X-ECC-Current-Tenant", "10000")
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            JSONObject result = JSONObject.parseObject(string);
            if (result != null) {
                if (Util.null2String(result.getString("status")).equals("success")) {
                    resultReturn.put("status", true);
                    resultReturn.put("data", result.getString("datas"));
                    return resultReturn;
                } else {
                    resultReturn.put("msg", string);
                }
            } else {
                resultReturn.put("msg", string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultReturn.put("msg", e.getMessage());
        }
        return resultReturn;
    }

    public JSONObject getDataHrm(String token, String url, ConnectionPool CONNECTION_POOL, String date) {
        JSONObject resultReturn = new JSONObject();
        resultReturn.put("status", false);

//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .connectionPool(CONNECTION_POOL)
//                .build();
//        MediaType mediaType = MediaType.parse("application/json");
//        Request request = new Request.Builder()
//                .url(url + "?syncDate=" + date)
//                .method("GET", null)
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Token", token)
//                .build();
//        try {
//            Response response = client.newCall(request).execute();
//            String string = response.body().string();
//            JSONObject result = JSONObject.parseObject(string);
//            if (result != null) {
//                if (Util.null2String(result.getString("msg")).equals("success")) {
//                    resultReturn.put("status", true);
//                    resultReturn.put("data", result.getString("data"));
//                    return resultReturn;
//
//                } else {
//                    resultReturn.put("msg", string);
//                }
//            } else {
//                resultReturn.put("msg", string);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return resultReturn;
    }

    public void synCom(JSONArray data) {
        if (data.size() > 0) {
            String sql = "";
            RecordSet recordSet = new RecordSet();
            for (int i = 0; i < data.size(); i++) {
                OrgXmlBean orgBean = new OrgXmlBean();
                JSONObject obj = (JSONObject) data.get(i);
                String code = Util.null2String(obj.getString("code")).toUpperCase();
                //U开头是公司，D开头是部⻔
                if (code.startsWith("U")) {
                    String zsjid = Util.null2String(obj.getString("id"));
                    String name_chs = Util.null2String(obj.getString("name_chs"));
                    String ishistory = Util.null2String(obj.getString("ishistory"));//0是停用
                    String pptrpkkid = Util.null2String(obj.getString("parentid_parentelement"));
                    //是否封存
                    if (ishistory.equals("0")) {
                        orgBean.setCanceled("1");
                    }

                    sql = "select subcomid from hrmsubcompanydefined where zsjid = '" + zsjid + "'";
                    recordSet.executeQuery(sql);
                    if (recordSet.next()) {
                        String comid = Util.null2String(recordSet.getString("subcomid"));
                        sql = "select subcompanycode from hrmsubcompany where id = " + comid;
                        recordSet.executeQuery(sql);
                        recordSet.next();
                        String subcompanycode = Util.null2String(recordSet.getString("subcompanycode"));
                        orgBean.setCode(subcompanycode);
                    } else {
                        orgBean.setCode(code);
                    }

                    sql = "select subcomid from hrmsubcompanydefined where zsjid = '" + pptrpkkid + "'";
                    recordSet.executeQuery(sql);
                    if (recordSet.next()) {
                        String comid = Util.null2String(recordSet.getString("subcomid"));
                        sql = "select subcompanycode,showorder from hrmsubcompany where id = " + comid;
                        recordSet.executeQuery(sql);
                        recordSet.next();
                        orgBean.setParent_code(Util.null2String(recordSet.getString("subcompanycode")));
                        String orderder = Util.null2String(recordSet.getString("showorder"));
                        String integerStr = String.valueOf((int) Double.parseDouble(orderder));
                        orgBean.setOrder(integerStr);
                    } else {
                        orgBean.setParent_code("0");
                    }
                    orgBean.setFullname(name_chs);
                    orgBean.setShortname(name_chs);
//                    logger.info("分部:" + JSONObject.toJSONString(orgBean));
                    manager.addSubCompany(orgBean);
                }
            }
        }
    }

    public void synDept(JSONArray data) {
        if (data.size() > 0) {
            String sql = "";
            RecordSet recordSet = new RecordSet();
            for (int i = 0; i < data.size(); i++) {
                OrgXmlBean orgBean = new OrgXmlBean();
                JSONObject obj = (JSONObject) data.get(i);
                String code = Util.null2String(obj.getString("code")).toUpperCase();
                //U开头是公司，D开头是部⻔
                if (code.startsWith("D")) {
                    String zsjid = Util.null2String(obj.getString("id"));
                    String name_chs = Util.null2String(obj.getString("name_chs"));
                    String ishistory = Util.null2String(obj.getString("ishistory"));//0是停用
                    String pptrpkid = Util.null2String(obj.getString("parentid_parentelement"));

                    if (ishistory.equals("0")) {
                        orgBean.setCanceled("1");
                    }
                    //显示顺序
                    String orderid = Util.null2String(obj.getString(""));

                    sql = "select deptid from hrmdepartmentdefined where zsjid = '" + zsjid + "'";
                    recordSet.executeQuery(sql);
                    if (recordSet.next()) {
                        String deptid = Util.null2String(recordSet.getString("deptid"));
                        sql = "select departmentcode,showorder from hrmdepartment where id = " + deptid;
                        recordSet.executeQuery(sql);
                        recordSet.next();
                        String showorder = Util.null2String(recordSet.getString("showorder"));
                        String subcompanycode = Util.null2String(recordSet.getString("subcompanycode"));
                        orgBean.setOrder(showorder);
                        orgBean.setCode(subcompanycode);
                    } else {
                        orgBean.setCode(code);
                        orgBean.setOrder(orderid);
                    }



                    sql = "select deptid from hrmdepartmentdefined where zsjid = '" + pptrpkid + "'";
                    recordSet.executeQuery(sql);
                    if (recordSet.next()) {
                        //二级部门
                        String deptid = Util.null2String(recordSet.getString("deptid"));
                        sql = "select subcompanyid1,departmentcode from hrmdepartment where id = " + deptid;
                        recordSet.executeQuery(sql);
                        recordSet.next();
                        orgBean.setParent_code(Util.null2String(recordSet.getString("departmentcode")));
                        String subcompanyid1 = Util.null2String(recordSet.getString("subcompanyid1"));

                        sql = "select subcompanycode from hrmsubcompany where id=" + subcompanyid1;
                        recordSet.execute(sql);
                        recordSet.next();
                        String orgcode = recordSet.getString("subcompanycode");
                        orgBean.setOrg_code(orgcode);
                    } else {
                        //一级部门
                        orgBean.setParent_code("");
                        sql = "select subcomid from hrmsubcompanydefined where rzxtgsbh = '" + pptrpkid + "'";
                        recordSet.executeQuery(sql);
                        recordSet.next();
                        String comid = Util.null2String(recordSet.getString("subcomid"));
                        sql = "select subcompanycode from hrmsubcompany where id = " + comid;
                        recordSet.executeQuery(sql);
                        recordSet.next();
                        orgBean.setOrg_code(Util.null2String(recordSet.getString("subcompanycode")));
                    }
                    orgBean.setFullname(name_chs);
                    orgBean.setShortname(name_chs);
//                    logger.info("部门:" + JSONObject.toJSONString(orgBean));
                    manager.addDepartment(orgBean);

                }
            }
        }
    }


    public void synHrm(JSONArray data, String password) {
        if (data.size() > 0) {
            String sql = "";
            RecordSet recordSet = new RecordSet();
            for (int i = 0; i < data.size(); i++) {
                JSONObject obj = (JSONObject) data.get(i);
                UserBean userBean = new UserBean();
                String id = Util.null2String(obj.getString("id"));
                String code = Util.null2String(obj.getString("code"));
                String name_chs = Util.null2String(obj.getString("name_chs"));
                String gender = Util.null2String(obj.getString("gender"));
                String mobile = Util.null2String(obj.getString("mobile"));
                String telephone = Util.null2String(obj.getString("telephone"));
                String fb_id = Util.null2String(obj.getString("ext_8_Lv9"));
                String status = Util.null2String(obj.getString("ext_18_Lv9"));//在岗 离职 退休 见习 试用 见习期离职 内退 试用离职 待岗
                String bm_id = Util.null2String(obj.getString("adminorg"));

                String gw = Util.null2String(obj.getString("a09q6"));
                String email = Util.null2String(obj.getString("a09q6"));
                String sfz = Util.null2String(obj.getString("a09q6"));
                String jobtitlecode = getJobtitlecode(gw);


//                if (optype.equals("U") && !u2502.equals("")) {
//                    userBean.setStatus("5");
//                } else {
//                    userBean.setStatus("1");
//                }


                sql = "select id from cus_fielddata where scope = 'HrmCustomFieldByInfoType' and SCOPEID = '-1' and field0? = ?";
                recordSet.executeQuery(sql,id);
                //新用户
                if (!recordSet.next()) {
                    //只有新用户的情况下设置密码和判断登录名是否重复
                    userBean.setPassword(password);
                    sql = "select count(*) as num from hrmresource where lastname = '" + name_chs + "'";
                    recordSet.executeQuery(sql);
                    if (recordSet.next()) {
                        int index = Util.getIntValue(recordSet.getString("num"), 0) + 1;
                        String loginid = name_chs + Util.null2String(getMap().get(index));
                        userBean.setLoginid(loginid);
                    } else {
                        userBean.setLoginid(name_chs);
                    }
                }else{
                    String userid = Util.null2String(recordSet.getString("id"));
                    recordSet.executeQuery("select workcode from hrmresource where id = ?",userid);
                    recordSet.next();
                    code = Util.null2String(recordSet.getString("workcode"));

                }
                //身份证当工号
                userBean.setWorkcode(code);
                userBean.setLastname(name_chs);

                sql = "select subcomid from hrmsubcompanydefined where zsjid = '" + fb_id + "'";
                recordSet.executeQuery(sql);
                recordSet.next();
                String comid = Util.null2String(recordSet.getString("subcomid"));
                sql = "select subcompanycode from hrmsubcompany where id = " + comid;
                recordSet.executeQuery(sql);
                recordSet.next();
                userBean.setSubcompanycode(Util.null2String(recordSet.getString("subcompanycode")));



                sql = "select deptid from hrmdepartmentdefined where zsjid = '" + bm_id + "'";
                recordSet.executeQuery(sql);
                recordSet.next();
                String deptid = Util.null2String(recordSet.getString("deptid"));
                sql="select departmentcode from hrmdepartment where id = ?";
                recordSet.executeQuery(sql,deptid);
                recordSet.next();
                userBean.setDepartmentcode(Util.null2String(recordSet.getString("departmentcode")));
                userBean.setJobtitlecode(jobtitlecode);
                userBean.setCertificatenum(sfz);

                userBean.setEmail(email);
                userBean.setMobile(mobile);
                userBean.setTelephone(telephone);
                userBean.setSex(gender);
                userBean.setSeclevel("10");
                manager.synHrmResource(userBean);
//                logger.info("人员" + JSONObject.toJSONString(userBean));
//                sql = "select id,password,dsporder from hrmresource where workcode = '" + a0173 + "'";
//                recordSet.executeQuery(sql);
//                boolean exist = recordSet.next();

//                sql = "update hrmresource set locationid=2 where workcode='" + a0173 + "'";
//                recordSet.execute(sql);

                //更新顺序字段
//                sql = "select id,password,dsporder from hrmresource where workcode = '" + a0173 + "'";
//                recordSet.executeQuery(sql);
//                recordSet.next();
//                if (!exist) {
//                    String userid = Util.null2String(recordSet.getString("id"));
//                    sql = "update hrmresource set dsporder = 999 where  id = " + userid;
//                    recordSet.executeUpdate(sql);
//
//
//                }

            }
        }
    }

    private static String getJobtitlecode(String jobtitlename) {
        JobTitlesComInfo jobTitlesComInfo = new JobTitlesComInfo();
        RecordSet rs = new RecordSet();
        String sql;
        sql = "select id,jobtitlecode from HrmJobTitles where jobactivityid=1501 and jobtitlename=?";
        rs.executeQuery(sql, jobtitlename);
        String jobtitlecode;
        if (rs.next()) {
            int id = rs.getInt("id");
            jobtitlecode = Util.null2String(rs.getString("jobtitlecode"));
            if (jobtitlecode.isEmpty()) {
                jobtitlecode = IdUtil.simpleUUID();
                sql = "update HrmJobTitles set jobtitlecode=? where id=?";
                rs.executeUpdate(sql, jobtitlecode, id);
                jobTitlesComInfo.removeJobTitlesCache();
            }
        } else {
            jobtitlecode = IdUtil.simpleUUID();
            sql = "insert into HrmJobTitles(jobtitlecode,jobtitlename,jobtitlemark,jobactivityid) values(?,?,?,?)";
            rs.executeUpdate(sql, jobtitlecode, jobtitlename, jobtitlename, 1501);
        }
        return jobtitlecode;
    }

    private Map<Integer, Character> getMap() {
        Map<Integer, Character> map = new HashMap<>();
        for (int i = 2; i <= 27; i++) {
            char ch = (char) ('a' + (i - 2));
            map.put(i, ch);
        }
        return map;
    }
}
