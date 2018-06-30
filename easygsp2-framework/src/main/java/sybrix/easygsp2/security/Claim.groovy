package sybrix.easygsp2.security

class Scope {
    String claimName
    String value

    public Scope(){

    }

    public Scope(String claimName, String value){
        this.claimName = claimName
        this.value =  value
    }

    public Scope(ClaimType claimType, String value){
        this.claimName = claimType.val()
        this.value =  value
    }
}
