/**
 *
 */
package com.rocketsoftware.rdoe.service.impl;

import com.github.pagehelper.Page;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.rocketsoftware.common.config.aspect.annotation.LogRecord;
import com.rocketsoftware.common.custom.DeleteFileInputStream;
import com.rocketsoftware.common.def.FileFormat;
import com.rocketsoftware.common.def.MessageCodeConstants;
import com.rocketsoftware.common.exception.RDOException;
import com.rocketsoftware.common.exception.RDORuntimeException;
import com.rocketsoftware.common.service.IMessageService;
import com.rocketsoftware.common.util.*;
import com.rocketsoftware.rdoe.ald.deployment.*;
import com.rocketsoftware.rdoe.common.DeploymentSetActionType;
import com.rocketsoftware.rdoe.common.DeploymentSetTargetActionType;
import com.rocketsoftware.rdoe.controller.vo.deployment.*;
import com.rocketsoftware.rdoe.dao.rdoe.mapper.DeploymentMapper;
import com.rocketsoftware.rdoe.dao.rdoe.model.*;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.ProfileModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.deployment.TargetModel;
import com.rocketsoftware.rdoe.dao.rdoe.model.enums.ReportType;
import com.rocketsoftware.rdoe.service.IAldService;
import com.rocketsoftware.rdoe.service.IDeploymentService;
import com.rocketsoftware.rdoe.utils.RDOeMessageCodeConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Rocket
 * @date 12/09/2020
 *
 */
