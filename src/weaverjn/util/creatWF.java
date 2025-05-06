package weaverjn.util;

import com.weaver.general.Util;
import weaver.workflow.webservices.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class creatWF {

    public static void main(String[] args) {

        //主表字段
        Map<String, String> mapMain = new HashMap<>();
        mapMain.put("字段名", "字段值");

        //明细一字段
        List<Map<String, String>> dt1data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("明细字段名", "明细字段值");
        dt1data.add(map);

        String requestid = createWf(mapMain, "测试流程主表", "6", "1");
        String requestid_1 = createWf(mapMain, dt1data, "测试流程明细表", "6", "1");

        System.out.println("requestid---" + requestid);
        System.out.println("requestid_1---" + requestid_1);

    }

    /**
     * 创建只有主表流程
     *
     * @param mapMain    字段信息
     * @param lcbt       流程标题
     * @param workflowid 流程id
     * @param userid     创建人
     * @return 流程requestid
     */
    private static String createWf(Map<String, String> mapMain, String lcbt, String workflowid, String userid) {
        WorkflowServiceImpl service = new WorkflowServiceImpl();
        WorkflowRequestTableField[] requestTableFields = new WorkflowRequestTableField[mapMain.size()];
        int i = 0;
        for (String k : mapMain.keySet()) {
            String v = mapMain.get(k);
            requestTableFields[i] = new WorkflowRequestTableField();
            requestTableFields[i].setFieldName(k);
            requestTableFields[i].setFieldValue(v);
            requestTableFields[i].setView(true);
            requestTableFields[i].setEdit(true);
            i++;
        }
        WorkflowRequestTableRecord[] requestTableRecords = new WorkflowRequestTableRecord[1];
        requestTableRecords[0] = new WorkflowRequestTableRecord();
        requestTableRecords[0].setWorkflowRequestTableFields(requestTableFields);
        WorkflowMainTableInfo mainTableInfo = new WorkflowMainTableInfo();
        mainTableInfo.setRequestRecords(requestTableRecords);

        WorkflowBaseInfo baseInfo = new WorkflowBaseInfo();
        baseInfo.setWorkflowId(workflowid);

        WorkflowRequestInfo requestInfo = new WorkflowRequestInfo();
        requestInfo.setRequestName(lcbt);

        requestInfo.setCreatorId(userid);
        requestInfo.setRequestLevel("0");//紧急程度

        requestInfo.setIsnextflow("0");//是否自动流转到创建人的下一个节点
        requestInfo.setWorkflowMainTableInfo(mainTableInfo);
        requestInfo.setWorkflowBaseInfo(baseInfo);

        String requestid = service.doCreateWorkflowRequest(requestInfo, weaver.general.Util.getIntValue(userid));

        return requestid;
    }

    /**
     * 创建带有明细一的流程
     *
     * @param mapMain    字段信息
     * @param dt1data    明细一字段信息
     * @param lcbt       流程标题
     * @param workflowid 流程id
     * @param userid     创建人
     * @return 流程requestid
     */
    private static String createWf(Map<String, String> mapMain, List<Map<String, String>> dt1data, String lcbt, String workflowid, String userid) {
        WorkflowServiceImpl service = new WorkflowServiceImpl();
        WorkflowRequestTableField[] requestTableFields = new WorkflowRequestTableField[mapMain.size()];
        int i = 0;
        for (String k : mapMain.keySet()) {
            String v = mapMain.get(k);
            requestTableFields[i] = new WorkflowRequestTableField();
            requestTableFields[i].setFieldName(k);
            requestTableFields[i].setFieldValue(Util.null2String(v));
            requestTableFields[i].setView(true);
            requestTableFields[i].setEdit(true);
            i++;
        }
        WorkflowRequestTableRecord[] requestTableRecords = new WorkflowRequestTableRecord[1];
        requestTableRecords[0] = new WorkflowRequestTableRecord();
        requestTableRecords[0].setWorkflowRequestTableFields(requestTableFields);
        WorkflowMainTableInfo mainTableInfo = new WorkflowMainTableInfo();
        mainTableInfo.setRequestRecords(requestTableRecords);

        WorkflowDetailTableInfo[] detailTableInfos = new WorkflowDetailTableInfo[1];//2,明细表个数

        //明细一
        detailTableInfos[0] = new WorkflowDetailTableInfo();
        requestTableRecords = new WorkflowRequestTableRecord[dt1data.size()];
        int j = 0;
        for (Map<String, String> map : dt1data) {
            requestTableFields = new WorkflowRequestTableField[map.size()];
            i = 0;
            for (String k : map.keySet()) {
                String v = map.get(k);
                requestTableFields[i] = new WorkflowRequestTableField();
                requestTableFields[i].setFieldName(k);
                requestTableFields[i].setFieldValue(Util.null2String(v));
                requestTableFields[i].setView(true);
                requestTableFields[i].setEdit(true);
                i++;
            }
            requestTableRecords[j] = new WorkflowRequestTableRecord();
            requestTableRecords[j].setWorkflowRequestTableFields(requestTableFields);
            j++;
        }

        detailTableInfos[0].setWorkflowRequestTableRecords(requestTableRecords);

        WorkflowBaseInfo baseInfo = new WorkflowBaseInfo();
        baseInfo.setWorkflowId(workflowid);

        WorkflowRequestInfo requestInfo = new WorkflowRequestInfo();
        requestInfo.setRequestName(lcbt);

        requestInfo.setCreatorId(userid);
        requestInfo.setRequestLevel("0");//紧急程度

        requestInfo.setIsnextflow("0");//是否自动流转到创建人的下一个节点
        requestInfo.setWorkflowMainTableInfo(mainTableInfo);
        requestInfo.setWorkflowBaseInfo(baseInfo);
        requestInfo.setWorkflowDetailTableInfos(detailTableInfos);


        String requestid = service.doCreateWorkflowRequest(requestInfo, weaver.general.Util.getIntValue(userid));

        return requestid;
    }

    /**
     * 创建带有明细一和明细二的流程
     *
     * @param mapMain    字段信息
     * @param dt1data    明细一字段信息
     * @param dt2data    明细二字段信息
     * @param lcbt       流程标题
     * @param workflowid 流程id
     * @param userid     创建人
     * @return 流程requestid
     */
    private static String createWf(Map<String, String> mapMain, List<Map<String, String>> dt1data, List<Map<String, String>> dt2data, String lcbt, String workflowid, String userid) {
        WorkflowServiceImpl service = new WorkflowServiceImpl();
        WorkflowRequestTableField[] requestTableFields = new WorkflowRequestTableField[mapMain.size()];
        int i = 0;
        for (String k : mapMain.keySet()) {
            String v = mapMain.get(k);
            requestTableFields[i] = new WorkflowRequestTableField();
            requestTableFields[i].setFieldName(k);
            requestTableFields[i].setFieldValue(Util.null2String(v));
            requestTableFields[i].setView(true);
            requestTableFields[i].setEdit(true);
            i++;
        }
        WorkflowRequestTableRecord[] requestTableRecords = new WorkflowRequestTableRecord[1];
        requestTableRecords[0] = new WorkflowRequestTableRecord();
        requestTableRecords[0].setWorkflowRequestTableFields(requestTableFields);
        WorkflowMainTableInfo mainTableInfo = new WorkflowMainTableInfo();
        mainTableInfo.setRequestRecords(requestTableRecords);

        WorkflowDetailTableInfo[] detailTableInfos = new WorkflowDetailTableInfo[2];//2,明细表个数

        //明细一
        detailTableInfos[0] = new WorkflowDetailTableInfo();
        requestTableRecords = new WorkflowRequestTableRecord[dt1data.size()];
        int j = 0;
        for (Map<String, String> map : dt1data) {
            requestTableFields = new WorkflowRequestTableField[map.size()];
            i = 0;
            for (String k : map.keySet()) {
                String v = map.get(k);
                requestTableFields[i] = new WorkflowRequestTableField();
                requestTableFields[i].setFieldName(k);
                requestTableFields[i].setFieldValue(Util.null2String(v));
                requestTableFields[i].setView(true);
                requestTableFields[i].setEdit(true);
                i++;
            }
            requestTableRecords[j] = new WorkflowRequestTableRecord();
            requestTableRecords[j].setWorkflowRequestTableFields(requestTableFields);
            j++;
        }

        detailTableInfos[0].setWorkflowRequestTableRecords(requestTableRecords);

        //明细二
        detailTableInfos[1] = new WorkflowDetailTableInfo();//1，第二个明细表
        requestTableRecords = new WorkflowRequestTableRecord[dt2data.size()];
        int j2 = 0;
        for (Map<String, String> map : dt2data) {
            requestTableFields = new WorkflowRequestTableField[map.size()];
            i = 0;
            for (String k : map.keySet()) {
                String v = map.get(k);
                requestTableFields[i] = new WorkflowRequestTableField();
                requestTableFields[i].setFieldName(k);
                requestTableFields[i].setFieldValue(Util.null2String(v));
                requestTableFields[i].setView(true);
                requestTableFields[i].setEdit(true);
                i++;
            }
            requestTableRecords[j2] = new WorkflowRequestTableRecord();
            requestTableRecords[j2].setWorkflowRequestTableFields(requestTableFields);
            j2++;
        }

        detailTableInfos[1].setWorkflowRequestTableRecords(requestTableRecords);

        WorkflowBaseInfo baseInfo = new WorkflowBaseInfo();
        baseInfo.setWorkflowId(workflowid);

        WorkflowRequestInfo requestInfo = new WorkflowRequestInfo();
        requestInfo.setRequestName(lcbt);

        requestInfo.setCreatorId(userid);
        requestInfo.setRequestLevel("0");//紧急程度

        requestInfo.setIsnextflow("0");//是否自动流转到创建人的下一个节点
        requestInfo.setWorkflowMainTableInfo(mainTableInfo);
        requestInfo.setWorkflowBaseInfo(baseInfo);
        requestInfo.setWorkflowDetailTableInfos(detailTableInfos);

        String requestid = service.doCreateWorkflowRequest(requestInfo, weaver.general.Util.getIntValue(userid));

        return requestid;
    }

}