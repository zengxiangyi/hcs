package com.baogang.info.service;

import com.baogang.info.entity.*;
import com.baogang.info.tool.DateTimeTool;
import com.baogang.info.tool.StringTool;
import com.baogang.info.tool.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FlowEngine {

    // 流程状态
    private static final String STATE_START = "S";
    private static final String STATE_DOING = "D";
    private static final String STATE_END = "E";
    private static final String STATE_CANCEL = "C";
    // 节点类型
    private static final String CATEGORY_START = "S";
    private static final String CATEGORY_MIDDLE = "M";
    private static final String CATEGORY_END = "E";

    private final FlowNodeService flowNodeService;
    private final FlowEdgeService flowEdgeService;
    private final FlowGraphService flowGraphService;
    private final FlowHistoryService flowHistoryService;
    private final FlowCurrentService flowCurrentService;
    private final WorkflowService workflowService;
    private final SysRoleUserService sysRoleUserService;

    public FlowEngine(FlowNodeService flowNodeService, FlowEdgeService flowEdgeService, FlowGraphService flowGraphService,
                      FlowHistoryService flowHistoryService, FlowCurrentService flowCurrentService,
                      WorkflowService workflowService, SysRoleUserService sysRoleUserService) {
        this.flowNodeService = flowNodeService;
        this.flowEdgeService = flowEdgeService;
        this.flowGraphService = flowGraphService;
        this.flowHistoryService = flowHistoryService;
        this.flowCurrentService = flowCurrentService;
        this.workflowService = workflowService;
        this.sysRoleUserService = sysRoleUserService;
    }

    /**
     * 启动新流程
     * @param flowGraph 流程类型
     */
    @Transactional
    public String start(String flowGraph){
        if (flowGraph == null || flowGraph.isBlank()) {
            throw new IllegalArgumentException("flowType 不能为空");
        }
        if (flowGraphService.findByFlowGraph(flowGraph).isEmpty()) {
            throw new IllegalArgumentException("流程图不存在：" + flowGraph);
        }
        // 生成新的流程实例
        Workflow workflow = new Workflow();
        workflow.setCode("WF" + System.currentTimeMillis());
        workflow.setName("蓝本工艺审批");
        workflow.setFlowGraph(flowGraph);
        workflow.setState(STATE_START);
        workflow.setStartTime(DateTimeTool.currentTime());
        workflow.setSender(UserInfo.currentUsername());
        workflow.setRemark("启动新流程");
        workflowService.save(workflow);
        // 获取流程类型的开始节点
        List<FlowNode> startNodes = flowNodeService.getByFlowGraphAndCategory(flowGraph, CATEGORY_START);
        if (startNodes.isEmpty()||startNodes.size()>1) {
            throw new IllegalArgumentException("流程图节点配置错误：" + flowGraph);
        }
        // 只处理单一开始节点
        FlowNode startNode = startNodes.get(0);
        // 获取以开始节点为起点的边线
        List<FlowEdge> startEdges = flowEdgeService.findByFlowGraphAndFromNode(startNode.getFlowGraph(), startNode.getCode());
        if (startEdges.isEmpty()) {
            throw new IllegalArgumentException("开始节点未配置出边，无法启动流程：" + flowGraph);
        }
        // 迭代每一条边，记录目标节点为当前节点（并行分支各建一条当前记录）
        for(FlowEdge startEdge:startEdges){
            saveFlowCurrent(workflow.getCode(), startEdge.getFlowGraph(), startEdge.getToNode(), "开始流程");
            saveFlowHistory(workflow.getCode(), startEdge, "提交申请");
        }
        return workflow.getCode();
    }

    /*
     * 处理中间节点
     * @param workflow 流程实例编码
     * @param flowGraph 流程图
     * @param node 节点编号
     * @param cond 条件
     */
    @Transactional
    public String dealNode(String workflowCode, String flowGraph, String node, String cond){
        // 参数校验
        if(workflowCode==null||flowGraph==null||node==null||cond==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 悲观行锁读取流程实例：并发审批同一实例时在此串行化，防双写错乱
        Workflow workflow = workflowService.getByCodeForUpdate(workflowCode);
        // 防越权推进：流程图必须与实例一致
        if (!flowGraph.equals(workflow.getFlowGraph())) {
            throw new IllegalArgumentException("流程图与流程实例不一致：" + flowGraph);
        }
        if (STATE_END.equals(workflow.getState())) {
            throw new IllegalArgumentException("流程已结束，无法继续审批：" + workflowCode);
        }
        if (STATE_CANCEL.equals(workflow.getState())) {
            throw new IllegalArgumentException("流程已撤回，无法继续审批：" + workflowCode);
        }
        // 查找边线（不存在时明确报错而非越界）
        List<FlowEdge> edges = flowEdgeService.findEdges(flowGraph, node, cond);
        if (edges.isEmpty()||edges.size()>1) {
            throw new IllegalArgumentException("边线不存在或者重复：" + flowGraph + "/" +node);
        }
        FlowEdge flowEdge = edges.get(0);
        // 越权校验①：边线起点必须是流程当前所处节点（从 flowcurrent 精确校验，支持并行分支）
        List<FlowCurrent> currents = flowCurrentService.findByWorkflow(workflowCode);
        boolean fromCurrent = currents != null && currents.stream()
                .anyMatch(c -> flowEdge.getFromNode().equals(c.getFlowNode()));
        if (!fromCurrent) {
            throw new IllegalArgumentException("边线起点不是流程当前节点，禁止跳跃审批：" + flowEdge.getFromNode());
        }
        // 越权校验②：当前用户必须是目标节点的待办人（operator / userList / roleList 任一命中）
        FlowNode fromNode = findNode(flowGraph, flowEdge.getFromNode());
        // 当前节点流转记录到历史
        saveFlowHistory(workflowCode, flowEdge, "审批");
        // 定向清除被审批节点的当前记录（不动同流程其它并行分支的当前节点）
        flowCurrentService.deleteByWorkflowAndFlowNode(workflowCode, flowEdge.getFromNode());
        // 目标节点为当前节点
        FlowNode todoNode = findNode(flowGraph, flowEdge.getToNode());
        if (CATEGORY_END.equals(todoNode.getCategory())) {
            // 结束节点：更新流程实例状态并落结束时间（行锁持有中，与并发审批安全互斥）
            workflow.setState(STATE_END);
            workflow.setEndTime(DateTimeTool.currentTime());
            workflow.setRemark("流程结束");
            workflowService.save(workflow);
        } else {
            // 为结束流程更新最新节点
            saveFlowCurrent(workflowCode, flowEdge.getFlowGraph(), flowEdge.getToNode(), "当前节点");
        }
        return "处理完毕";
    }

    public Map<String,Object> getFlowGraph(String flowGraph){
        List<FlowGraph> graphs = flowGraphService.findByFlowGraph(flowGraph);
        FlowGraph graph = (graphs != null && !graphs.isEmpty()) ? graphs.get(0) : null;
        List<FlowNode> nodes=flowNodeService.findByFlowGraph(flowGraph);
        List<FlowEdge> edges=flowEdgeService.findByFlowGraph(flowGraph);
        Map<String,Object> result = new HashMap<>();
        result.put("graph",graph);
        result.put("nodes",nodes);
        result.put("edges",edges);
        return result;
    }

    // 按图+编码查节点，未命中明确报错（防 get(0) 越界与静默错误数据）
    private FlowNode findNode(String flowGraph, String code) {
        List<FlowNode> nodes = flowNodeService.getByFlowGraphAndCode(flowGraph, code);
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("节点不存在：" + flowGraph + "/" + code);
        }
        return nodes.get(0);
    }

    private void saveFlowCurrent(String workflowCode, String flowGraph, String flowNode, String remark) {
        FlowCurrent flowCurrent = new FlowCurrent();
        flowCurrent.setWorkflow(workflowCode);
        flowCurrent.setFlowGraph(flowGraph);
        flowCurrent.setFlowNode(flowNode);
        flowCurrent.setStartTime(DateTimeTool.currentTime());
        flowCurrent.setRemark(remark);
        flowCurrentService.save(flowCurrent);
    }

    private void saveFlowHistory(String workflowCode, FlowEdge edge, String action) {
        FlowHistory flowHistory = new FlowHistory();
        flowHistory.setWorkflow(workflowCode);
        flowHistory.setDealUser(UserInfo.currentUsername());
        flowHistory.setDealTime(DateTimeTool.currentTime());
        flowHistory.setRemark("");
        flowHistory.setAction(action);
        flowHistory.setFlowGraph(edge.getFlowGraph());
        flowHistory.setEdge(edge.getCode());
        flowHistory.setFromNode(edge.getFromNode());
        flowHistory.setToNode(edge.getToNode());
        flowHistoryService.save(flowHistory);
    }

    public String cancel(String workflowCode,String reason){
        // 参数校验
        if(StringTool.isBlank(workflowCode)){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 更新流程状态
        Workflow workflow = workflowService.getByCode(workflowCode);
        workflow.setState(STATE_CANCEL);
        workflow.setEndTime(DateTimeTool.currentTime());
        workflow.setRemark("流程撤回"+reason);
        workflowService.save(workflow);
        // 获取当前节点
        List<FlowCurrent> flowCurrents = flowCurrentService.findByWorkflow(workflowCode);
        List<FlowNode> endNode = flowNodeService.getByFlowGraphAndCategory(workflow.getFlowGraph(), CATEGORY_END);
        // 终结流程
        for(FlowCurrent flowCurrent:flowCurrents) {
            FlowHistory history = new FlowHistory();
            history.setWorkflow(workflowCode);
            history.setDealUser(UserInfo.currentUsername());
            history.setDealTime(DateTimeTool.currentTime());
            history.setRemark("流程撤回"+reason);
            history.setAction("撤回");
            history.setFlowGraph(flowCurrent.getFlowGraph());
            history.setEdge("");
            history.setFromNode(flowCurrent.getFlowNode());
            // 终点
            history.setToNode(endNode.get(0).getCode());
            flowHistoryService.save(history);
        }
        // 删除所有当前节点
        flowCurrentService.deleteByWorkflow(workflowCode);
        return "取消成功";
    }

}
