package com.rocketsoftware.rdoi.dao.rdoi.mapper;

import com.rocketsoftware.rdoi.controller.vo.libGrp.LibGroupVo;
import com.rocketsoftware.rdoi.dao.rdoi.model.LibGroupAssignmentModel;
import com.rocketsoftware.rdoi.dao.rdoi.model.LibGroupModel;

import java.util.List;

public interface LibGroupMapper {

     List<LibGroupModel> getLibGroupByGAR(String groupId, String prdId, String relId);

     List<LibGroupAssignmentModel> getLibGroupAssignmentByGAR(String groupId, String prdId, String relId);


}