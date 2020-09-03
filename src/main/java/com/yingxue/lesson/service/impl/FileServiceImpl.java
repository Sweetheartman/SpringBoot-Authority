package com.yingxue.lesson.service.impl;

import com.github.pagehelper.PageHelper;
import com.yingxue.lesson.entity.SysFile;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysFileMapper;
import com.yingxue.lesson.service.FileService;
import com.yingxue.lesson.utils.PageUtil;
import com.yingxue.lesson.vo.req.FilePageReqVO;
import com.yingxue.lesson.vo.resp.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: permission-actual-project
 * @description: 文件上传服务接口实现类
 * @author: lidekun
 * @create: 2020-08-30 14:23
 **/
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Value("${file.path}")
    private String FILE_PATH;

    @Value("${file.base-url}")
    private String baseUrl;

    @Autowired
    private SysFileMapper sysFileMapper;

    @Override
    public String upload(MultipartFile file, String userId, Integer type) {
        String originalFilename = file.getOriginalFilename();
        String extensionName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + "." + extensionName;

        File destFile = new File(FILE_PATH + fileName);

        if(!destFile.getParentFile().exists()){
            destFile.getParentFile().mkdirs();
        }
        try{
            file.transferTo(destFile);
        }catch(IOException e){
            e.printStackTrace();
            log.error("上传失败:{}", e);
            throw new BusinessException(BaseResponseCode.UPLOAD_FILE_ERROR);
        }
        String fileUrl = baseUrl + fileName;
        log.info("baseUrl:{}", fileUrl);
        SysFile sysFile=new SysFile();
        sysFile.setId(UUID.randomUUID().toString());
        sysFile.setFileName(fileName);
        sysFile.setOriginalName(originalFilename);
        sysFile.setExtensionName(extensionName);
        sysFile.setCreateId(userId);
        sysFile.setType(type);
        sysFile.setFileUrl(fileUrl);
        sysFile.setSize(FileUtils.byteCountToDisplaySize(file.getSize()));
        sysFile.setCreateTime(new Date());
        int i = sysFileMapper.insertSelective(sysFile);
        if(i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        return fileUrl;
    }

    @Override
    public void download(String fileId, HttpServletResponse response) {
        SysFile sysFile = sysFileMapper.selectByPrimaryKey(fileId);
        if(sysFile == null){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        try {
            // 设置响应内容ISO-8859-1
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String fileName = new String(sysFile.getOriginalName().getBytes("UTF-8"), "ISO-8859-1");

            // 文件下载响应头设置
            // inline:直接在页面显示;attachment以附件形式下载
            response.setHeader("content-disposition",String.format("attachment;filename=%s",fileName));
        } catch (UnsupportedEncodingException e) {
           throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        ServletOutputStream outputStream=null;
        try {
            File file=new File(FILE_PATH+sysFile.getFileName());
            outputStream = response.getOutputStream();
            // 将文件以二进制形式读取，然后再写入响应文件流
            IOUtils.write(FileUtils.readFileToByteArray(file),outputStream);
        } catch (IOException e) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int deleteByFileUrl(String fileUrl) {
        //删除了文件信息表里面的数据
        int i = sysFileMapper.deleteByFileUrl(fileUrl);
        String fileName=fileUrl.substring(fileUrl.lastIndexOf("/")+1);
        //删除磁盘文件
        deleteDestFile(fileName);
        return i;
    }

    private void deleteDestFile(String fileName){
        File file=new File(FILE_PATH+fileName);
        if(file.exists()){
            file.delete();
        }
    }

    @Override
    public PageVO<SysFile> pageInfo(FilePageReqVO vo, String userId) {
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());
        List<SysFile> sysFiles = sysFileMapper.selectByUserId(userId);
        return PageUtil.getPageVo(sysFiles);
    }
}
