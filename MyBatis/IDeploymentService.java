package com.rocketsoftware.rdoe.service;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rocketsoftware.common.exception.RDOException;
import com.rocketsoftware.rdoe.controller.vo.deployment.*;
import com.rocketsoftware.rdoe.dao.rdoe.model.DeploymentProfileModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.DeploymentSetModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.EnvironmentModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.OperationSystemModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.ProfileModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.TargetModel;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletResponse;

/**
 * @InterfaceName: IDeploymentService
 * @Description: Define the interface method for common deployment services
 * @Author: Rocket
 * @Date: 2020-12-17 13:44
 **/
public interface IDeploymentService {

    /**
     * @param startDate
     * @param endDate
     * @return
     * @throws RDOException
     */
    List<DeploymentListVo> getDeploymentList(String startDate, String endDate, String keyword, String status, String searchScope) throws RDOException;

    /**
     * Get deployment list by profileId
     * @param profileId
     * @return
     * @throws RDOException
     */
    List<DeploymentListVo> getDeploymentListByProfileId(Integer profileId) throws RDOException;

    /**
     * Get target all info based on a given target id.
     * @param targetId
     * @return
     * @throws RDOException
     */
    TargetModel getTargetInfo(@Param("targetId") String targetId) throws RDOException;

    /**
     * Get profile info based on a given profile id.
     * @param profileId
     * @return
     * @throws RDOException
     */
    ProfileModel getProfileInfo(String profileId) throws RDOException;

    /**
     * Get deployment sets based on a given target id.
     * @param targetId
     * @return
     * @throws RDOException
     */
    List<DeploymentListVo> getDeploymentListByTarget(String targetId) throws RDOException;

    /**
     * Get deployment sets based on a given target id by SQL
     *
     * @param targetId
     * @return
     * @throws RDOException
     */
    List<DeploymentSetModel> getDeploymentListByTargetSQL(String targetId) throws RDOException;

    /**
     * Modify Deployment Set Target
     * @param targetVo
     * @return
     * @throws RDOException
     */
    String modifyDeploymentSetTarget(DeploymentSetTargetVo targetVo) throws RDOException;

    /**
     * Create a Target
     * @param targetVo
     * @return
     * @throws RDOException
     */
    String createDeploymentSetTarget(Computer targetVo) throws RDOException;

    /**
     * Do action for a deployment set.
     * @param deploymentSetId
     * @param action
     * @return
     * @throws RDOException
     */
    String deploymentSetAction(String deploymentSetId, String action) throws RDOException;

    /**
     * Do target action for a deployment set.
     * @param targetName
     * @param deploymentSetId
     * @param action
     * @return
     * @throws RDOException
     */
    String deploymentSetTargetAction(String targetName, String deploymentSetId, String action) throws RDOException;

    /**
     * Get report for a deployment set.
     *
     * type is:
     *         deploy
     *         install
     *         backout
     *         restore
     *
     * @param type
     * @param deploymentSetId
     * @param computerName
     * @param targetName
     * @return
     * @throws RDOException
     */
    String getReport(String type, String deploymentSetName, String computerName, String targetName) throws RDOException;

    /**
     * Modify deployment profile
     *
     * @param DeploymentProfileVo
     * @return
     * @throws RDOException
     */

    List<String> modifyProfile(DeploymentProfileVo deploymentProfileVo) throws RDOException;

    /**
     * @param envId, environment id
     * @return the details of the environment deployment base info, for example the default profile
     * @throws RDOException
     */
    List<EnvironmentModel> envDeployProps(List<Integer> envIds) throws RDOException;

    /**
     * @param prfId, deployment profile id
     * @return The details of a specified deployment profile
     * @throws RDOException
     */
    DeploymentProfileModel getDeploymentProfileStep(Integer prfId) throws RDOException;

    /**
     * get real time operation system list
     * @return
     * @throws RDOException
     */
    List<OperationSystemModel> getRealtimeOperationSystemList() throws RDOException;

    /**
     * get non real operation system list
     * @return
     * @throws RDOException
     */
    List<OperationSystemModel> getNonRealtimeOperationSystemList() throws RDOException;

    Path generateReports(DeploymentReportDownloadVo deploymentReportDownloadVo) throws RDOException;

    List<Computer> getComputerList(String name, String description, String osName, String osVersion, String deployAllowed, String client) throws RDOException;

    Integer getComputerCounts() throws RDOException;

    Map<String, Object> getComputersMap(String name, String description, String osName, String osVersion, String deployAllowed, String client) throws RDOException;


}
