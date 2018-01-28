package sybrix.easygsp2.fileupload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import sybrix.easygsp2.security.EasyGspServletRequest;
import sybrix.easygsp2.util.PropertiesFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileUpload {


        public static List<FileItem> parseFileUploads(EasyGspServletRequest request, PropertiesFile propertiesFile) throws FileUploadException {
                if (ServletFileUpload.isMultipartContent(request)) {
                        DiskFileItemFactory factory = new DiskFileItemFactory(propertiesFile.getInt("fileUpload.threshold", 10240), new File(propertiesFile.getString("fileUpload.tempDirectory"), System.getProperty("java.io.tmpdir")));
                        Map<String, Object> uploads = request.getUploads();

                        // Create a new file upload handler
                        ServletFileUpload upload = new ServletFileUpload(factory);

                        upload.setFileSizeMax(propertiesFile.getLong("fileUpload.maxFileSize", -1L));

                        // Set overall request size constraint
                        upload.setSizeMax(propertiesFile.getLong("fileUpload.maxRequestSize", -1L));

                        // Parse the request
                        List<FileItem> l = upload.parseRequest(request);

                        for (FileItem fileItem : l) {
                                if (fileItem.isFormField()) {
                                        request.getParameterMap().put(fileItem.getFieldName(), new String[]{fileItem.getString()});
                                } else {
                                        if (fileItem.getSize() > 0) {
                                                if (uploads.containsKey(fileItem.getFieldName())) {
                                                        Object obj = uploads.get(fileItem.getFieldName());
                                                        if (obj instanceof List) {
                                                                ((List) obj).add(fileItem);
                                                        } else {
                                                                List<FileItem> newUploads = new ArrayList();
                                                                newUploads.add((FileItem) obj); // add existing item
                                                                newUploads.add(fileItem);  // add new item
                                                                uploads.put(fileItem.getFieldName(), newUploads);

                                                        }
                                                } else {
                                                        uploads.put(fileItem.getFieldName(), fileItem);
                                                }
                                        }
                                }

                        }

                        return l;
                } else {
                        return new ArrayList();
                }
        }
}
