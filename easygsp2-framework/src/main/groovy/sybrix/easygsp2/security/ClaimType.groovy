package sybrix.easygsp2.security


enum ClaimType {
    ISSUER('iis'),
    SUBJECT('sub'),
    AUDIENCE('aud'),
    EXPIRATION_TIMESTAMP('exp'),
    NOT_BEFORE_TIMESTAMP('nbf'),
    ISSUED_AT_TIMESTAMP('iat'),
    UNIQUE_ID('jti'),
    VALIDATED('val'),
    PROFILE_ID('pid')

    private final String claimName

    ClaimType(String claimName) {
        this.claimName = claimName
    }

    String val() {
        claimName
    }
}
