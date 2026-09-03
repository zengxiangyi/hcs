package com.baogang.info.service;

import com.baogang.info.entity.*;
import com.baogang.info.tool.DateTimeTool;
import com.baogang.info.tool.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FlowEngine {

    private final FlowNodeService flowNodeService;
    private final FlowEdgeService flowEdgeService;
    private final FlowGraphService flowGraphService;
    private final FlowHistoryService flowHistoryService;
    private final FlowCurrentService flowCurrentService;
    private final WorkflowService workflowService;

    public FlowEngine(FlowNodeService flowNodeService, FlowEdgeService flowEdgeService, FlowGraphService flowGraphService, FlowHistoryService flowHistoryService, FlowCurrentService flowCurrentService, WorkflowService workflowService) {
        this.flowNodeService = flowNodeService;
        this.flowEdgeService = flowEdgeService;
        this.flowGraphService = flowGraphService;
        this.flowHistoryService = flowHistoryService;
        this.flowCurrentService = flowCurrentService;
        this.workflowService = workflowService;
    }

    /**
     * 启动新流程
     * @param flowGraph 流程类型
     */
    @Transactional
    public String start(String flowGraph){
        // 生成新的流程实例
        Workflow workflow = new Workflow();
        workflow.setCode("WF"+System.currentTimeMillis());
        workflow.setName("蓝本工艺审批001");
        workflow.setFlowGraph(flowGraph);
        workflow.setState("start");
        workflow.setStartTime(DateTimeTool.currentTime());
        workflow.setSender(UserInfo.currentUsername());
        workflow.setRemark("启动新流程");
        workflowService.save(workflow);
        // 获取流程类型的开始节点
        FlowNode startNode = flowNodeService.getByFlowGraphAndCategory(workflow.getFlowGraph(),"S").get(0);
        if(startNode!=null){
            // 获取已开始为起点的边线
            List<FlowEdge> startEdges = flowEdgeService.findByFlowGraphAndFromNode(startNode.getFlowGraph(),startNode.getCode());
            if(startEdges.size()>0){
                // 迭代每一条边，记录目标节点为当前节点
                for(FlowEdge startEdge:startEdges){
                    FlowCurrent flowCurrent = new FlowCurrent();
                    flowCurrent.setWorkflow(workflow.getCode());
                    flowCurrent.setFlowGraph(startEdge.getFlowGraph());
                    flowCurrent.setFlowNode(startEdge.getToNode());
                    flowCurrent.setStartTime(DateTimeTool.currentTime());
                    flowCurrent.setRemark("开始流程");
                    flowCurrentService.save(flowCurrent);
                    // 记录历史记录
                    FlowHistory flowHistory = new FlowHistory();
                    flowHistory.setWorkflow(workflow.getCode());
                    flowHistory.setDealUser(UserInfo.currentUsername());
                    flowHistory.setDealTime(DateTimeTool.currentTime());
                    flowHistory.setRemark("开始流程");
                    flowHistory.setAction("start");
                    flowHistory.setFlowGraph(startEdge.getFlowGraph());
                    flowHistory.setEdge(startEdge.getCode());
                    flowHistory.setFromNode(startEdge.getFromNode());
                    flowHistory.setToNode(startEdge.getToNode());
                    flowHistoryService.save(flowHistory);
                }
            }
        }
        return "start";
    }

    /*
     * 处理中间节点
     * @param workflow 流程实例
     * @param flowGraph 流程图
     * @param edgeCode 边线编号
     */
    @Transactional
    public String dealNode(String workflow,String flowGraph,String edge){
        // 参数校验
        if(workflow==null||flowGraph==null||edge==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 查找边线
        FlowEdge flowEdge = flowEdgeService.findByFlowGraphAndCode(flowGraph,edge).get(0);
        if(flowEdge!=null){
            // 当前节点记录到历史记录
            FlowHistory flowHistory = new FlowHistory();
            flowHistory.setWorkflow(workflow);
            flowHistory.setDealUser(UserInfo.currentUsername());
            flowHistory.setDealTime(DateTimeTool.currentTime());
            flowHistory.setRemark("");
            flowHistory.setAction("");
            flowHistory.setFlowGraph(flowGraph);
            flowHistory.setEdge(edge);
            flowHistory.setFromNode(flowEdge.getFromNode());
            flowHistory.setToNode(flowEdge.getToNode());
            flowHistoryService.save(flowHistory);
            // 清除当前的节点
            flowCurrentService.deleteByWorkflow(workflow);
           // 目标节点为当前节点
            FlowNode todoNode = flowNodeService.getByFlowGraphAndCode(flowGraph,flowEdge.getToNode()).get(0);
            if(todoNode!=null&&todoNode.getCategory().equals("E")){
                // 结束节点，更新流程实例状态
                // workflowService.updateState(workflow,"end");
            }else {
                FlowCurrent flowCurrent = new FlowCurrent();
                flowCurrent.setWorkflow(workflow);
                flowCurrent.setFlowGraph(flowEdge.getFlowGraph());
                flowCurrent.setFlowNode(flowEdge.getToNode());
                flowCurrent.setStartTime(DateTimeTool.currentTime());
                flowCurrent.setRemark("当前节点");
                flowCurrentService.save(flowCurrent);
            }
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

}
