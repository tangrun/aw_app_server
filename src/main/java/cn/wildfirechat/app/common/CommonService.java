package cn.wildfirechat.app.common;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.common.repository.UploadFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public String getDownloadPath(UploadFile uploadFile){
        return localFileDownloadDomain + uploadFile.getId();
    }

    public Object downloadFile(String id, HttpServletResponse response) {
        UploadFile uploadFile = uploadFileRepository.findById(id).orElse(null);
        if (uploadFile == null) {
            return RestResult.error(RestResult.RestCode.ERROR_FILE_DOWNLOAD_ERROR);
        }
        File file = new File(ossTempPath, uploadFile.getLocalPath());

        try {
            if (file.exists() && file.isFile()){
                String contentType = StringUtils.isBlank(uploadFile.getMimetype()) ? "application/octet-stream" : uploadFile.getMimetype();
                response.setHeader("content-type", contentType);
                response.setHeader("content-length", file.length()+"");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(uploadFile.getLocalName(), "UTF-8"));
                IOUtils.copyLarge(new FileInputStream(file), response.getOutputStream());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(RestResult.RestCode.ERROR_FILE_DOWNLOAD_ERROR);
    }

    public UploadFile uploadFile(String user, MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        String suffix = "";
        if (i > 0) {
            suffix = originalFilename.substring(i);
        }

        String fileName = System.currentTimeMillis() + "-" + uuid + "" + suffix;
        Calendar instance = Calendar.getInstance();
        String fileDir = instance.get(Calendar.YEAR)
                + File.separator + (instance.get(Calendar.MONTH))
                + File.separator + instance.get(Calendar.DAY_OF_MONTH);
        File localFile = new File(ossTempPath + File.separator + fileDir, fileName);

        String md5;
        try {
            File parentFile = localFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.transferTo(localFile);
            FileInputStream fileInputStream = new FileInputStream(localFile);
            boolean needDelete = false;
            try {

                md5 = DigestUtils.md5Hex(fileInputStream);
                if (StringUtils.isNotBlank(md5)) {
                    UploadFile uploadFile = uploadFileRepository.findByMd5(md5);
                    if (uploadFile != null) {
                        needDelete = true;
                        return uploadFile;
                    }
                }
            } finally {
                IOUtils.closeQuietly(fileInputStream);
                if (needDelete)
                    localFile.delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }

        UploadFile uploadFile = new UploadFile();
        uploadFile.setId(uuid);
        uploadFile.setLocalName(fileName);
        uploadFile.setLocalPath(fileDir + File.separator + fileName);
        uploadFile.setOriginName(originalFilename);
        uploadFile.setUser(user);
        uploadFile.setMd5(md5);
        {
            Tika tika = new Tika();
            try {
                String detect = tika.detect(localFile);
                uploadFile.setMimetype(detect);
            } catch (IOException e) {
            }
        }
        uploadFile.setSize(file.getSize());

        uploadFileRepository.saveAndFlush(uploadFile);

        return uploadFile;
    }

}
