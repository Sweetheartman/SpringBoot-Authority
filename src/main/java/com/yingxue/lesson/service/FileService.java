package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysFile;
import com.yingxue.lesson.vo.req.FilePageReqVO;
import com.yingxue.lesson.vo.resp.PageVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * @program: permission-actual-project
 * @description: 文件上传接口
 * @author: lidekun
 * @create: 2020-08-30 14:22
 **/
public interface FileService {

    String upload(MultipartFile file, String userId, Integer type);
    void download(String fileId, HttpServletResponse response);
    int deleteByFileUrl(String fileUrl);

    PageVO<SysFile> pageInfo(FilePageReqVO vo, String userId);
}
