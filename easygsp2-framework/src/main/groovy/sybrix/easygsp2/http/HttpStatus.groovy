package sybrix.easygsp2.http;

public enum HttpStatus {

        OK(200),
        CREATED(201),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        BAD_REQUEST(400),
        INTERNAL_SERVER_ERROR(500),
        UNSUPPORTED_MEDIA_TYPE(406)

        private final Integer val

        HttpStatus(Integer val) {
                this.val = val
        }

        Integer val() {
                val
        }
}
