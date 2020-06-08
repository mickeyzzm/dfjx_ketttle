package org.seaboxdata.systemmng.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.seaboxdata.systemmng.entity.UserGroupAttributeEntity;
import org.seaboxdata.systemmng.util.data.JsonResp;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import net.sf.json.JSONObject;

/**
 * 文件的上传与下载
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/fileUploadDown")
public class FileUploadAndDownController {

	// 最大文件大小
	private long maxSize = 1000000;
	
	@RequestMapping(value = "fileUpload", method = RequestMethod.POST)
	@ResponseBody
	public String fileUpload(HttpServletRequest request, HttpServletResponse response) {
		JsonResp result = new JsonResp();
		try {
			UserGroupAttributeEntity attr=(UserGroupAttributeEntity)request.getSession().getAttribute("userInfo");
            String savePath = "/data/kettle/upload";
			if(null!=attr){
            	savePath = savePath + "/" + attr.getUserGroupName();
            }
			
			String validate = this.validateFields(request);
			if ("true".equals(validate)) {
				Map<String, String> map = this.initFields(request, savePath);
				if("true".equals(map.get("success"))) {
					result.setStatus(true);
					result.setResult(map.get("filePath"));
				} else {
					result.setStatus(false);
					result.setMessage(map.get("msg"));
				}
			} else {
				result.setStatus(false);
				result.setMessage(validate);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}
		
		return JSONObject.fromObject(result).toString();
	}

	public String replaceDataStr() {
		String pattern = "\\{(\\w+)\\}";
		Pattern p = Pattern.compile(pattern);
		String str = "/{yyyy}/{MM}/{dd}/";
		Matcher m = p.matcher(str);
		while (m.find()) {
			String format = m.group(1);
			if (format != null) {
				str = str.replaceFirst(pattern, dateToString(new Date(), format));
			}
		}
		return str;
	}

	public String dateToString(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	/**
	 * 获取文件后缀
	 * 
	 * @param fileName
	 * @return
	 */
	public String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf(".");
		if (index > 0) {
			return fileName.substring(index).toLowerCase();
		} else {
			return "";
		}
	}

	/**
	 * 上传验证,并初始化文件目录
	 *
	 * @param request
	 */
	private String validateFields(HttpServletRequest request) {
		String errorInfo = "true";
		// boolean errorFlag = true;
		// 获取内容类型
		String contentType = request.getContentType();
		int contentLength = request.getContentLength();
		if (contentType == null || !contentType.startsWith("multipart")) {
			System.out.println("请求不包含multipart/form-data流");
			errorInfo = "请求不包含multipart/form-data流";
		} else if (maxSize < contentLength) {
			System.out.println("上传文件大小超出文件最大大小");
			errorInfo = "上传文件大小超出文件最大大小[" + maxSize + "]";
		} else if (!ServletFileUpload.isMultipartContent(request)) {
			errorInfo = "请选择文件";
		} else {
			
		}

		return errorInfo;
	}

	/**
	 * 处理上传内容
	 *
	 * @param request
	 * @param maxSize
	 * @return
	 */
//    @SuppressWarnings("unchecked")
	private Map<String, String> initFields(HttpServletRequest request, String savePath ) throws Exception{

		// 存储表单字段和非表单字段
		Map<String, String> map = new HashMap<String, String>();
		map.put("success", "false");
		map.put("msg", "上传文件失败!");
		try {
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
			if (multipartResolver.isMultipart(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				Iterator<String> iter = multiRequest.getFileNames();
				while (iter.hasNext()) {
					MultipartFile file = multiRequest.getFile((String) iter.next());
					// 有上传文件的话，容量是大于0的。
					if (file.getSize() > 0) {
						String fileName = file.getOriginalFilename();
						String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

						savePath = savePath + replaceDataStr() + ext + "/";
						File dirFile = new File(savePath);
						if (!dirFile.exists()) {
							dirFile.mkdirs();
						}

						String targetFileName = UUID.randomUUID() + "." + ext;

						File localFile = new File(savePath, targetFileName);
						file.transferTo(localFile);
						
						map.put("success", "true");
						map.put("filePath", savePath + targetFileName);
					}
				}
				
				map.put("msg", "上传文件成功!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("msg", e.getMessage());
		}
		
		return map;
	}
}
