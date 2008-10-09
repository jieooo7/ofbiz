/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.*;
import java.lang.*;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.*;

projectId = parameters.projectId;
userLogin = parameters.userLogin;

//project info
result = dispatcher.runSync("getProject", [projectId : projectId, userLogin : userLogin]);
project = result.projectInfo;
if (project && project.startDate) 
    context.chartStart = project.startDate;
else
    context.chartStart = UtilDateTime.nowTimestamp(); // default todays date
if (project && project.completionDate)         
    context.chartEnd = project.completionDate;
else
    context.chartEnd = UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), 14); // default 14 days long

if (project == null) return;

ganttList = new LinkedList();
result = dispatcher.runSync("getProjectPhaseList", [userLogin : userLogin , projectId : projectId]);
phases = result.phaseList;
if (phases){
	phases.each { phase ->
		newPhase = phase;
        if (!newPhase.estimatedStartDate && newPhase.actualStartDate) {
            newPhase.estimatedStartDate = newPhase.actualStartDate;
        }
        if (!newPhase.estimatedStartDate) {
            newPhase.estimatedStartDate = context.chartStart;
        }
        if (!newPhase.estimatedCompletionDate && newPhase.actualCompletionDate) {
            newPhase.estimatedCompletionDate = newPhase.actualCompletionDateDate;
        }
        if (!newPhase.estimatedCompletionDate) {
            newPhase.estimatedCompletionDate = UtilDateTime.addDaysToTimestamp(newPhase.estimatedStartDate, 3);
        }
        newPhase.workEffortTypeId = "PHASE";
        Debug.log("===start: " + newPhase.estimatedStartDate + "===end: " + newPhase.estimatedCompletionDate);
		ganttList.add(newPhase);
		Debug.log("=======" + phase.phaseName + "======" + phase.workEffortTypeId + " phaseId" + phase.phaseId);
		tasks = delegator.findByAnd("WorkEffort", ["workEffortParentId" : phase.phaseId]);
		if (tasks){
			tasks.each { task ->
	        Debug.log("===name====" + task.workEffortName + "===type===" + task.workEffortTypeId + "===id===" + task.workEffortId);
	        	resultTaskInfo = dispatcher.runSync("getProjectTask", [userLogin : userLogin , taskId : task.workEffortId]);
                Debug.log("===restaskinfo " + resultTaskInfo);
	        	taskInfo = resultTaskInfo.taskInfo;
	        	Debug.log("===taskinfo " + taskInfo);
                if (!taskInfo.estimatedStartDate && taskInfo.actualStartDate) {
                    taskInfo.estimatedStartDate = taskInfo.actualStartDate;
                }
                if (!taskInfo.estimatedStartDate) {
                    taskInfo.estimatedStartDate = newPhase.estimatedStartDate;
                }
                if (!taskInfo.estimatedCompletionDate && taskInfo.actualCompletionDate) {
                    taskInfo.estimatedCompletionDate = taskInfo.actualCompletionDate;
                }
                if (!taskInfo.estimatedCompletionDate) {
                    taskInfo.estimatedCompletionDate = UtilDateTime.addDaysToTimestamp(newPhase.estimatedStartDate, 3);
                } 
                taskInfo.workEffortTypeId = "TASK";
				ganttList.add(taskInfo);
			}
		}
	}
}
context.phaseTaskList = ganttList;

