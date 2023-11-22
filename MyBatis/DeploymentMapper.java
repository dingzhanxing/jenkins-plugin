package com.rocketsoftware.rdoe.dao.rdoe.mapper;

import java.util.List;
import java.util.Map;

import com.rocketsoftware.rdoe.controller.vo.deployment.Computer;
import com.rocketsoftware.rdoe.dao.rdoe.model.DeploymentProfileModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.DeploymentSetModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.EnvironmentModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.OperationSystemModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.TargetModel;
import org.apache.ibatis.annotations.Param;

import com.rocketsoftware.common.exception.RDOException;
import com.rocketsoftware.rdoe.controller.vo.deployment.DeploymentListVo;

/**
 * @ClassName: DeploymentMapper
 * @Author: czhu
 * @Date: 2020-12-10 13:11
 **/
public interface DeploymentMapper {
    /**
     *
     * @param paramMap
     * @return
     * @throws RDOException
     */
    List<DeploymentListVo> getDeploymentList(Map<String, String> paramMap) throws RDOException;

    /**
     *
     * @return
     * @throws RDOException
     */
    List<Integer> getRecoverDeploymentSetList() throws RDOException;

    /**
     * Get all deployment sets by a given profile id.
     * @param profileId
     * @return
     * @throws RDOException
     */
    List<DeploymentListVo> getDeploymentProfileSets(@Param("profileId") Integer profileId) throws RDOException;

    /**
     * Get target all info based on a given target id.
     * @param targetId
     * @return
     * @throws RDOException
     */
    TargetModel getTargetInfo(@Param("targetId") String targetId) throws RDOException;

    /**
     * Get deployment set lists by target name
     *
     * @param targetId
     * @return
     * @throws RDOException
     */
    List<DeploymentSetModel> getDeploymentListByTargetSQL(@Param("targetId") String targetId, @Param("deploymentSetIds") List<DeploymentSetModel> deploymentSetIds) throws RDOException;

    List<DeploymentSetModel> getNoneDuplicatedDeploymentsByTargetSQL(@Param("targetId") String targetId) throws RDOException;
    
    /**
     * @param envId, environment id
     * @return
     * @throws RDOException
     */
    List<DeploymentProfileModel> getProfilesByEnvId(@Param("envId") Integer envId)
        throws RDOException;

    /**
     * @param prfId, deployment profile id
     * @return
     * @throws RDOException
     */
    DeploymentProfileModel getProfileStepDetails(@Param("prfId") Integer prfId) throws RDOException;

    /**
     * @param envIds, environment id
     * @return
     * @throws RDOException
     */
    List<EnvironmentModel> getEnvDeployProps(@Param("envIds") List<Integer> envIds) throws RDOException;

    /**
     * get operation system list
     * @return
     * @throws RDOException
     */
    List<OperationSystemModel> getOperationSystemList() throws RDOException;

    List<Computer> getComputerList(String name, String description, String osId, String osVersion, String deployAllowed, String client);

    Integer getComputerCounts(String name, String description, String osId, String osVersion, String deployAllowed, String client);



}
