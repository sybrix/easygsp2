package sybrix.easygsp2.security

import com.fasterxml.jackson.annotation.JsonIgnore

class Client {
        @JsonIgnore
        Long id
        String clientId
        String clientSecret
        String clientName
}
