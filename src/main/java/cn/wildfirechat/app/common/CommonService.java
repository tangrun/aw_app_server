package cn.wildfirechat.app.common;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.common.repository.UploadFileRepository;
import cn.wildfirechat.app.tools.ShortUUIDGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.UUID;

@Service
public class CommonService {

    @Value("${local.media.temp_storage}")
    private String ossTempPath;

    @Value("${local.file.download.domain}")
    public String localFileDownloadDomain;

    @Resource
    private UploadFileRepository uploadFileRepository;

    public Object downloadFile(String id, HttpServletResponse response) {
        UploadFile uploadFile = uploadFileRepository.findById(id).orElse(null);
        if (uploadFile== null){
            return RestResult.error(RestResult.RestCode.ERROR_FILE_DOWNLOAD_ERROR);
        }
        try {
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(uploadFile.getLocalName(), "UTF-8"));
            IOUtils.copyLarge(new FileInputStream(new File(ossTempPath,uploadFile.getLocalPath())), response.getOutputStream());
            return null;
        }catch (Exception e){
            return RestResult.error(RestResult.RestCode.ERROR_FILE_DOWNLOAD_ERROR);
        }
    }

    public UploadFile uploadFile(String user,MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        String suffix = "";
        if (i > 0) {
            suffix = originalFilename.substring(i);
        }

        String fileName = System.currentTimeMillis() + "-" + uuid + "" + suffix;
        Calendar instance = Calendar.getInstance();
        String fileDir =  instance.get(Calendar.YEAR)
                + File.separator + (instance.get(Calendar.MONTH))
                + File.separator + instance.get(Calendar.DAY_OF_MONTH);
        File localFile = new File(ossTempPath + File.separator  + fileDir, fileName);
        try {
            File parentFile = localFile.getParentFile();
            if (!parentFile.exists()){
                parentFile.mkdirs();
            }
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }

        UploadFile uploadFile = new UploadFile();
        uploadFile.setId(uuid);
        uploadFile.setLocalName(fileName);
        uploadFile.setLocalPath(fileDir+File.separator+fileName);
        uploadFile.setOriginName(originalFilename);
        uploadFile.setUser(user);

        uploadFileRepository.saveAndFlush(uploadFile);

        return uploadFile;
    }

}
