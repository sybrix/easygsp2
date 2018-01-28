package sybrix.easygsp2.templates;


import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TemplateInfo {

        private boolean errorOccurred;
        private String uniqueTemplateScriptName;
        private String requestUri;
        private File requestFile;

        private String appFolderClassPathLocation;
        private String templateRoot;
        private Set<File> children = new HashSet<File>();

        private boolean templateRequest;
        private boolean codeBehindNewer;

        public File getRequestFile() {
                return requestFile;
        }

        public void setRequestFile(File requestFile) {
                this.requestFile = requestFile;
        }

        public Set<File> getChildren() {
                return children;
        }

        public void setChildren(Set<File> children) {
                this.children = children;
        }


        public boolean errorOccurred() {
                return errorOccurred;
        }

        public void setErrorOccurred(boolean errorOccurred) {
                this.errorOccurred = errorOccurred;
        }

        public String getUniqueTemplateScriptName() {
                return uniqueTemplateScriptName;
        }

        public void setUniqueTemplateScriptName(String uniqueTemplateScriptName) {
                this.uniqueTemplateScriptName = uniqueTemplateScriptName;
        }

        public String getRequestUri() {
                return requestUri;
        }

        public void setRequestUri(String requestUri) {
                this.requestUri = requestUri;
                int i = requestUri.lastIndexOf('/');
                if (i >= 0) {
                        templateRoot = requestUri.substring(0, i + 1);
                } else {
                        templateRoot = "";
                }
        }

        public String getAppFolderClassPathLocation() {
                return appFolderClassPathLocation;
        }

        public void setAppFolderClassPathLocation(String appFolderClassPathLocation) {
                this.appFolderClassPathLocation = appFolderClassPathLocation;
        }


        public String getTemplateRoot() {
                return templateRoot;
        }

        public void setTemplateRequest(boolean templateRequest) {
                this.templateRequest = templateRequest;
        }

        public boolean isTemplateRequest() {
                return templateRequest;
        }

        public boolean isCodeBehindNewer() {
                return codeBehindNewer;
        }
}
