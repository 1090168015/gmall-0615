package com.atguigu.gmall.usm.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

import lombok.Data;

/**
 * 会员收藏的专题活动
 * 
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:21:33
 */
@ApiModel
@Data
@TableName("ums_member_collect_subject")
public class MemberCollectSubjectEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	@ApiModelProperty(name = "id",value = "id")
	private Long id;
	/**
	 * subject_id
	 */
	@ApiModelProperty(name = "subjectId",value = "subject_id")
	private Long subjectId;
	/**
	 * subject_name
	 */
	@ApiModelProperty(name = "subjectName",value = "subject_name")
	private String subjectName;
	/**
	 * subject_img
	 */
	@ApiModelProperty(name = "subjectImg",value = "subject_img")
	private String subjectImg;
	/**
	 * 活动url
	 */
	@ApiModelProperty(name = "subjectUrll",value = "活动url")
	private String subjectUrll;

}
