package sybrix.easygsp2.exceptions

class ApiError {
        ApiError(Integer code, String message) {
                this.code = code
                this.message = message
        }

        ApiError(String message) {
                this.message = message
        }
        Integer code
        String message
}