@Service public class DeploymentService implements IDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    @Autowired @Qualifier("sqlSessionFactoryForLME") private SqlSessionFactory
        sqlSessionFactoryForLME;

    @Qualifier("sqlSessionTemplateForLME") private SqlSessionTemplate sqlSessionTemplateForLME;

    @Autowired private DeploymentMapper deploymentMapper;
    @Autowired private IAldService aldService;
    @Autowired
    private IMessageService messageService;

    @Autowired
    private DeploymentSetReportService deploymentSetReportService;

    @Override
    public String deploymentSetTargetAction(String targetName, String deploymentSetId, String action) throws RDOException {
        DeploymentSetTargetActionType[] types = DeploymentSetTargetActionType.values();
        action = action.toLowerCase();
        boolean flag = false;
        for(DeploymentSetTargetActionType type : types) {
            if(type.name().equals(action)) {
                flag = true;
                break;
            }
        }
        if(!flag) {
            throw new RDOException("The Target Action For Deployment Set is invalid.");
        }

        Stopwatch watch = Stopwatch.createStarted();
        DeploymentSetTargetActionCommand command = new DeploymentSetTargetActionCommand();
        command.setDeploymentSetTargetActionParams(targetName, deploymentSetId, action);
        aldService.exec(command);
        logger.info("DeploymentService deploymentSetTargetAction total time consumed in {} seconds.", watch);
        return command.getResult();
    }

    @Override
    public String deploymentSetAction(String deploymentSetId, String action) throws RDOException {
        DeploymentSetActionType [] types = DeploymentSetActionType.values();
        action = action.toLowerCase();
        boolean flag = false;
        for(DeploymentSetActionType type : types) {
            if(type.name().equals(action)) {
                flag = true;
                break;
            }
        }
        if(!flag) {
            throw new RDOException("The Action For Deployment Set is invalid.");
        }

        Stopwatch watch = Stopwatch.createStarted();
        DeploymentSetActionCommand command = new DeploymentSetActionCommand();
        command.setDeploymentSetActionParams(deploymentSetId, action);
        aldService.exec(command);
        logger.info("DeploymentService deploymentSetAction total time consumed in {} seconds.", watch);
        return command.getResult();
    }

    @Override
    public String createDeploymentSetTarget(Computer targetVo) throws RDOException {
        Stopwatch watch = Stopwatch.createStarted();
        DeploymentSetCreateTargetCommand command = new DeploymentSetCreateTargetCommand();
        command.setDeploymentSetCreateTargetParams(targetVo);
        aldService.exec(command);
        logger.info("DeploymentService createDeploymentSetTarget total time consumed in {} seconds.", watch);
        return command.getResult();
    }

    @Override
    public String modifyDeploymentSetTarget(DeploymentSetTargetVo targetVo) throws RDOException {
        Stopwatch watch = Stopwatch.createStarted();
        DeploymentSetModifyTargetCommand command = new DeploymentSetModifyTargetCommand();
        command.setDeploymentSetModifyTargetParams(targetVo);
        aldService.exec(command);
        logger.info("DeploymentService modifyDeploymentSetTarget total time consumed in {} seconds.", watch);
        return command.getResult();
    }

    @Override
    public List<DeploymentListVo> getDeploymentListByTarget(String targetId) throws RDOException {
        Stopwatch watch = Stopwatch.createStarted();
        DeploymentSetTargetCommand command = new DeploymentSetTargetCommand();
        command.setDeploymentSetTargetParams(targetId);
        aldService.exec(command);
        logger.info("DeploymentService getDeploymentListByTarget total time consumed in {} seconds.", watch);
        return command.getDeploymentList();
    }

    @Override
    public List<DeploymentSetModel> getDeploymentListByTargetSQL(String targetId) throws RDOException {
        PageUtil.startPage("DSTDESSTTS DESC");
        List<DeploymentSetModel> noneDuplicatedList = deploymentMapper.getNoneDuplicatedDeploymentsByTargetSQL(targetId);
        PageUtil.clearPage();

        List<DeploymentSetModel> deploymentSetModelList = this.deploymentMapper.getDeploymentListByTargetSQL(targetId, noneDuplicatedList);
        Map<String, DeploymentSetModel> mapData = Maps.newHashMap();
        Page<DeploymentSetModel> filteredPageList = new Page<>();
        Page<DeploymentSetModel> noneDuplicatedPageList = (Page) noneDuplicatedList;
        filteredPageList.setPageNum(noneDuplicatedPageList.getPageNum());
        filteredPageList.setPageSize(noneDuplicatedPageList.getPageSize());
        filteredPageList.setTotal(noneDuplicatedPageList.getTotal());
        for (DeploymentSetModel deploymentSetModel : deploymentSetModelList) {
            String statusCode = deploymentSetModel.getDestinationStatusCode();
            statusCode = StringUtils.trim(statusCode);

            String deploymentSetName = deploymentSetModel.getName();
            DeploymentSetModel itemModel = mapData.get(deploymentSetName);
            if(itemModel == null) {
                mapData.put(deploymentSetName, deploymentSetModel);
            } else {
                // found duplicated item
                if(!"1".equals(statusCode)) {
                    mapData.put(deploymentSetName, deploymentSetModel);
                }
                // remove it.
                logger.debug("Found duplicated item, {}", deploymentSetName);
            }
        }

        Date utcNow = DateTimeUtil.getCurrentUTCTime();
        for (DeploymentSetModel noneDuplicatedModel : noneDuplicatedList) {
            DeploymentSetModel deploymentSetModel = mapData.get(noneDuplicatedModel.getName());
            if(deploymentSetModel == null) {
                logger.warn("Failed to get target deployment set model.");
                continue;
            }

            Integer recoverCount = deploymentSetModel.getRecoverCount();
            if(null != recoverCount && recoverCount > 0) {
                deploymentSetModel.setDestinationStatus("Recovered");
                // need to add to the list if recovered item found.
                filteredPageList.add(deploymentSetModel);
                continue;
            }
            String statusCode = deploymentSetModel.getDestinationStatusCode();
            statusCode = StringUtils.trim(statusCode);
            String stepCode = deploymentSetModel.getDestinationStepCode();
            stepCode = StringUtils.trim(stepCode);
            Boolean isTimeout = false;
            Date ntfyTime = deploymentSetModel.getNtfyTime();
            String ntfyTimeStr = DateTimeUtil.parseDateToYMDSSString(ntfyTime);
            if ((ntfyTime != null && ntfyTime.before(utcNow)) || (StringUtils.isNotEmpty(ntfyTimeStr)
                    && ntfyTimeStr.indexOf("2999-01-01") != -1)) {
                isTimeout = true;
            }

            String theStatus = "";
            if("1".equals(statusCode)) {
                switch (stepCode) {
                    case "3": theStatus = statusAsString(statusCode, isTimeout, recoverCount); break;
                    case "5":
                    case "6": theStatus = "Success"; break;
                    default: theStatus = "Action Required";
                }
            } else {
                theStatus = statusAsString(statusCode, isTimeout, recoverCount);
            }

            deploymentSetModel.setDestinationStatus(theStatus);
            filteredPageList.add(deploymentSetModel);
        }

        return filteredPageList;
    }

    private String statusAsString(String statusCode, Boolean isTimeout, Integer recovcnt) {
        if (recovcnt != null && recovcnt > 0) {
            return "Recovered";
        }
        if ("0".equals(statusCode) && isTimeout) {  // time out only if in progress
            return "Timed Out";
        } else {
            switch (statusCode) {
                case "0":  return "Started";
                case "1":  return "Success";
                case "2":  return "Failed";
                case "3":  return "Canceled";
                case "4":  return "Skipped";
            }
        }
        return String.format("status [%s]", statusCode);
    }

    @Override
    public ProfileModel getProfileInfo(String profileId) throws RDOException {
        Stopwatch watch = Stopwatch.createStarted();
        DeploymentProfileInfoCommand command = new DeploymentProfileInfoCommand();
        command.setProfileInfoPrams(profileId);
        aldService.exec(command);
        logger.info("DeploymentService getProfileInfo total time consumed in {} seconds.", watch);
        return command.getProfileModel();
    }

    @Override
    public TargetModel getTargetInfo(String targetId) throws RDOException {
        // fix bugs as we need to change targetid to uppercase before searching.
        TargetModel targetModel = this.deploymentMapper.getTargetInfo(targetId.toUpperCase());

        if(null == targetModel) {
            String params[] = {targetId};
            throw new RDOException(MessageCodeConstants.RDOE06001, messageService.getMessage(MessageCodeConstants.RDOE06001, params));
        }

        // remove space
        targetModel.setDescription(StringUtils.trim(targetModel.getDescription()));
        targetModel.setOsDescription(StringUtils.trim(targetModel.getOsDescription()));
        targetModel.setOsName(StringUtils.trim(targetModel.getOsName()));
        TargetModel.DCInfo dc = new TargetModel.DCInfo();
        dc.setActive(false);
        if(targetModel.getDmStatus() != null && targetModel.getDmStatus() == 1) {
            dc.setActive(true);
        }
        dc.setExternal(false);
        if(StringUtils.isNotEmpty(targetModel.getDmExternal()) && NumberUtils.isParsable(targetModel.getDmExternal())) {
            Integer result = Integer.parseInt(targetModel.getDmExternal());
            if(result == 1) {
                dc.setExternal(true);
            }
        }
        dc.setHost(targetModel.getHost());
        dc.setPort(targetModel.getPort());
        targetModel.setDc(dc);

        TargetModel.DSInfo ds = new TargetModel.DSInfo();
        String distServer = targetModel.getDistServer();
        if(StringUtils.isNotEmpty(distServer)) {
            int pos = distServer.indexOf(":");
            if(pos > 0) {
                ds.setHost(distServer.substring(0, pos));
                String tempPort = distServer.substring(pos + 1);
                if(NumberUtils.isParsable(tempPort)) {
                    ds.setPort(Integer.parseInt(tempPort));
                }
            } else {
                ds.setHost(distServer);
            }
        }

        targetModel.setDs(ds);
        return targetModel;
    }

    @Override
    public List<DeploymentListVo> getDeploymentListByProfileId(Integer profileId) throws RDOException {
        Stopwatch watch = Stopwatch.createStarted();
        Date utcDateTime = DateTimeUtil.getCurrentUTCTime();
        List<Integer> problemSetIds = deploymentMapper.getRecoverDeploymentSetList();
        // start to paginate
        PageUtil.startPage("STATECHANGETIMESTAMP DESC");
        List<DeploymentListVo> dList = this.deploymentMapper.getDeploymentProfileSets(profileId);
        for (DeploymentListVo vo : dList) {
            vo.setDeploymentSetName(StringUtils.trim(vo.getDeploymentSetName()));
            vo.setStatus(getDeploymentSetStatus(vo.getDeploymentSetStatus(),// deploymentSetStatusVal
                    vo.getDeploymentSetId(), vo.getDstntfytim(), // deploymentSetNotifyTime
                    vo.getDststatcde(), // deploymentSetDestinationStatusCode
                    vo.getLstreqstat(), // lastRequestedStatus
                    problemSetIds, utcDateTime));
        }
        logger.info("Time consumed: {}", watch);
        return dList;
    }


    @LogRecord
    @Override public List<DeploymentListVo> getDeploymentList(String startDate, String endDate, String keyword, String status, String searchScope)
        throws RDOException {
        Map<String, String> paramMap = Maps.newHashMap();
        Date utcDateTime = DateTimeUtil.getCurrentUTCTime();
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);

        keyword = StringUtil.getKeywordWrapper(keyword).toUpperCase();
        paramMap.put("keyword",keyword);

        paramMap.put("status", status);
        paramMap.put("searchScope", searchScope);
        paramMap.put("utcDateTime", DateTimeUtil.parseDateToYMDSSString(new Date()));

        List<Integer> problemSetIds = deploymentMapper.getRecoverDeploymentSetList();
        PageUtil.startPage("EFFSTRTS DESC");
        List<DeploymentListVo> dList = this.deploymentMapper.getDeploymentList(paramMap);
        List<DeploymentSetReportSNPModel> rptSnpList = getRptSnpList(dList);

        for (DeploymentListVo vo : dList) {
            String destinationName = "-";
            String destinationTargetName = vo.getDestinationTargetName();
            String destinationComputerName = vo.getDestinationComputerName();
            logger.debug("Calculate status for {}", vo.getDeploymentSetName());
            if (StringUtils.isNotEmpty(destinationComputerName)) {
                String destComName = StringUtils.trim(destinationComputerName);
                if (destComName.length() > 25) {
                    destComName = destComName.substring(0, 25);
                }
                if (StringUtils.isNotEmpty(destinationTargetName)) {
                    destinationName = destComName + " : " + StringUtils.trim(destinationTargetName);
                } else {
                    destinationName = destComName;
                }
            }
            vo.setDestinationName(destinationName);

            if (StringUtils.isNotEmpty(destinationComputerName)) {
                vo.setDestinationComputerName(StringUtils.trim(destinationComputerName));
            } else {
                vo.setDestinationComputerName("-");
            }
            if (StringUtils.isNotEmpty(destinationTargetName)) {
                vo.setDestinationTargetName(StringUtils.trim(destinationTargetName));
            } else {
                vo.setDestinationTargetName("-");
            }

            if (StringUtils.isNotEmpty(vo.getDeploymentProfileName())) {
                vo.setDeploymentProfileName(StringUtils.trim(vo.getDeploymentProfileName()));
            } else {
                vo.setDeploymentProfileName("-");
            }

            if (StringUtils.isNotEmpty(vo.getDeploymentStep())) {
                vo.setDeploymentStep(StringUtils.trim(vo.getDeploymentStep()));
            } else {
                vo.setDeploymentStep("-");
            }
            if (StringUtils.isNotEmpty(vo.getDeploymentSetName())) {
                vo.setDeploymentSetName(StringUtils.trim(vo.getDeploymentSetName()));
            } else {
                vo.setDeploymentSetName("-");
            }

            vo.setServiceType("lme");
            vo.setStatus(getDeploymentDestinationStatus(vo.getDststatval(),// deploymentSetStatusVal
                vo.getDeploymentSetId(), vo.getDstntfytim(), // deploymentSetNotifyTime
                vo.getDststatcde(), // deploymentSetDestinationStatusCode
                vo.getLstreqstat(), // lastRequestedStatus
                problemSetIds, utcDateTime));

            resetStatus(vo, rptSnpList);
        }
        return dList;
    }

    private void resetStatus(DeploymentListVo deploymentListVo, List<DeploymentSetReportSNPModel> rptSnpList) {
        if (deploymentListVo != null &&
                rptSnpList != null &&
                deploymentSetReportService.isAfterInstall(deploymentListVo.getDeploymentStep())) {
            Boolean verify = rptSnpList.stream().filter(r -> {
                ReportType reportType = ReportType.valueOf(deploymentListVo.getDeploymentStep().toUpperCase());
                if (r.getDstSetType() != reportType.ordinal()) {
                    return false;
                }
                boolean match = false;
                if (deploymentListVo.getDeploymentSetId() != null
                        && deploymentListVo.getDeploymentSetId().toString().equals(r.getDstSetId())
                        && StringUtils.isNotBlank(deploymentListVo.getDestinationComputerId())
                        && deploymentListVo.getDestinationComputerId().equals(r.getComputerId())) {

                    if (StringUtils.isNotBlank(deploymentListVo.getDestinationTargetId())) {
                        if (deploymentListVo.getDestinationTargetId().equals(r.getDstTargetId())) match = true;
                    } else if (StringUtils.trimToNull(r.getDstTargetId()) == null) {
                        match = true;
                    }
                }
                return match;
            }).allMatch(r -> {
                String sigListStr = r.getSigList();
                Boolean vf = Boolean.TRUE;
                if (StringUtils.isNotBlank(sigListStr)) {
                    Map<String, Object> sigListMap = JacksonUtil.convertToMap(sigListStr);
                    if (sigListMap != null) {
                        for (String key: sigListMap.keySet()) {
                            if (DeploymentSetReportService.DELETE_FILES_COLLECTION_FLAG.equals(key)) continue;
                            Map<String, Object> itemMap = (Map<String, Object>)sigListMap.get(key);
                            if (itemMap != null && itemMap.containsKey("verify")) {
                                String client = (String)itemMap.get("client");
                                if (ReportType.BACKOUT.name().equals(deploymentListVo.getDeploymentStep().toUpperCase()) &&
                                        StringUtils.isBlank(client)) continue;
                                vf = (Boolean)itemMap.get("verify");
                                if (!vf) break;
                            }
                        }
                    }
                }
                return vf;
            });

            deploymentListVo.setVerify(verify);
        }
    }

    private List<DeploymentSetReportSNPModel> getRptSnpList(List<DeploymentListVo> dList) {
        List<DeploymentSetReportSNPModel> result = null;
        if (dList != null) {
            MutablePair<List<Integer>, Set<ReportType>> pair = dList.stream()
                    .filter(d -> deploymentSetReportService.isAfterInstall(d.getDeploymentStep()))
                    .collect(MutablePair::new, (c, d) -> {
                        List leftList = c.getLeft();
                        if (leftList == null) {
                            leftList = new ArrayList<>();
                            c.setLeft(leftList);
                        }
                        Set rightSet = c.getRight();
                        if (rightSet == null) {
                            rightSet = new HashSet();
                            c.setRight(rightSet);
                        }
                        if (d.getDeploymentSetId() != null) {
                            leftList.add(d.getDeploymentSetId());
                            rightSet.add(ReportType.valueOf(d.getDeploymentStep().toUpperCase()));
                        }
                    }, (a, b) -> {});

            if (pair.getLeft() != null && pair.getLeft().size() > 0) {
                result = deploymentSetReportService.getSigListByDstIdList(pair.getLeft(), new ArrayList<>(pair.getRight()));
            }
        }
        return result;
    }

    @Override
    public String getReport(String type, String deploymentSetName, String computerName, String targetName) throws RDOException {

        DeploymentSetReportCommand deploymentSetReportCommand = new DeploymentSetReportCommand();

        deploymentSetReportCommand.getReport(type, deploymentSetName, computerName, targetName);
        DeploymentSetReportCommand command = (DeploymentSetReportCommand) aldService.exec(deploymentSetReportCommand);

        return command.getResult();
    }

    /**
     * Batch download deployment related reports
     */
    public Path generateReports(DeploymentReportDownloadVo deploymentReportDownloadVo) throws RDOException {

        String type = deploymentReportDownloadVo.getType();
        String deploymentSetName = deploymentReportDownloadVo.getDeploymentSetName();
        Optional<List<Map<String, String>>> targetList = Optional.of(deploymentReportDownloadVo.getTargetList());

        return targetList.filter(t -> t != null && t.size() > 0)
                .map(t -> {
                    Path path = null;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                        LocalDateTime localDateTime = LocalDateTime.now();
                        String dateTimeStr = localDateTime.format(formatter);
                        StringBuilder tempFileNamePrefixBuilder = new StringBuilder();
                        tempFileNamePrefixBuilder.append(deploymentSetName)
                                .append("_")
                                .append(type)
                                .append( "_" )
                                .append(dateTimeStr)
                                .append("#");
                        path = Files.createTempFile(tempFileNamePrefixBuilder.toString(), ".zip");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    StringBuilder errorsBuilder = new StringBuilder();

                    String format = deploymentReportDownloadVo.getFormat();
                    if (StringUtils.isBlank(format)) {
                        format = FileFormat.TXT.getText();
                    }

                    try(ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(path.toFile()))) {
                        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);

                        byte[] buffer = new byte[64 * 1024];
                        int readSize = 0;

                        for (Map<String, String> m : t) {
                            String computerName = m.get("computerName");
                            String targetName = m.get("targetName");
                            String stepStatus = m.get("stepStatus");

                            if (StringUtils.isBlank(computerName)) {
                                continue;
                            }

                            DeploymentSetReportCommand deploymentSetReportCommand = new DeploymentSetReportCommand();
                            deploymentSetReportCommand.getReport(type, deploymentSetName, computerName, targetName);
                            DeploymentSetReportCommand command;
                            String result = "";
                            try {
                                command = (DeploymentSetReportCommand) aldService.exec(deploymentSetReportCommand);
                                result = command.getResult();
                            } catch (Exception e) {
                                errorsBuilder.append(messageService.getMessage(RDOeMessageCodeConstants.RDOE40001, new String[]{computerName, targetName, type, e.getMessage()}))
                                        .append(System.lineSeparator());
                                continue;
                            }

                            if (StringUtils.isNotBlank(result)) {
                                StringBuilder entryNameBuilder = new StringBuilder();
                                entryNameBuilder.append(computerName)
                                        .append("_")
                                        .append(targetName)
                                        .append( "_" )
                                        .append(type)
                                        .append( "_" )
                                        .append(stepStatus);

                                InputStream inputStream = null;
                                if (FileFormat.TXT.getText().equals(format)) {
                                    inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
                                } else {
                                    String[] arr = result.split(System.lineSeparator());
                                    List<String> lines = Arrays.asList(arr);
                                    StringBuilder prefixBuilder = new StringBuilder(entryNameBuilder.toString());
                                    prefixBuilder.append("-")
                                            .append(deploymentSetName)
                                            .append("-")
                                            .append(RequestParamUtil.getUserNameFromToken());
                                    Path tempPdfPath = PDFUtils.generatePDFOf(lines, prefixBuilder.toString());
                                    inputStream = new DeleteFileInputStream(tempPdfPath.toFile());
                                }

                                try {
                                    entryNameBuilder.append(".").append(format);
                                    String entryName = entryNameBuilder.toString();
                                    ZipEntry zipEntry = new ZipEntry(entryName);
                                    zipOutputStream.putNextEntry(zipEntry);
                                    while((readSize = inputStream.read(buffer)) >= 0) {
                                        zipOutputStream.write(buffer, 0, readSize);
                                    }
                                } finally {
                                    zipOutputStream.closeEntry();
                                    if (inputStream != null) inputStream.close();
                                }
                            }
                        }

                        String errors = errorsBuilder.toString();
                        if (StringUtils.isNotBlank(errors)) {
                            String failures = "failures." + format;
                            ZipEntry zipEntry = new ZipEntry(failures);
                            zipOutputStream.putNextEntry(zipEntry);
                            InputStream inputStream = null;
                            try {
                                if (FileFormat.PDF.getText().equals(format)) {
                                    List<String> list = Arrays.asList(errors.split(System.lineSeparator()));
                                    Path failuresFilePath = PDFUtils.generatePDFOf(list,"failures_rdoe");
                                    inputStream = new DeleteFileInputStream(failuresFilePath.toFile());
                                } else {
                                    inputStream = new ByteArrayInputStream(errors.getBytes(StandardCharsets.UTF_8));
                                }

                                while((readSize = inputStream.read(buffer)) >= 0) {
                                    zipOutputStream.write(buffer, 0, readSize);
                                }
                            } catch (Exception e) {
                                throw new RDORuntimeException(MessageCodeConstants.RDO_COMMON_01003,
                                        CommonMessageUtil.getMessage(MessageCodeConstants.RDO_COMMON_01003,
                                                new Object[] {failures, e.getMessage()}), e);
                            } finally {
                                if (inputStream != null ) inputStream.close();
                            }
                            zipOutputStream.closeEntry();
                        }

                        zipOutputStream.flush();

                    } catch (IOException e) {
                        throw new RDORuntimeException(e);
                    }

                    return path;
                }).orElse(null);
    }

    @Override
    public List<String> modifyProfile(DeploymentProfileVo deploymentProfileVo) throws RDOException{

        DeploymentProfileConfCommand deploymentProfileConfCommand = new DeploymentProfileConfCommand();

        if(deploymentProfileVo.getEmailRecipients() != null){
            deploymentProfileConfCommand.setEmailRecipients(deploymentProfileVo.getEmailRecipients());
        }

        if (StringUtils.isNotEmpty(deploymentProfileVo.getDeployNotification())) {
            deploymentProfileConfCommand.setDeployNotification(deploymentProfileVo.getDeployNotification());
        }

        if (StringUtils.isNotEmpty(deploymentProfileVo.getInstallNotification())) {
            deploymentProfileConfCommand.setInstallNotification(deploymentProfileVo.getInstallNotification());
        }

        if (StringUtils.isNotEmpty(deploymentProfileVo.getTimeoutTime())) {
            deploymentProfileConfCommand.setTimeoutTime(deploymentProfileVo.getTimeoutTime());
        }

        if (StringUtils.isNotEmpty(deploymentProfileVo.getSnoozeTime())) {
            deploymentProfileConfCommand.setSnoozeTime(deploymentProfileVo.getSnoozeTime());
        }

        deploymentProfileConfCommand.setId(deploymentProfileVo.getId());

        DeploymentProfileConfCommand command = (DeploymentProfileConfCommand) aldService.exec(deploymentProfileConfCommand);

        return command.getResult();
    }
    
    

    /**
     * @param envIds, environment id
     * @return the details of the environment deployment base info, for example the default profile
     * @throws RDOException
     */
    @Override
    public List<EnvironmentModel> envDeployProps(List<Integer> envIds) throws RDOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<EnvironmentModel> environmentModel = deploymentMapper.getEnvDeployProps(envIds);

        stopWatch.stop();
        logger.info("DeploymentService envDeployProps total time consumed in {} seconds.",
            stopWatch.getTotalTimeSeconds());
        return environmentModel;
    }

    /**
     * @param prfId, deployment profile id
     * @return The details of a specified deployment profile
     * @throws RDOException
     */
    @Override
    public DeploymentProfileModel getDeploymentProfileStep(Integer prfId) throws RDOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        DeploymentProfileModel profileModel;
        logger.debug("DeploymentService getDeploymentProfileStep by profile id {}", prfId);
        profileModel = deploymentMapper.getProfileStepDetails(prfId);
        stopWatch.stop();
        logger.info("DeploymentService getDeploymentProfileStep total time consumed in {} seconds.",
            stopWatch.getTotalTimeSeconds());
        return profileModel;
    }

    private String getDeploymentSetStatus(String deploymentSetStatusVal, Integer deploymentSetId,
                                          Date deploymentSetNotifyTime,
                                          Integer deploymentSetStatusCode, Integer lastRequestedStatus,
                                          List<Integer> recoveredDeploymentSetList, Date utcDateTime) {
        String retVal = "-";
        if (deploymentSetNotifyTime != null) {
            retVal = deploymentSetStatusVal;
            String deploymentSetNotifyTimeStr =
                    DateTimeUtil.parseDateToYMDSSString(deploymentSetNotifyTime);

            if(lastRequestedStatus == 0 && deploymentSetStatusCode != 2
                    && (deploymentSetNotifyTimeStr.indexOf("2999-01-01") != -1 || deploymentSetNotifyTime
                    .before(utcDateTime))) {
                retVal = "Timed Out";
            } else if(this.isRecovered(recoveredDeploymentSetList, deploymentSetId)) {
                retVal = "Recovered";
            } else if(
                    lastRequestedStatus == 0 && deploymentSetStatusCode == 0 &&
                            (deploymentSetNotifyTimeStr.indexOf("2999-01-01") != -1 || deploymentSetNotifyTime
                                    .before(utcDateTime))) {
                retVal = "Timed Out";
            } else if(lastRequestedStatus == 0 && deploymentSetStatusCode == 1 && (deploymentSetNotifyTimeStr.indexOf("2999-01-01") == -1 && deploymentSetNotifyTime
                    .after(utcDateTime))) {
                retVal = "Started";
            }
        }
        return retVal;
    }


    private String getDeploymentDestinationStatus(String deploymentSetStatusVal,
        Integer deploymentSetId, Date deploymentSetNotifyTime,
        Integer deploymentSetDestinationStatusCode, Integer lastRequestedStatus,
        List<Integer> recoveredDeploymentSetList, Date utcDateTime) {
        String retVal = "-";
        if (deploymentSetNotifyTime != null) {
            retVal = deploymentSetStatusVal;
            String deploymentSetNotifyTimeStr =
                DateTimeUtil.parseDateToYMDSSString(deploymentSetNotifyTime);
            //ARDO-1570: compare with old web portal, when deploymentSetDestinationStatusCode is null should also
            //be the true condition since the default value for number type is 0 in typescript.
            if (deploymentSetDestinationStatusCode == null
                || deploymentSetDestinationStatusCode == 0) {
                if (StringUtils.isNotEmpty(deploymentSetNotifyTimeStr)
                    && deploymentSetNotifyTimeStr.indexOf("2999-01-01") != -1) {
                    retVal = "Timed Out";
                }
                if (deploymentSetNotifyTime != null && deploymentSetNotifyTime
                    .before(utcDateTime)) {
                    retVal = "Timed Out";
                }
            } else if (isRecovered(recoveredDeploymentSetList, deploymentSetId)) {
                retVal = "Recovered";
            }
        }
        return retVal;
    }

    private boolean isRecovered(List<Integer> recoveredDeploymentSetList, Integer deploymentSetId) {
        boolean retVal = false;
        for (Integer problemSetId : recoveredDeploymentSetList) {
            if (deploymentSetId != null && problemSetId !=null  && problemSetId.intValue() == deploymentSetId.intValue()) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }


    @CachePut(value = "operationSystems", key="'os'")
    public List<OperationSystemModel> getRealtimeOperationSystemList() throws RDOException {

        List<OperationSystemModel> operationSystems = deploymentMapper.getOperationSystemList();
        return operationSystems;
    }


    @Cacheable(value = "operationSystems", sync = true, key="'os'")
    public List<OperationSystemModel> getNonRealtimeOperationSystemList() throws RDOException {

        List<OperationSystemModel> operationSystems = deploymentMapper.getOperationSystemList();
        return operationSystems;
    }

    @Override
    public List<Computer> getComputerList(String name, String description, String osId, String osVersion, String deployAllowed, String client) throws RDOException {
        name = StringUtil.getKeywordWrapper(name).toUpperCase();
        description = StringUtil.getKeywordWrapper(description).toUpperCase();
        osVersion = StringUtil.getKeywordWrapper(osVersion).toUpperCase();
        PageUtil.startPage(null);
        List<Computer> computerList = deploymentMapper.getComputerList(name, description, osId, osVersion, deployAllowed, client);
        PageUtil.clearPage();
        return computerList;
    }

    @Override
    public Integer getComputerCounts() throws RDOException {
        Integer counts = deploymentMapper.getComputerCounts(null, null, null,null, null, null);
        return counts;
    }

    @Override
    public Map<String, Object> getComputersMap(String name, String description, String osId, String osVersion, String deployAllowed, String client) throws RDOException {
        Map<String, Object> computerMap = new HashMap<>();
        name = StringUtil.getKeywordWrapper(name).toUpperCase();
        description = StringUtil.getKeywordWrapper(description).toUpperCase();
        osVersion = StringUtil.getKeywordWrapper(osVersion).toUpperCase();
        PageUtil.startPage(null);
        List<Computer> computerList = deploymentMapper.getComputerList(name, description, osId, osVersion, deployAllowed, client);
        PageUtil.clearPage();
        Integer counts = deploymentMapper.getComputerCounts(name, description, osId, osVersion, deployAllowed, client);
        computerMap.put("computerList", computerList);
        computerMap.put("totalCount", counts);
        return computerMap;
    }


}
