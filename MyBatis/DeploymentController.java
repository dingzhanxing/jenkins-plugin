package com.rocketsoftware.rdoe.controller;

import com.rocketsoftware.common.config.DBEnv;
import com.rocketsoftware.common.custom.DeleteFileInputStream;
import com.rocketsoftware.common.def.FileFormat;
import com.rocketsoftware.common.def.MessageCodeConstants;
import com.rocketsoftware.common.exception.RDOException;
import com.rocketsoftware.common.exception.RDORuntimeException;
import com.rocketsoftware.common.service.FileDownloadService;
import com.rocketsoftware.common.service.IMessageService;
import com.rocketsoftware.common.util.*;
import com.rocketsoftware.rdoe.common.RDOEConstants;
import com.rocketsoftware.rdoe.controller.vo.deployment.*;
import com.rocketsoftware.rdoe.dao.rdoe.model.DeploymentProfileModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.DeploymentSetModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.EnvironmentModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.OperationSystemModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.ProfileModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.TargetModel;
import com.rocketsoftware.rdoe.service.IDeploymentService;
import com.rocketsoftware.rdoe.service.IDeploymentSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Rocket
 * @date Dec, 9th 2020
 */
@RestController
@RequestMapping(path = "api/rdoe")
@Api(tags = {"deployment"})
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IDeploymentService deploymentService;

    @Autowired
    private IDeploymentSetService deploymentSetService;

    @Autowired
    private FileDownloadService fileDownloadService;

    public static final String TARGETID_EMPTY = "targetId is empty.";

    /**
     * @api {GET} /api/rdoe/deployments Get deployment set list by date
     * @apiVersion 10.0.1
     * @apiName get_deployments_date
     * @apiGroup 03 Deployment
     * @apiSampleRequest /deployments
     *
     * @apiDescription Get Deployment set list based on start date and end date.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} [startDate]       Start date.
     * @apiQuery {String} [endDate]         End date.
     * @apiQuery {String} [keyword]         The keyword to search for.
     * @apiQuery {String} [searchScope]     It is used to limit search scope by the keyword parameter. Valid values are Destination(1), Profile(2), Deployment set(3). If this parameter is not given, all three fields will be searched for the keyword.
     * @apiQuery {String} [status]          Status for deployment set. There are 8 kinds of status, and each status should be case sensitive.<br/>
     *                     1 Skipped: Skipped.<br/>
     *                     2 Timed out: Timed out.<br/>
     *                     3 Restored: Recovered or Restore.<br/>
     *                     4 Failed: Failed,Canceled,Cancelled or Failure.<br/>
     *                     5 Attention: Deploy, Prepare.<br/>
     *                     6 In Process: Started.<br/>
     *                     7 Backed out: Backout.<br/>
     *                     8 Completed: Success.<br/>
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object[]}   payload							                    Returned payload data.
     * @apiSuccess {Number}     payload.deploymentProfileId             Profile ID.
     * @apiSuccess {String}     payload.deploymentProfileName           Profile name.
     * @apiSuccess {Number}     payload.deploymentSetId                 Deployment set ID.
     * @apiSuccess {String}     payload.deploymentSetName               Deployment set name.
     * @apiSuccess {String}     payload.deploymentStep                  Deployment step.
     * @apiSuccess {String}     payload.destinationComputerId           Computer ID.
     * @apiSuccess {String}     payload.destinationComputerName         Computer name.
     * @apiSuccess {String}     payload.destinationName                 Destination name.
     * @apiSuccess {String}     payload.destinationTargetId             Destination target ID.
     * @apiSuccess {String}     payload.destinationTargetName           Destination target name.
     * @apiSuccess {String}     payload.lastChangedDate                 Last changed date.
     * @apiSuccess {String}     payload.serviceType                     Service type.
     * @apiSuccess {String}     payload.status                          Status.
     * @apiSuccess {Number}     payload.lstreqstp                       Last request step.
     * @apiSuccess {Number}     payload.lstreqstat                      Last request status.
     * @apiSuccess {Number}     payload.dststatcde                      Destination status code.
     * @apiSuccess {String}     payload.dstntfytim                      Destination notification time.
     * @apiSuccess {String}     payload.dststatval                      Destination status value.
     * @apiSuccess {Boolean}    payload.verify                          Verify.
     *
     * @apiSuccessExample Success-Response:
     * {
     * "status": 200,
     * "returncode": "0",
     * "message": "OK",
     * "pageIndex": 1,
     * "pageSize": 10,
     * "recordsTotal": 1,
     * "payload": [
     *  {
     *      "deploymentProfileId": 10001,
     *      "deploymentProfileName": "zcy01",
     *      "deploymentSetId": 10002,
     *      "deploymentSetName": "D00010002",
     *      "deploymentStep": "Install",
     *      "destinationComputerId": "10001",
     *      "destinationComputerName": "waldevaldnzcy01",
     *      "destinationName": "waldevaldnzcy01 : zcyo1-tgt",
     *      "destinationTargetId": "10001",
     *      "destinationTargetName": "zcyo1-tgt",
     *      "lastChangedDate": "2023-06-28T05:13:07.128+00:00",
     *      "serviceType": "lme",
     *      "status": "Success",
     *      "lstreqstp": 3,
     *      "lstreqstat": 1,
     *      "dststatcde": 1,
     *      "dstntfytim": "3000-01-01T00:00:00.000+00:00",
     *      "dststatval": "Success",
     *      "verify": true
     *  },
     * ]
     * }
     */
    /**
     * Used to get deployments based on start date and end date
     *
     * @param startDate start date as string format
     * @param endDate   end date as string format
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment Set List based on start date and end date.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "Start Date.", required = false,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "endDate", value = "End Date.", required = false,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "keyword", value = "Search keyword.", required = false,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "searchScope", value = "It is used to limit search scope by the keyword parameter. " +
                    "Valid values are Destination(1), Profile(2), Deployment set(3). If this parameter is not given, the scope of search keyword will be for all the three fields.", required = false,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "status", value = "Status for deployment set." +
                    "There are 8 kinds of status, and each status should be case sensitive.<br/>" +
                    "No Name: value.<br/>" +
                    "1 Skipped: Skipped.<br/>" +
                    "2 Timed out: Timed out.<br/>" +
                    "3 Restored: Recovered or Restore.<br/>" +
                    "4 Failed: Failed,Canceled,Cancelled or Failure.<br/>" +
                    "5 Attention: Deploy,Prepare.<br/>" +
                    "6 In Process: Started.<br/>" +
                    "7 Backed out: Backout.<br/>" +
                    "8 Completed: Success.<br/>", required = false,
                    dataType = "java.lang.String")})
    @GetMapping("deployments")
    public ResultUtil<Object> deploymentList(String startDate, String endDate, String keyword, String status, String searchScope) throws RDOException {
        if (StringUtils.isEmpty(startDate)) {
            startDate = RDOEConstants.START_DATE_DEFAULT_STRING;
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = RDOEConstants.END_DATE_DEFAULT_STRING;
        }
        List<DeploymentListVo> list = deploymentService.getDeploymentList(startDate, endDate, keyword, status, searchScope);
        return ResultUtil.success(list);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} /api/rdoe/deployments/{deploymentSetId} Get deployment set detailed info
     * @apiVersion 10.0.1
     * @apiName get_deployments_set_byId
     * @apiGroup 03 Deployment
     * @apiSampleRequest /deployments/:deploymentSetId
     *
     * @apiDescription Get deployment det detailed info by deployment set ID.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} deploymentSetId     Deployment set ID.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object[]}   payload							                     Returned payload data.
     * @apiSuccess {Number}     payload.deploymentStatusCode             Profile ID.
     * @apiSuccess {Number}     payload.lastRequestStat                  Last request status.
     * @apiSuccess {Number}     payload.lastRequestStep                  Last request step.
     * @apiSuccess {String}     payload.deploymentNotificationTime       Deployment notification time.
     * @apiSuccess {Number}     payload.completedStep                    Completed step.
     * @apiSuccess {Number}     payload.completedStatus                  Completed status.
     * @apiSuccess {String}     payload.stets                            Stets.
     * @apiSuccess {String}     payload.deploymentSetName                Deployment set name
     * @apiSuccess {String}     payload.deploymentSetDescription         Deployment set description.
     * @apiSuccess {String}     payload.profileName                      Profile name.
     * @apiSuccess {Number}     payload.profileId                        Profile ID.
     * @apiSuccess {String}     payload.environment                      Environment.
     * @apiSuccess {Object[]}   payload.taskList                         Task list.
     * @apiSuccess {Number}     payload.taskList.taskId                  Task ID.
     * @apiSuccess {String}     payload.taskList.taskName                Task name.
     * @apiSuccess {String}     payload.taskList.description             Task description.
     * @apiSuccess {String}     payload.taskList.taskDate                Task date.
     * @apiSuccess {String}     payload.taskList.taskClosed              Task closed.
     * @apiSuccess {Boolean}    payload.taskList.closed                  Closed.
     * @apiSuccess {Object[]}   payload.taskList.issue                   Issue.
     * @apiSuccess {String}     payload.taskList.issue.issueId           Issue ID.
     * @apiSuccess {String}     payload.taskList.issue.issueNumber       Issue number.
     * @apiSuccess {Object[]}   payload.deploymentSetCounts              Deployment Set counts.
     * @apiSuccess {Number}     payload.deploymentSetCounts.count        Count.
     * @apiSuccess {String}     payload.deploymentSetCounts.keyName      Key name.
     * @apiSuccess {Object[]}   payload.targets                          Targets.
     * @apiSuccess {Number}     payload.targets.computerId               Target computer ID.
     * @apiSuccess {String}     payload.targets.computerName             Target computer name.
     * @apiSuccess {String}     payload.targets.targetName               Target name.
     * @apiSuccess {Number}     payload.targets.targetId                 Target ID.
     * @apiSuccess {Number}     payload.targets.numFiles                 Number files.
     * @apiSuccess {Boolean}    payload.targets.redeployOrRecovery       Redeploy or recovery.
     * @apiSuccess {String}     payload.targets.status                   Status.
     * @apiSuccess {String}     payload.targets.step                     Step.
     * @apiSuccess {String}     payload.targets.fileCount                File count.
     * @apiSuccess {String}     payload.targets.statCode                 Stats code.
     * @apiSuccess {String}     payload.targets.lastRequestStat          Last request status.
     * @apiSuccess {String}     payload.targets.destinationStep          Destination step.
     * @apiSuccess {String}     payload.targets.lastRequestStep          Last request step.
     * @apiSuccess {String}     payload.targets.lastCompletedStep        Last completed step.
     * @apiSuccess {String}     payload.targets.deploymentNotificationTime     Deployment Notification time.
     * @apiSuccess {Boolean}    payload.targets.verify                         Verify.
     * @apiSuccess {String}     payload.date                        Date.
     * @apiSuccess {Boolean}    payload.redeployOrRecovery          Redeploy or recovery.
     * @apiSuccess {String}   payload.status                        Status.
     * @apiSuccess {Object}   payload.stepStatus                    Step status.
     * @apiSuccess {String}   payload.stepStatus.add                Step status add.
     * @apiSuccess {String}   payload.stepStatus.prepare            Step status prepare.
     * @apiSuccess {String}   payload.stepStatus.deploy             Step status deploy.
     * @apiSuccess {String}   payload.stepStatus.install            Step status install.
     * @apiSuccess {String}   payload.stepStatus.done               Step status done.
     * @apiSuccess {Object}   payload.numDeploys                    Number of deploys.
     * @apiSuccess {Number}   payload.numDeploys.success            Number of deploys success.
     * @apiSuccess {Number}   payload.numDeploys.failed             Number of deploys failed.
     * @apiSuccess {Number}   payload.numDeploys.skipped            Number of deploys skipped.
     * @apiSuccess {Object}   payload.numInstalls                   Number of installs.
     * @apiSuccess {Number}   payload.numInstalls.success           Number of installs success.
     * @apiSuccess {Number}   payload.numInstalls.failed            Number of installs failed.
     * @apiSuccess {Number}   payload.numInstalls.skipped           Number of installs skipped.
     * @apiSuccess {Number}   payload.numParts                      Number of parts.
     * @apiSuccess {Number}   payload.numDestinations               Number of destinations.
     * @apiSuccess {Boolean}   payload.report                       Report.
     *
     * @apiSuccessExample Success-Response:
     * {
     *      "status": 200,
     *      "returncode": "0",
     *      "message": "OK",
     *      "recordsTotal": 0,
     *      "payload": {
     *          "deploymentStatusCode": 1,
     *          "lastRequestStat": 1,
     *          "lastRequestStep": 3,
     *          "deploymentNotificationTime": "3000-01-01T00:00:00.000+00:00",
     *          "completedStep": 3,
     *          "completedStatus": 1,
     *          "stets": "2023-06-28T05:13:07.200+00:00",
     *          "deploymentSetName": "D00010002",
     *          "deploymentSetDescription": "Ad Hoc czhu 2023-06-28 01:12:24",
     *          "profileName": "zcy01",
     *          "profileId": 10001,
     *          "environment": "INV",
     *          "taskList": [
     *              {
     *                  "taskId": 10002,
     *                  "taskName": "task01",
     *                  "description": "",
     *                  "taskDate": "2023-06-28T05:11:28.971+00:00",
     *                  "taskClosed": "0",
     *                  "closed": false,
     *                  "issue": [
     *                      {
     *                          "issueId": "0",
     *                          "issueNumber": "0"
     *                      }
     *                  ]
     *              }
     *          ],
     *          "deploymentSetCounts": [
     *              {
     *                  "count": 1,
     *                  "keyName": "numDestinations"
     *              },
     *              {
     *                  "count": 0,
     *                  "keyName": "recoveryCnt"
     *              },
     *              {
     *                  "count": 1,
     *                  "keyName": "deployedSuccess"
     *              },
     *              {
     *                  "count": 0,
     *                  "keyName": "deployedFailed"
     *              },
     *              {
     *                  "count": 0,
     *                  "keyName": "deployedSkipped"
     *              },
     *              {
     *                  "count": 1,
     *                  "keyName": "installedSuccess"
     *              },
     *              {
     *                  "count": 0,
     *                  "keyName": "installedFailed"
     *              },
     *              {
     *                  "count": 0,
     *                  "keyName": "installedSkipped"
     *              },
     *              {
     *                  "count": 1,
     *                  "keyName": "numParts"
     *              }
     *          ],
     *          "targets": [
     *              {
     *                  "computerId": 10001,
     *                  "computerName": "waldevaldnzcy01",
     *                  "targetName": "zcyo1-tgt",
     *                  "targetId": 10001,
     *                  "numFiles": 1,
     *                  "redeployOrRecovery": false,
     *                  "status": "Success:8",
     *                  "step": "Install:2",
     *                  "fileCount": "1       ",
     *                  "statCode": "1 ",
     *                  "lastRequestStat": "1",
     *                  "destinationStep": "3 ",
     *                  "lastRequestStep": "3",
     *                  "lastCompletedStep": "3",
     *                  "deploymentNotificationTime": "3000-01-01T00:00:00.000+00:00",
     *                  "verify": true
     *              }
     *          ],
     *          "date": "2023-06-28 05:13:07",
     *          "redeployOrRecovery": false,
     *          "status": "InstallDone:26",
     *          "stepStatus": {
     *              "add": "Success:6",
     *              "prepare": "Success:6",
     *              "deploy": "Success:6",
     *              "install": "Success:6",
     *              "done": "Success:6"
     *          },
     *          "numDeploys": {
     *              "success": 1,
     *              "failed": 0,
     *              "skipped": 0
     *           },
     *          "numInstalls": {
     *              "success": 1,
     *              "failed": 0,
     *              "skipped": 0
     *          },
     *          "numParts": 1,
     *          "numDestinations": 1,
     *          "report": true
     *      }
     * }
     */
    /**
     * Get Deployment set detailed information based on id.
     *
     * @param deploymentSetId
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment Set Detailed Info based on a Deployment Set Id.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentSetId", value = "Deployment Set Id.", required = true,
                    dataType = "java.lang.String")})
    @GetMapping("deployments/{deploymentSetId}")
    public ResultUtil<Object> deploymentSetInfo(@PathVariable("deploymentSetId") String deploymentSetId) throws RDOException {
        logger.debug("deploymentSetInfo START");
        if (StringUtils.isEmpty(deploymentSetId)) {
            logger.warn("Deployment Set Id is empty.");
            return ResultUtil.error("Missing parameter deploymentSetId.");
        }

        // do serivce
        DeploymentSetInfoVo deploymentSetInfoVo = this.deploymentSetService.getDeploymentSetInfo(deploymentSetId);
        return ResultUtil.success(deploymentSetInfoVo);
    }

    /**
     * @api {GET} /api/rdoe/profiles/ext/{profileIdPlainText} Get deployment profile
     * @apiVersion 10.2.2
     * @apiName get_profile_ext
     * @apiGroup 03 Deployment
     *
     * @apiDescription Get deployment profile by profile ID.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} profileIdPlainText       Deployment profile ID. For example: 10002.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object}     payload							                    Returned payload data.
     * @apiSuccess {String}     payload.name                             Profile name.
     * @apiSuccess {String}     payload.description                      Profile description.
     * @apiSuccess {String}     payload.autoStart                        Auto start step.
     * @apiSuccess {Boolean}    payload.printPrepareReport               Print prepare report.
     * @apiSuccess {String}     payload.notificationRecipients           Notification recipients.
     * @apiSuccess {String}     payload.ntfnEmail                        Notification email.
     * @apiSuccess {String}     payload.ntfnDeploy                       Notification deploy.
     * @apiSuccess {String}     payload.ntfnInstall                      Notification install.
     * @apiSuccess {Number}     payload.snoozeTime                       Snooze time.
     * @apiSuccess {Number}     payload.timeout                          Timeout.
     * @apiSuccess {Number}     payload.countDeployments                 Deployments count.
     * @apiSuccess {String}     payload.deployDateAlgorithm              Deploy date algorithm.
     * @apiSuccess {String}     payload.deployTimeAlgorithm              Deploy time algorithm.
     * @apiSuccess {String}     payload.installDateAlgorithm             Install date algorithm.
     * @apiSuccess {String}     payload.installTimeAlgorithm             Install time algorithm.
     *
     * @apiSuccessExample Success-Response:
     * {
     *     "status": 200,
     *     "returncode": "0",
     *     "message": "OK",
     *     "recordsTotal": 0,
     *     "payload": {
     *          "name": "JenkinsDemo01",
     *          "description": "Will remove",
     *          "autoStart": "Install",
     *          "printPrepareReport": false,
     *          "notificationRecipients": "none",
     *          "ntfnEmail": "none",
     *          "ntfnDeploy": "none",
     *          "ntfnInstall": "none",
     *          "snoozeTime": 60,
     *          "timeout": 60,
     *          "countDeployments": 5,
     *          "deployDateAlgorithm": "immediately",
     *          "deployTimeAlgorithm": "immediately",
     *          "installDateAlgorithm": "immediately",
     *          "installTimeAlgorithm": "immediately"
     *      }
     * }
     * @apiSampleRequest /profiles/ext/:profileIdPlainText
     */
    /**
     * Get deployment profile info using aldcs
     * Command Example:
     * ald -p 55555 -c 69ac8784-2c53-419c-90b0-06b7309d2fd7 deploy profileinfo -i 10003
     *
     * This API main logic is same as getDeploymentProfileInfo, the difference here is no profile ID decode logic.
     * Without decode logic, apidoc could easy call this API with the plaintext profile ID.
     *
     * @param profileIdPlainText
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment Set Profile Info based on a Profile Id.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "profileIdPlainText", value = "Profile Id.", required = true,
            dataType = "java.lang.String")})
    @GetMapping("profiles/ext/{profileIdPlainText}")
    public ResultUtil<Object> getDeploymentProfileInfoExt(@PathVariable("profileIdPlainText") String profileIdPlainText) throws RDOException {
        logger.debug("deploymentProfiles START");
        if (StringUtils.isEmpty(profileIdPlainText)) {
            throw new RDOException("profileIdPlainText is empty.");
        }

        // do service
        ProfileModel model = this.deploymentService.getProfileInfo(profileIdPlainText);
        return ResultUtil.success(model);
    }

    /**
     * Get deployment profile info using aldcs
     * Command Example:
     * ald -p 55555 -c 69ac8784-2c53-419c-90b0-06b7309d2fd7 deploy profileinfo -i 10003
     * @param profileId
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment Set Profile Info based on a Profile Id.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "profileId", value = "Profile Id.", required = true,
                    dataType = "java.lang.String")})
    @GetMapping("profiles/{profileId}")
    public ResultUtil<Object> getDeploymentProfileInfo(@PathVariable("profileId") String profileId) throws RDOException {
        logger.debug("deploymentProfiles START");
        if (StringUtils.isEmpty(profileId)) {
            throw new RDOException("profileId is empty.");
        }

        // do service
        ProfileModel model = this.deploymentService.getProfileInfo(Base64Util.decodeURL(profileId));
        return ResultUtil.success(model);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} /api/rdoe/target/ext/{targetName} Get deployment target info
     * @apiVersion 10.2.2
     * @apiName get_target_ext
     * @apiGroup 03 Deployment
     * @apiSampleRequest /target/ext/:targetName
     *
     * @apiDescription Get deployment set target info.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} targetName       Target name.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object}     payload							                    Returned payload data.
     * @apiSuccess {String}     payload.name                             Target name.
     * @apiSuccess {String}     payload.description                      Target description.
     * @apiSuccess {String}     payload.osName                           OS name.
     * @apiSuccess {Boolean}    payload.osDescription                    OS description.
     * @apiSuccess {String}     payload.dmStatus                         Deployment status.
     * @apiSuccess {String}     payload.dmExternal                       Deployment external.
     * @apiSuccess {String}     payload.host                             Host.
     * @apiSuccess {String}     payload.port                             Port.
     * @apiSuccess {Number}     payload.distServer                       Deployment server.
     * @apiSuccess {Object}     payload.dc                               Deployment client.
     * @apiSuccess {Boolean}    payload.dc.external                      Deployment client external.
     * @apiSuccess {String}     payload.dc.host                          Deployment client host.
     * @apiSuccess {Number}     payload.dc.port                          Deployment client port.
     * @apiSuccess {Boolean}    payload.dc.active                        Deployment client active.
     * @apiSuccess {Object}     payload.ds                               Deployment server.
     * @apiSuccess {String}     payload.ds.host                          Deployment server host.
     * @apiSuccess {Number}     payload.ds.port                          Deployment server port.
     *
     * @apiSuccessExample Success-Response:
     * {
     *     "status": 200,
     *     "returncode": "0",
     *     "message": "OK",
     *     "recordsTotal": 0,
     *     "payload": {
     *          "name": "waldevaldnzcy01",
     *          "description": "Created by Aldon LM",
     *          "osName": "Win 10",
     *          "osDescription": "Microsoft(R) Windows(R) 10",
     *          "dmStatus": 1,
     *          "dmExternal": "0",
     *          "host": "10.117.16.109",
     *          "port": 2001,
     *          "distServer": "WALDEVALDNBPI05:7891",
     *          "dc": {
     *              "external": false,
     *              "host": "10.117.16.109",
     *              "port": 2001,
     *              "active": true
     *          },
     *          "ds": {
     *              "host": "WALDEVALDNBPI05",
     *              "port": 7891
     *          }
     *      }
     * }
     */
    /**
     * Get Deployment Target Info based on a given target name.
     *
     * This API main logic is same as getTargetInfo, the difference here is no target name decode logic.
     * Without decode logic, apidoc could easy call this API with the plaintext target name.
     *
     * @param targetName
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment target info based on a target name.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "targetName", value = "Target name.", required = true,
            dataType = "java.lang.String")})
    @GetMapping("target/ext/{targetName}")
    public ResultUtil<Object> getTargetInfoExt(@NotEmpty(message="Target.targetId.notEmpty") @PathVariable("targetName") String targetName) throws RDOException {
        logger.debug("getTargetInfoExt START");

        TargetModel model = this.deploymentService.getTargetInfo(targetName);

        return ResultUtil.success(model);
    }

    /**
     * Get Deployment Target Info based on a given target id.
     * @param targetId
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment Set Target Info based on a Target Id.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetId", value = "Target Id.", required = true,
                    dataType = "java.lang.String")})
    @GetMapping("target/{targetId}")
    public ResultUtil<Object> getTargetInfo(@NotEmpty(message="Target.targetId.notEmpty") @PathVariable("targetId") String targetId) throws RDOException {
        logger.debug("getTargetInfo START");

        TargetModel model = this.deploymentService.getTargetInfo(Base64Util.decodeURL(targetId));

        return ResultUtil.success(model);
    }

    /**
     * Get Deployment set list based on a Target by ALDCS.
     * And this may be changed due to performance issues as per discussed.
     * and it will be changed by using SQL.
     * Currently the implementation for ALDCS can be executed properly and can be paged properly.
     *  Command Example:
     *  ald -p 55555 -c 5ee2f074-619f-49b5-bd92-ff011579051f deploy destinfo -d dlndevaldnyzl01
     * @param targetId
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get Deployment set list based on a Target.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetId", value = "Target Id.", required = true,
                    dataType = "java.lang.String")})
    @GetMapping("target/{targetId}/deployments_v2")
    @Deprecated
    public ResultUtil<Object> getDeploymentSetsByTarget(@PathVariable("targetId") String targetId) throws RDOException {
        logger.debug("getDeploymentSetsByTarget START");
        if (StringUtils.isEmpty(targetId)) {
            throw new RDOException(TARGETID_EMPTY);
        }

        List<DeploymentListVo> dList = this.deploymentService.getDeploymentListByTarget(Base64Util.decodeURL(targetId));

        return ResultUtil.success(dList);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} target/ext/{targetName}/deployments Get deployment set list by target
     * @apiVersion 10.2.2
     * @apiName get_target_deployments
     * @apiGroup 03 Deployment
     * @apiSampleRequest /target/ext/{targetName}/deployments
     *
     * @apiDescription Get deployment set list by target.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} targetName       Target name.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object[]}     payload							                    Returned payload data.
     * @apiSuccess {String}     payload.name                              Deployment set.
     * @apiSuccess {String}     payload.date                              Date.
     * @apiSuccess {String}     payload.ntfyTime                          Notify time.
     * @apiSuccess {Boolean}    payload.destinationStep                   Destination step.
     * @apiSuccess {String}     payload.destinationStatus                 Destination status.
     * @apiSuccess {String}     payload.destinationStepCode               Destination step code.
     * @apiSuccess {String}     payload.destinationStatusCode             Destination status code.
     * @apiSuccess {Number}     payload.recoverCount                      Recover count.
     *
     * @apiSuccessExample Success-Response:
     * {
     *     "status": 200,
     *     "returncode": "0",
     *     "message": "OK",
     *     "pageIndex": 1,
     *     "pageSize": 10,
     *     "recordsTotal": 2,
     *     "payload": [
     *      {
     *          "date": "2023-06-28T05:13:07.140+00:00",
     *          "name": "D00010002",
     *          "ntfyTime": "3000-01-01T00:00:00.000+00:00",
     *          "destinationStep": "Install",
     *          "destinationStatus": "Success",
     *          "destinationStepCode": "3 ",
     *          "destinationStatusCode": "1 ",
     *          "recoverCount": 0
     *      },
     *      {
     *          "date": "2023-06-28T02:50:05.159+00:00",
     *          "name": "D00010001",
     *          "ntfyTime": "3000-01-01T00:00:00.000+00:00",
     *          "destinationStep": "Install",
     *          "destinationStatus": "Success",
     *          "destinationStepCode": "3 ",
     *          "destinationStatusCode": "1 ",
     *          "recoverCount": 0
     *      }
     *     ]
     * }
     */
    /**
     * getDeploymentSetsByTargetExt implement by SQL
     *
     * This API main logic is same as getDeploymentSetsByTarget, the difference here is no target name decode logic.
     * Without decode logic, apidoc could easy call this API with the plaintext target name.
     */
    @ApiOperation(value = "Get deployment set list by target.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "targetName", value = "Target name.", required = true,
            dataType = "java.lang.String")})
    @GetMapping("target/ext/{targetName}/deployments")
    public ResultUtil<Object> getDeploymentSetsByTargetSQLExt(@PathVariable("targetName") String targetName) throws RDOException {
        logger.debug("getDeploymentSetsByTargetSQLExt START");
        if (StringUtils.isEmpty(targetName)) {
            throw new RDOException(TARGETID_EMPTY);
        }

        List<DeploymentSetModel> dList = this.deploymentService.getDeploymentListByTargetSQL(targetName);

        return ResultUtil.success(dList);
    }

    /**
     * getDeploymentSetsByTarget implement by SQL
     */
    @ApiOperation(value = "Get Deployment set list based on a Target.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetId", value = "Target Id.", required = true,
                    dataType = "java.lang.String")})
        @GetMapping("target/{targetId}/deployments")
    public ResultUtil<Object> getDeploymentSetsByTargetSQL(@PathVariable("targetId") String targetId) throws RDOException {
        logger.debug("getDeploymentSetsByTargetSQL START");
        if (StringUtils.isEmpty(targetId)) {
            throw new RDOException(TARGETID_EMPTY);
        }

        List<DeploymentSetModel> dList = this.deploymentService.getDeploymentListByTargetSQL(Base64Util.decodeURL(targetId));

        return ResultUtil.success(dList);
    }

    /**
     * Modify a target by ALDCS.
     * Command Example:
     *  // if external is true
     *  ald -p 55555 -c c7493dbb-a6c8-42d9-a522-6906381a6b89 deploy destinfo -x yes -s localhost:4321 -y dlndevaldnyzl01.dev.rocketsoftware.com -p 1234 dlndevaldnyzl01
     *  // if external is false
     *  ald -p 55555 -c c7493dbb-a6c8-42d9-a522-6906381a6b89 deploy destinfo -x no dlndevaldnyzl01
     * @param targetVo
     * @param targetId
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Modify a target.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetVo", value = "Target info object.", required = true,
                    dataType = "com.rocketsoftware.rdoe.controller.vo.deployment.DeploymentSetTargetVo")})
    @PostMapping(path="target/{targetId}/modify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultUtil<String> modifyDeploymentSetTarget(@RequestBody DeploymentSetTargetVo targetVo, @PathVariable("targetId") String targetId) throws RDOException {
        logger.debug("modifyDeploymentSetTarget START");
        if (StringUtils.isEmpty(targetId)) {
            throw new RDOException(TARGETID_EMPTY);
        }

        targetVo.setTargetId(Base64Util.decodeURL(targetId));
        // Service invocation
        String result = this.deploymentService.modifyDeploymentSetTarget(targetVo);

        return ResultUtil.success("OK");
    }

    /**
     * Create a computer by ALDCS
     * Command Example:
     * ald.exe -p 55555 -c e79e6bd2-196e-45ee-b0fb-c6abff179e0b newcomputer -D -d "this is  my computer" -o "Win 7" "computer 001"
     * @param computer
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Create a computer.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "computer", value = "Target info object.", required = true,
                    dataType = "com.rocketsoftware.rdoe.controller.vo.deployment.Computer")})
    @PostMapping(path="computer/new", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object createDeploymentSetTarget(@RequestBody @Valid Computer computer) throws RDOException {
        logger.debug("createDeploymentSetTarget START");
        // Service invocation
        String result = this.deploymentService.createDeploymentSetTarget(computer);
        return ResultUtil.success(result);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} /api/rdoe/profiles/{profileId}/deployments Get deployment set list by profile
     * @apiVersion 10.0.1
     * @apiName get_deployment_set
     * @apiGroup 03 Deployment
     * @apiSampleRequest /profiles/:profileId/deployments
     *
     * @apiDescription Get Deployment set list by profile.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} profileId       Deployment profile ID. For example: 10002.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object[]}     payload							                    Returned payload data.
     * @apiSuccess {Number}     payload.deploymentSetId                   Deployment set ID.
     * @apiSuccess {String}     payload.deploymentSetName                 Deployment set name.
     * @apiSuccess {String}     payload.status                            Status.
     * @apiSuccess {Number}     payload.lstreqstp                         Last request step
     * @apiSuccess {Number}     payload.lstreqstat                        Last request status.
     * @apiSuccess {Number}     payload.dststatcde                        Notification email.
     * @apiSuccess {String}     payload.dstntfytim                        Notification time.
     * @apiSuccess {String}     payload.deploymentSetStep                 Deployment set step.
     * @apiSuccess {String}     payload.deploymentSetStatus               Deployment set status.
     * @apiSuccess {String}     payload.stateChangeTimeStamp              State change time.
     *
     * @apiSuccessExample Success-Response:
     * {
     *     "status": 200,
     *     "returncode": "0",
     *     "message": "OK",
     *     "recordsTotal": 2,
     *     "pageIndex": 1,
     * 	   "pageSize": 5000,
     * 	   "payload": [
     * 	        {
     * 	            "deploymentSetId": 12372,
     * 	            "deploymentSetName": "D00012372",
     * 	            "status": "Timed Out",
     * 	            "lstreqstp": 2,
     * 	            "lstreqstat": 0,
     * 	            "dststatcde": 1,
     * 	            "dstntfytim": "2999-01-01T00:00:00.000+00:00",
     * 	            "deploymentSetStep": "Prepare",
     * 	            "deploymentSetStatus": "Success",
     * 	            "stateChangeTimeStamp": "2023-02-16T09:43:15.147+00:00"
     * 	        },
     * 	        {
     * 	        "deploymentSetId": 12371,
     * 	        "deploymentSetName": "D00012371",
     * 	        "status": "Success",
     * 	        "lstreqstp": 1,
     * 	        "lstreqstat": 1,
     * 	        "dststatcde": 1,
     * 	        "dstntfytim": "3000-01-01T00:00:00.000+00:00",
     * 	        "deploymentSetStep": "Prepare",
     * 	        "deploymentSetStatus": "Success",
     * 	        "stateChangeTimeStamp": "2023-02-09T08:12:39.875+00:00"
     * 	        }
     * 	   ]
     * }
     */
    @ApiOperation(value = "Get Deployment Set List based on a Profile.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "profileId", value = "Profile Id", required = true,
                    dataType = "java.lang.String")})
    @GetMapping("profiles/{profileId}/deployments")
    public ResultUtil<Object> getDeploymentProfileSets(@PathVariable("profileId") String profileId) throws RDOException {
        logger.debug("getDeploymentProfileSets START");
        if (StringUtils.isEmpty(profileId)) {
            throw new RDOException("profileId is empty.");
        }

        Integer _profileId = 0;

        if(NumberUtils.isParsable(profileId)) {
            _profileId = Integer.parseInt(profileId);
        } else {
            throw new RDOException("Failed to convert profile id to Integer.");
        }

        List<DeploymentListVo> list = this.deploymentService.getDeploymentListByProfileId(_profileId);
        logger.debug("getDeploymentProfileSets END");
        return ResultUtil.success(list);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} /api/rdoe/deployments/{deploymentSetId}/report/{type} Get deployment set report
     * @apiVersion 10.0.1
     * @apiName get_deployment_report
     * @apiGroup 03 Deployment
     * @apiSampleRequest /deployments/:deploymentSetId/report/:type
     *
     * @apiDescription Get deployment set report.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} deploymentSetId       Deployment profile ID. For example: D00012372.
     * @apiQuery {String} type                  Report type. For example: deploy/install/backout/restore.
     * @apiQuery {String} [computerName]        Computer name.
     * @apiQuery {String} [targetName]          Target name.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {String}     payload							                    Returned payload data.
     *
     * @apiSuccessExample Success-Response:
     * {
     *    "status": 200,
     *    "returncode": "0",
     *    "message": "OK",
     *    "recordsTotal": 0,
     *    "payload": "                         Deployment Report\r\nRDOe Deployment                                              Jun 27, 2023  22:49:38\r\n                                                     (C) Rocket Software Inc. 2022. All Rights Reserved.\r\n \r\nRelease: CZHUGRP/CZHUAPP/SRC(0.1.1)                                                                          \r\nDeployment Set . . . . . . : D00010001      \r\nEnvironment. . . . . . . . : INV            \r\nDescription. . . . . . . . : Ad Hoc czhu 2023-06-27 22:49:11                   \r\n  \r\nComputer: 10.117.16.109:waldevaldnzcy01\r\n  \r\nReceived Set: /tmp/P10000.10000_DEPLOY.zip\r\n  \r\nFile Name                                                Install Path\r\n---------                                                ------------\r\nTestsrc01.txt                                            c:\\bpi05-dest                                               \r\n  \r\n1 Files Received\r\n"
     * }
     */
    /**
     * Get Deployment Report
     * @param deploymentSetId
     * @param type:
     *             prepare
     *             deploy
     *             install
     *             backout
     *             restore
     * @param computerName : computer name
     * @param targetName : target name
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get deployment report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentSetId", value = "Deployment set ID", required = true, dataType = "java.lang.String", paramType = "path"),
            @ApiImplicitParam(name = "type", value = "type is deploy/install/backout/restore", required = true, dataType = "java.lang.String", paramType = "path"),
            @ApiImplicitParam(name = "computerName", value = "Computer name", required = false, dataType = "java.lang.String", paramType = "query"),
            @ApiImplicitParam(name = "targetName", value = "Target name", required = false, dataType = "java.lang.String", paramType = "query")})
    @GetMapping("deployments/{deploymentSetId}/report/{type}")
    public ResultUtil<String> getDeploymentSetReport(
            @PathVariable("deploymentSetId") String deploymentSetId,
            @PathVariable("type") String type,
            String computerName,
            String targetName
            ) throws RDOException {
        logger.debug("getDeploymentSetReport START");
        if (StringUtils.isEmpty(deploymentSetId) || StringUtils.isEmpty(type)) {
            throw new RDOException("deploymentSetId or report type is empty.");
        }

        String report = deploymentService.getReport(type, deploymentSetId, computerName, targetName);

        return ResultUtil.success(report, "OK");
    }

    /**
     * Download Deployment Report
     * @param deploymentSetId
     * @param type:
     *             prepare
     *             deploy
     *             install
     *             backout
     *             restore
     * @param computerName : computer name
     * @param targetName : target name
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Download deployment report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentSetId", value = "Deployment set ID", required = true, dataType = "java.lang.String", paramType = "path"),
            @ApiImplicitParam(name = "type", value = "type is deploy/install/backout/restore", required = true, dataType = "java.lang.String", paramType = "path"),
            @ApiImplicitParam(name = "computerName", value = "Computer name", required = false, dataType = "java.lang.String", paramType = "query"),
            @ApiImplicitParam(name = "targetName", value = "Target name", required = false, dataType = "java.lang.String", paramType = "query")})
    @GetMapping("download/deployments/{deploymentSetId}/report/{type}")
    public void downloadDeploymentSetReport(
            @PathVariable("deploymentSetId") String deploymentSetId,
            @PathVariable("type") String type,
            String computerName,
            String targetName,
            @RequestParam(value = "format", defaultValue = "txt") String format,
            HttpServletResponse response
    ) throws RDOException, IOException {
        if (StringUtils.isEmpty(deploymentSetId) || StringUtils.isEmpty(type)) {
            throw new RDOException("deploymentSetId or report type is empty.");
        }
        String report = deploymentService.getReport(type, deploymentSetId, computerName, targetName);
        String fileName = type + "." + format;
        if (FileFormat.TXT.getText().equals(format)) {
            fileDownloadService.downloadFromString(report, fileName, response);
        } else {
            List<String> list = Arrays.asList(report.split(System.lineSeparator()));
            String tempPrefix = deploymentSetId + "-" + RequestParamUtil.getUserNameFromToken();
            Path path = PDFUtils.generatePDFOf(list, tempPrefix);
            fileDownloadService.deleteFileAfterDownload(path.toFile(), fileName, response);
        }
    }

    /**
     * Do Action for a deployment set by ALDCS.
     * Command example:
     * ald -p 55555 -c ec372a7e-7d4b-40eb-9dd6-c328ce53fb40 deploy cancel D00010213
     * @param deploymentSetId
     * @param action
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Do Action for a Deployment Set")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentSetId", value = "Deployment Set Id", required = true,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "action", value = "Action for a deployment set. Valid actions are: snooze, dismiss, backout, restore, cancel, prepare, deploy, install", required = true,
                    dataType = "java.lang.String")})
    @PostMapping("deployments/{deploymentSetId}/{action}")
    public ResultUtil<String> deploymentSetAction(@PathVariable("deploymentSetId") String deploymentSetId,
                                                     @PathVariable("action") String action) throws RDOException {
        logger.debug("deploymentSetAction START");
        Boolean deploy = RequestParamUtil.getDeploy(DBEnv.LME.name());
        if(deploy){
            if (StringUtils.isEmpty(deploymentSetId) || StringUtils.isEmpty(action)) {
                throw new RDOException("deploymentSetId or action is empty.");
            }
            this.deploymentService.deploymentSetAction(deploymentSetId, action);
        }else{
            throw new RDORuntimeException(MessageCodeConstants.RDOE06002,messageService.getMessage(MessageCodeConstants.RDOE06002));
        }

        return ResultUtil.success("OK");
    }

    /**
     * Do Target Action for a Deployment Set by ALDCS
     *
     * Command example:
     * ald -p 55555 -c fc771759-7f29-42d6-90f8-bfc2a8945f4c deploy skipdest D00010353 CN-L-2709:ylin_local
     * @param targetName target full name separated by :
     * @param deploymentSetId
     * @param action
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Do Target Action for a Deployment Set")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetName", value = "target full name separated by :", required = true,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "deploymentSetId", value = "Deployment Set Id", required = true,
                    dataType = "java.lang.String"),
            @ApiImplicitParam(name = "action", value = "Target Action for a deployment set. Valid actions are: skip, cancel, deploy, install, backout, restore", required = true,
                    dataType = "java.lang.String")})
    @PostMapping("target/{targetName}/deployments/{deploymentSetId}/{action}")
    public ResultUtil<String> deploymentSetTargetAction(@PathVariable("targetName") String targetName,
                                                  @PathVariable("deploymentSetId") String deploymentSetId,
                                                  @PathVariable("action") String action) throws RDOException {
        logger.debug("deploymentSetTargetAction START");
        Boolean deploy = RequestParamUtil.getDeploy(DBEnv.LME.name());
        if(deploy){
            if (StringUtils.isEmpty(targetName) || StringUtils.isEmpty(deploymentSetId) || StringUtils.isEmpty(action)) {
                throw new RDOException("targetName or deploymentSetId or action is empty.");
            }
            this.deploymentService.deploymentSetTargetAction(targetName, deploymentSetId, action);
        }else{
            throw new RDORuntimeException(MessageCodeConstants.RDOE06002,messageService.getMessage(MessageCodeConstants.RDOE06002));
        }

        return ResultUtil.success("OK");
    }

    /**
     * Modify Deployment profile.
     *
     * @param deploymentProfileVo profile VO.
     *
     * {
     *   "deployNotification": "all",
     *   "emailRecipients": [
     *     "dren@rs.com",
     *     "internal@rs.com"
     *   ],
     *   "id": 10014,(require)
     *   "installNotification": "none",
     *   "snoozeTime": "20",
     *   "timeoutTime": "30"
     * }
     *
     *  installNotification and deployNotification values: none, all, failure.
     *  snoozeTime and timeoutTime unit is minute.
     *
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Modify deployment profile")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentProfileVo", value = "deploymentProfileVo", required = true, 
                    dataType = "com.rocketsoftware.rdoe.controller.vo.deployment.DeploymentProfileVo", paramType = "query")})
    @RequestMapping(value = "profiles", method = RequestMethod.PUT)
    public ResultUtil<Object> modifyProfile(@RequestBody @Valid DeploymentProfileVo deploymentProfileVo) throws RDOException{

        List<String> result = deploymentService.modifyProfile(deploymentProfileVo);

        return ResultUtil.success(result);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} /api/rdoe/deployments/environment/{envId}/info Get deployment info by environment ID
     * @apiVersion 10.0.1
     * @apiName get_env_deployment
     * @apiGroup 03 Deployment
     * @apiSampleRequest /deployments/environment/:envId/info
     *
     * @apiDescription Get deployment info by environment ID.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} envId                 Environment ID.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object}     payload							                        Returned payload data.
     * @apiSuccess {Number}     payload.envId							                  Environment ID.
     * @apiSuccess {String}     payload.abbreviation							          Environment abbreviation.
     * @apiSuccess {String}     payload.distributionProfileOverridable			Deployment profile overridable.
     *
     * @apiSuccessExample Success-Response:
     * {
     *      "status": 200,
     *      "returncode": "0",
     *      "message": "OK",
     *      "recordsTotal": 0,
     *      "payload": {
     *          "envId": 10002,
     *          "abbreviation": "INV",
     *          "distributionProfileOverridable": "1"
     *      }
     * }
     */
    /**
     * @param envId, environment id
     * @return deployment profile base info a the specified environment
     * @throws RDOException
     */
    @ApiOperation(value = "Get deployment info by environment id") @ApiImplicitParams({
        @ApiImplicitParam(name = "envId", value = "Environment ID", required = true, dataType = "java.lang.Integer", paramType = "path")})
    @GetMapping(value = "deployments/environment/{envId}/info")
    public ResultUtil<EnvironmentModel> getEnvDeployInfos(@PathVariable("envId") Integer envId)
        throws RDOException {
        
        List<Integer> envIds = new ArrayList<>();
        envIds.add(envId);
        
        List<EnvironmentModel> envDeployProps = deploymentService.envDeployProps(envIds);
        
        EnvironmentModel model = null;
        if (envDeployProps.size() > 0) {
            model = envDeployProps.get(0);
        }
        return ResultUtil.success(model);
    }

    /**
     * @apiIgnore Will deliver this in the further
     * @api {GET} /api/rdoe/deployments/{profileId}/step Get deployment profile step details by profile ID
     * @apiVersion 10.0.1
     * @apiName get_profile_deployment_step
     * @apiGroup 03 Deployment
     * @apiSampleRequest /deployments/:profileId/step
     *
     * @apiDescription Get deployment profile step details by profile ID.
     * @apiUse RDOP_TOKEN_HEADER
     *
     * @apiQuery {String} envId                 Environment ID.
     *
     * @apiUse RESULT_COMMON_PARAMS
     * @apiSuccess {Object}     payload							                        Returned payload data.
     * @apiSuccess {Number}     payload.distributionProfileId							  Profile ID.
     * @apiSuccess {String}     payload.distributionProfileName							Profile name.
     * @apiSuccess {String}     payload.distributionProfileDesc			        Profile description.
     * @apiSuccess {String}     payload.distributionAutoStart			          Auto start.
     * @apiSuccess {String}     payload.distributionAutoStartName			      Auto start name.
     *
     * @apiSuccessExample Success-Response:
     * {
     *      "status": 200,
     *      "returncode": "0",
     *      "message": "OK",
     *      "recordsTotal": 0,
     *      "payload": {
     *          "distributionProfileId": 10001,
     *          "distributionProfileName": "zcy01",
     *          "distributionProfileDesc": "",
     *          "distributionAutoStart": "3",
     *          "distributionAutoStartName": "Install"
     *      }
     * }
     */
    /**
     * @param profileId, deployment profile id
     * @return Customize deployment profile step details
     * @throws RDOException
     */
    @ApiOperation(value = "Get deployment profile step details by profile id") @ApiImplicitParams({
        @ApiImplicitParam(name = "profileId", value = "Profile ID", required = true, dataType = "java.lang.Integer", paramType = "path")})
    @GetMapping(value = "deployments/{profileId}/step")
    public ResultUtil<DeploymentProfileModel> getProfilesStepDetails(
        @PathVariable("profileId") Integer profileId) throws RDOException {
        DeploymentProfileModel deployProfileStep = deploymentService.getDeploymentProfileStep(profileId);
        // Customize the step name by step ID
        if (deployProfileStep != null) {
            if (deployProfileStep.getDistributionAutoStart().equals("1")) {
                deployProfileStep.setDistributionAutoStartName("Prepare");
            } else if (deployProfileStep.getDistributionAutoStart().equals("2")) {
                deployProfileStep.setDistributionAutoStartName("Deploy");
            } else if (deployProfileStep.getDistributionAutoStart().equals("3")){
                deployProfileStep.setDistributionAutoStartName("Install");
            } else {
                deployProfileStep.setDistributionAutoStartName("Add");
            }
        }
        return ResultUtil.success(deployProfileStep);
    }

    /**
     * get the operation system list
     * @param forceRefresh
     * @return
     * @throws RDOException
     */
    @ApiOperation(value = "Get operation system list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "forceRefresh", value = "Real-time data acquisition", defaultValue = "false", required = false, dataType = "java.lang.Boolean", paramType = "query")})
    @GetMapping(value = "operationSystem/list")
    public ResultUtil<List<OperationSystemModel>> getOperationSystemList(@RequestParam(name="forceRefresh", defaultValue = "false") Boolean forceRefresh)
            throws RDOException {
        List<OperationSystemModel> osModel = null;
        if(forceRefresh){
            osModel = deploymentService.getRealtimeOperationSystemList();
        }else{
            osModel = deploymentService.getNonRealtimeOperationSystemList();
        }

        return ResultUtil.success(osModel);
    }

    @ApiOperation(value = "Download deployment reports")
    @PostMapping("deployment/reports")
    public ResponseEntity<StreamingResponseBody> generateReports(@RequestBody DeploymentReportDownloadVo deploymentReportDownloadVo) throws RDOException, FileNotFoundException {

        Path path = deploymentService.generateReports(deploymentReportDownloadVo);
        File file = path.toFile();
        InputStream inputStream = new DeleteFileInputStream(file);
        String fileName = file.getName().substring(0, file.getName().indexOf("#")) + ".zip";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.LAST_MODIFIED, String.valueOf(file.lastModified()));
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        httpHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));

        StreamingResponseBody responseBody = outputStream -> {
            int bytesSize;
            byte[] buffer = new byte[64 * 1024];
            while ((bytesSize = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, bytesSize);
            }
            inputStream.close();
        };

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @ApiOperation(value = "get computers list")
    @GetMapping(value = "deployment/computerList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "computer name", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "description", value = "computer description", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "osId", value = "os id", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "osVersion", value = "os version", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "deployAllowed", value = "deploy allowed", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "client", value = "client", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" )})
    public ResultUtil<List<Computer>> getComputerList(String name, String description, String osId, String osVersion, String deployAllowed, String client) throws RDOException {
        List<Computer> computerList = deploymentService.getComputerList(name, description, osId, osVersion, deployAllowed, client);
        return ResultUtil.success(computerList);
    }

    @ApiOperation(value = "get computers list counts")
    @GetMapping(value = "deployment/computerCounts")
    public ResultUtil<Integer> getComputerCounts() throws RDOException {
        Integer computerCounts = deploymentService.getComputerCounts();
        return ResultUtil.success(computerCounts);
    }

    @ApiOperation(value = "get computers map")
    @GetMapping(value = "deployment/computersMap")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "computer name", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "description", value = "computer description", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "osId", value = "os id", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "osVersion", value = "os version", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "deployAllowed", value = "deploy allowed", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" ),
            @ApiImplicitParam(name = "client", value = "client", required = false,
                    dataType = "java.lang.String", paramType = "query", defaultValue = "" )})
    public ResultUtil<Map<String, Object>> getComputersMap(String name, String description, String osId, String osVersion, String deployAllowed, String client) throws RDOException {
        Map<String, Object> computersMap = deploymentService.getComputersMap(name, description, osId, osVersion, deployAllowed, client);
        return ResultUtil.success(computersMap);
    }
}
