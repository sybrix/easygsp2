package sybrix.easygsp2.templates;


import java.io.InputStream;

public class ErrorFile {
        private InputStream inputStream;

        ErrorFile(InputStream inputStream){
                this.inputStream = inputStream;
        }

        public InputStream getInputStream() {
                return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
                this.inputStream = inputStream;
        }
}
